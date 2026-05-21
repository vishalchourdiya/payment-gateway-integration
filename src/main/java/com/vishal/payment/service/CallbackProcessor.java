package com.vishal.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vishal.payment.domain.PaymentStatus;
import com.vishal.payment.domain.PaymentTransaction;
import com.vishal.payment.domain.callback.CallbackEvent;
import com.vishal.payment.domain.merchant.Merchant;
import com.vishal.payment.dto.PaymentCallbackData;
import com.vishal.payment.repository.CallbackEventRepository;
import com.vishal.payment.repository.PaymentTransactionRepository;
import com.vishal.payment.security.AesCbcCryptoService;
import com.vishal.payment.security.ChecksumService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class CallbackProcessor {
    private final CallbackEventRepository callbackEventRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final MerchantService merchantService;
    private final AesCbcCryptoService cryptoService;
    private final ChecksumService checksumService;
    private final ObjectMapper objectMapper;
    private final PaymentStateMachine stateMachine;
    private final AuditService auditService;

    @Async
    @Transactional
    public void processAsync(Long eventId, String mid, String sourceIp) {
        CallbackEvent event = callbackEventRepository.findById(eventId).orElseThrow();
        try {
            Merchant merchant = merchantService.getActiveMerchant(mid);
            String decryptedJson = cryptoService.decrypt(event.getEncryptedData(), merchant.getEncryptionKey());
            event.setDecryptedData(decryptedJson);
            PaymentCallbackData data = objectMapper.readValue(decryptedJson, PaymentCallbackData.class);
            event.setOrderNo(data.orderNo());

            if (!checksumService.isValidCallbackSignature(data, event.getSignature())) {
                event.setFailureReason("INVALID_SIGNATURE");
                markSignatureFailed(data, decryptedJson, sourceIp);
                return;
            }

            PaymentTransaction tx = transactionRepository.findByOrderNo(data.orderNo())
                    .orElseGet(() -> PaymentTransaction.builder()
                            .mid(data.mid())
                            .orderNo(data.orderNo())
                            .amount(new BigDecimal(data.amount()))
                            .currency(data.currency())
                            .status(PaymentStatus.PROCESSING)
                            .build());

            if (tx.isTerminal() && event.getSignature().equals(tx.getLastCallbackSignature())) {
                log.info("idempotent_callback_noop orderNo={} txnId={}", tx.getOrderNo(), tx.getTxnId());
                event.setProcessed(true);
                event.setProcessedAt(Instant.now());
                return;
            }

            tx.setTxnId(data.txnId());
            tx.setCustRefNo(data.custRefNo());
            tx.setTxnRespCode(data.txnRespCode());
            tx.setProviderTxnStatus(data.txnStatus());
            tx.setLastCallbackPayload(decryptedJson);
            tx.setLastCallbackSignature(event.getSignature());
            PaymentStatus mappedStatus = mapCallbackStatus(data.txnRespCode(), data.txnStatus());
            moveStatus(tx, mappedStatus, "CALLBACK_PROCESSED", decryptedJson, sourceIp);

            event.setProcessed(true);
            event.setProcessedAt(Instant.now());
        } catch (JsonProcessingException ex) {
            event.setFailureReason("CALLBACK_JSON_PARSE_FAILED");
            log.error("callback_json_parse_failed eventId={} reason={}", eventId, ex.getMessage());
        } catch (Exception ex) {
            event.setFailureReason(ex.getMessage());
            log.error("callback_processing_failed eventId={} reason={}", eventId, ex.getMessage(), ex);
        }
    }

    private void moveStatus(PaymentTransaction tx, PaymentStatus newStatus, String eventType, String rawPayload, String sourceIp) {
        PaymentStatus oldStatus = tx.getStatus();
        if (!stateMachine.canMove(oldStatus, newStatus)) {
            log.warn("illegal_status_transition_ignored orderNo={} from={} to={}", tx.getOrderNo(), oldStatus, newStatus);
            return;
        }
        tx.setStatus(newStatus);
        if (stateMachine.isTerminal(newStatus)) tx.setCompletedAt(Instant.now());
        transactionRepository.save(tx);
        auditService.record(tx.getOrderNo(), oldStatus, newStatus, eventType, rawPayload, sourceIp);
        log.info("payment_status_changed orderNo={} txnId={} from={} to={}", tx.getOrderNo(), tx.getTxnId(), oldStatus, newStatus);
    }

    private void markSignatureFailed(PaymentCallbackData data, String decryptedJson, String sourceIp) {
        if (data.orderNo() == null) return;
        PaymentTransaction tx = transactionRepository.findByOrderNo(data.orderNo()).orElse(null);
        if (tx != null) {
            moveStatus(tx, PaymentStatus.CALLBACK_SIGNATURE_FAILED, "CALLBACK_SIGNATURE_FAILED", decryptedJson, sourceIp);
        } else {
            auditService.record(data.orderNo(), null, PaymentStatus.CALLBACK_SIGNATURE_FAILED, "CALLBACK_SIGNATURE_FAILED", decryptedJson, sourceIp);
        }
    }

    private PaymentStatus mapCallbackStatus(String code, String status) {
        if ("200".equals(code) || "success".equalsIgnoreCase(status)) return PaymentStatus.SUCCESS;
        if ("198".equals(code)) return PaymentStatus.PROCESSING;
        if ("197".equals(code)) return PaymentStatus.REJECTED;
        if ("194".equals(code)) return PaymentStatus.DECLINED;
        return PaymentStatus.FAILED;
    }
}
