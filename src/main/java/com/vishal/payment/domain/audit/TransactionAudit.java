package com.vishal.payment.domain.audit;

import com.vishal.payment.domain.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "transaction_audits", indexes = {
        @Index(name = "idx_audit_order_no", columnList = "order_no"),
        @Index(name = "idx_audit_created_at", columnList = "created_at")
})
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class TransactionAudit {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "order_no", nullable = false, length = 35)
    private String orderNo;
    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", length = 40)
    private PaymentStatus oldStatus;
    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 40)
    private PaymentStatus newStatus;
    @Column(name = "event_type", nullable = false, length = 60)
    private String eventType;
    @Column(name = "source_ip", length = 45)
    private String sourceIp;
    @Column(name = "raw_payload", columnDefinition = "TEXT")
    private String rawPayload;
    @Column(name = "created_at")
    private Instant createdAt;
    @PrePersist void onCreate() { createdAt = Instant.now(); }
}
