package com.vishal.payment.service;

import com.vishal.payment.domain.PaymentStatus;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.Set;

@Component
public class PaymentStateMachine {
    private static final Map<PaymentStatus, Set<PaymentStatus>> ALLOWED = Map.ofEntries(
            Map.entry(PaymentStatus.INITIATED, Set.of(PaymentStatus.PROVIDER_ACCEPTED, PaymentStatus.PROCESSING, PaymentStatus.FAILED, PaymentStatus.RECONCILIATION_REQUIRED)),
            Map.entry(PaymentStatus.PROVIDER_ACCEPTED, Set.of(PaymentStatus.PROCESSING, PaymentStatus.SUCCESS, PaymentStatus.FAILED, PaymentStatus.DECLINED, PaymentStatus.REJECTED, PaymentStatus.RECONCILIATION_REQUIRED)),
            Map.entry(PaymentStatus.PROCESSING, Set.of(PaymentStatus.SUCCESS, PaymentStatus.FAILED, PaymentStatus.DECLINED, PaymentStatus.REJECTED, PaymentStatus.EXPIRED, PaymentStatus.RECONCILIATION_REQUIRED)),
            Map.entry(PaymentStatus.RECONCILIATION_REQUIRED, Set.of(PaymentStatus.SUCCESS, PaymentStatus.FAILED, PaymentStatus.DECLINED, PaymentStatus.REJECTED, PaymentStatus.EXPIRED)),
            Map.entry(PaymentStatus.CALLBACK_SIGNATURE_FAILED, Set.of(PaymentStatus.RECONCILIATION_REQUIRED)),
            Map.entry(PaymentStatus.CALLBACK_DECRYPTION_FAILED, Set.of(PaymentStatus.RECONCILIATION_REQUIRED))
    );

    public boolean canMove(PaymentStatus from, PaymentStatus to) {
        if (from == null || from == to) return true;
        if (isTerminal(from)) return false;
        return ALLOWED.getOrDefault(from, Set.of()).contains(to);
    }

    public boolean isTerminal(PaymentStatus status) {
        return status == PaymentStatus.SUCCESS || status == PaymentStatus.FAILED || status == PaymentStatus.DECLINED ||
                status == PaymentStatus.REJECTED || status == PaymentStatus.EXPIRED || status == PaymentStatus.REVERSED;
    }
}
