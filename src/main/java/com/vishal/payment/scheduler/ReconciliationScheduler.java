package com.vishal.payment.scheduler;

import com.vishal.payment.config.PaymentGatewayProperties;
import com.vishal.payment.domain.PaymentStatus;
import com.vishal.payment.domain.PaymentTransaction;
import com.vishal.payment.repository.PaymentTransactionRepository;
import com.vishal.payment.service.AuditService;
import com.vishal.payment.service.PaymentStateMachine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReconciliationScheduler {
    private final PaymentTransactionRepository repository;
    private final PaymentGatewayProperties properties;
    private final PaymentStateMachine stateMachine;
    private final AuditService auditService;

    @Scheduled(cron = "${payment.gateway.reconciliation-cron:0 */10 * * * *}")
    @Transactional
    public void markStaleTransactionsForReconciliation() {
        Instant threshold = Instant.now().minus(properties.stalePaymentMinutes(), ChronoUnit.MINUTES);
        List<PaymentTransaction> stale = repository.findTop50ByStatusInAndCreatedAtBeforeOrderByCreatedAtAsc(
                List.of(PaymentStatus.INITIATED, PaymentStatus.PROVIDER_ACCEPTED, PaymentStatus.PROCESSING), threshold);
        for (PaymentTransaction tx : stale) {
            PaymentStatus old = tx.getStatus();
            if (!stateMachine.canMove(old, PaymentStatus.RECONCILIATION_REQUIRED)) continue;
            tx.setStatus(PaymentStatus.RECONCILIATION_REQUIRED);
            repository.save(tx);
            auditService.record(tx.getOrderNo(), old, PaymentStatus.RECONCILIATION_REQUIRED, "RECONCILIATION_MARKED", null, null);
            log.warn("payment_marked_for_reconciliation orderNo={} oldStatus={}", tx.getOrderNo(), old);
        }
    }
}
