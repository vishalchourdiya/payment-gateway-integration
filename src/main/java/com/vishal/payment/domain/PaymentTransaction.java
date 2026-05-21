package com.vishal.payment.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payment_transactions", indexes = {
        @Index(name = "idx_payment_order_no", columnList = "order_no", unique = true),
        @Index(name = "idx_payment_txn_id", columnList = "txn_id", unique = true),
        @Index(name = "idx_payment_status", columnList = "status"),
        @Index(name = "idx_payment_created_at", columnList = "created_at")
})
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class PaymentTransaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(nullable = false, length = 50)
    private String mid;

    @Column(name = "order_no", nullable = false, unique = true, length = 35)
    private String orderNo;

    @Column(nullable = false, precision = 13, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 10)
    private String currency;

    @Column(name = "user_id", length = 30)
    private String userId;

    @Column(name = "customer_vpa", length = 50)
    private String customerVpa;

    @Column(name = "mobile_no", length = 13)
    private String mobileNo;

    @Column(name = "email_id", length = 50)
    private String emailId;

    @Column(name = "customer_name", length = 100)
    private String customerName;

    @Column(name = "txn_id", unique = true, length = 40)
    private String txnId;

    @Column(name = "cust_ref_no", length = 40)
    private String custRefNo;

    @Column(name = "txn_resp_code", length = 20)
    private String txnRespCode;

    @Column(name = "provider_txn_status", length = 30)
    private String providerTxnStatus;

    @Column(name = "qr_string", columnDefinition = "TEXT")
    private String qrString;

    @Column(name = "provider_raw_response", columnDefinition = "TEXT")
    private String providerRawResponse;

    @Column(name = "last_callback_payload", columnDefinition = "TEXT")
    private String lastCallbackPayload;

    @Column(name = "last_callback_signature", length = 80)
    private String lastCallbackSignature;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PaymentStatus status;

    @Column(name = "created_at")
    private Instant createdAt;
    @Column(name = "updated_at")
    private Instant updatedAt;
    @Column(name = "completed_at")
    private Instant completedAt;

    public boolean isTerminal() {
        return status == PaymentStatus.SUCCESS || status == PaymentStatus.FAILED || status == PaymentStatus.DECLINED ||
                status == PaymentStatus.REJECTED || status == PaymentStatus.EXPIRED || status == PaymentStatus.REVERSED;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
