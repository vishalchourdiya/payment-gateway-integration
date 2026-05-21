package com.vishal.payment.domain.callback;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "callback_events", indexes = {
        @Index(name = "idx_callback_signature", columnList = "signature", unique = true),
        @Index(name = "idx_callback_order_no", columnList = "order_no")
})
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class CallbackEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "order_no", length = 35)
    private String orderNo;
    @Column(nullable = false, length = 80, unique = true)
    private String signature;
    @Column(name = "encrypted_data", nullable = false, columnDefinition = "TEXT")
    private String encryptedData;
    @Column(name = "decrypted_data", columnDefinition = "TEXT")
    private String decryptedData;
    @Column(name = "processed", nullable = false)
    private boolean processed;
    @Column(name = "failure_reason", length = 255)
    private String failureReason;
    @Column(name = "received_at")
    private Instant receivedAt;
    @Column(name = "processed_at")
    private Instant processedAt;
    @PrePersist void onCreate() { receivedAt = Instant.now(); }
}
