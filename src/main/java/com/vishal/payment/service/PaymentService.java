package com.vishal.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vishal.payment.client.PaymentGatewayClient;
import com.vishal.payment.config.PaymentGatewayProperties;
import com.vishal.payment.domain.PaymentStatus;
import com.vishal.payment.domain.PaymentTransaction;
import com.vishal.payment.domain.callback.CallbackEvent;
import com.vishal.payment.domain.merchant.Merchant;
import com.vishal.payment.dto.*;
import com.vishal.payment.exception.PaymentGatewayException;
import com.vishal.payment.exception.ResourceNotFoundException;
import com.vishal.payment.repository.CallbackEventRepository;
import com.vishal.payment.repository.PaymentTransactionRepository;
import com.vishal.payment.security.AesCbcCryptoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    private final PaymentTransactionRepository repository;
    private final CallbackEventRepository callbackEventRepository;
    private final PaymentGatewayProperties properties;
    private final AesCbcCryptoService cryptoService;
    private final PaymentGatewayClient gatewayClient;
    private final PaymentMapper mapper;
    private final OrderNumberGenerator orderNumberGenerator;
    private final ObjectMapper objectMapper;
    private final MerchantService merchantService;
    private final PaymentStateMachine stateMachine;
    private final AuditService auditService;
    private final CallbackProcessor callbackProcessor;

    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        Merchant merchant = merchantService.getActiveMerchant(properties.mid());
        String orderNo = orderNumberGenerator.generate();
        PaymentTransaction tx = PaymentTransaction.builder()
                .mid(merchant.getMid())
                .orderNo(orderNo)
                .amount(request.amount())
                .currency(request.currency())
                .userId(request.userId())
                .customerVpa(request.customerVpa())
                .mobileNo(request.mobileNo())
                .emailId(request.emailId())
                .customerName(request.name())
                .status(PaymentStatus.INITIATED)
                .build();
        repository.save(tx);
        auditService.record(orderNo, null, PaymentStatus.INITIATED, "PAYMENT_CREATED", writeJson(request), null);

        ProviderPaymentPayload providerPayload = mapper.toProviderPayload(request, orderNo, merchant);
        String encryptedPayload = cryptoService.encrypt(writeJson(providerPayload), merchant.getEncryptionKey());

        ProviderPaymentResponse providerResponse = gatewayClient.createPayment(new ProviderEncryptedRequest(merchant.getMid(), encryptedPayload));
        applyProviderResponse(tx, providerResponse);
        return mapper.toResponse(tx);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getByOrderNo(String orderNo) {
        return repository.findByOrderNo(orderNo).map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for orderNo: " + orderNo));
    }

    @Transactional
    public CallbackAckResponse handleCallback(CallbackRequest request, String sourceIp) {
        if (callbackEventRepository.existsBySignature(request.signature())) {
            log.info("duplicate_callback_ignored mid={} signature={}", request.mid(), request.signature());
            return CallbackAckResponse.accepted();
        }
        try {
            CallbackEvent event = callbackEventRepository.save(CallbackEvent.builder()
                    .signature(request.signature())
                    .encryptedData(request.data())
                    .processed(false)
                    .build());
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    callbackProcessor.processAsync(event.getId(), request.mid(), sourceIp);
                }
            });
            return CallbackAckResponse.accepted();
        } catch (DataIntegrityViolationException ex) {
            return CallbackAckResponse.accepted();
        }
    }

    private void applyProviderResponse(PaymentTransaction tx, ProviderPaymentResponse response) {
        tx.setProviderRawResponse(writeJson(response));
        PaymentStatus oldStatus = tx.getStatus();
        PaymentStatus newStatus = PaymentStatus.PROCESSING;
        if (response != null && response.responseData() != null) {
            tx.setTxnId(response.responseData().txnId());
            tx.setQrString(response.responseData().qrString());
            newStatus = "SUCCESS".equalsIgnoreCase(response.message()) ? PaymentStatus.PROVIDER_ACCEPTED : PaymentStatus.PROCESSING;
        }
        moveStatus(tx, newStatus, "PROVIDER_RESPONSE", tx.getProviderRawResponse(), null);
    }

    public void moveStatus(PaymentTransaction tx, PaymentStatus newStatus, String eventType, String rawPayload, String sourceIp) {
        PaymentStatus oldStatus = tx.getStatus();
        if (!stateMachine.canMove(oldStatus, newStatus)) {
            log.warn("illegal_status_transition_ignored orderNo={} from={} to={}", tx.getOrderNo(), oldStatus, newStatus);
            return;
        }
        tx.setStatus(newStatus);
        if (stateMachine.isTerminal(newStatus)) tx.setCompletedAt(java.time.Instant.now());
        repository.save(tx);
        auditService.record(tx.getOrderNo(), oldStatus, newStatus, eventType, rawPayload, sourceIp);
        log.info("payment_status_changed orderNo={} txnId={} from={} to={}", tx.getOrderNo(), tx.getTxnId(), oldStatus, newStatus);
    }

    private String writeJson(Object value) {
        try { return objectMapper.writeValueAsString(value); }
        catch (JsonProcessingException ex) { throw new PaymentGatewayException("JSON serialization failed", ex); }
    }
}
