package com.vishal.payment.service;

import com.vishal.payment.domain.PaymentStatus;
import com.vishal.payment.domain.audit.TransactionAudit;
import com.vishal.payment.repository.TransactionAuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditService {
    private final TransactionAuditRepository repository;
    private final SensitiveDataMasker sensitiveDataMasker;

    public void record(String orderNo, PaymentStatus oldStatus, PaymentStatus newStatus, String eventType, String rawPayload, String sourceIp) {
        repository.save(TransactionAudit.builder()
                .orderNo(orderNo)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .eventType(eventType)
                .rawPayload(sensitiveDataMasker.mask(rawPayload))
                .sourceIp(sourceIp)
                .build());
    }
}
