package com.vishal.payment.domain.merchant;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "merchants", indexes = @Index(name = "idx_merchant_mid", columnList = "mid", unique = true))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Merchant {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String mid;

    @Column(name = "merchant_name", nullable = false, length = 120)
    private String merchantName;

    @Column(name = "encryption_key", nullable = false, length = 64)
    private String encryptionKey;

    @Column(name = "callback_url", nullable = false, length = 150)
    private String callbackUrl;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    void onCreate() { createdAt = Instant.now(); }
}
