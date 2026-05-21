package com.vishal.payment.dto;

import com.vishal.payment.domain.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        String orderNo,
        String txnId,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        String qrString,
        Instant createdAt,
        Instant updatedAt
) {}
