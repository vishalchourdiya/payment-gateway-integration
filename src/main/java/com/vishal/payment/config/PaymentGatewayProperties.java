package com.vishal.payment.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "payment.gateway")
public record PaymentGatewayProperties(
        @NotBlank String mode,
        @NotBlank String mid,
        @NotBlank String encryptionKey,
        @NotBlank String uatUrl,
        @NotBlank String productionUrl,
        @NotBlank String callbackUrl,
        @Positive int connectTimeoutMs,
        @Positive int readTimeoutMs,
        @Positive int callbackReplayWindowMinutes,
        @Positive int stalePaymentMinutes
) {
    public String activeUrl() {
        return "PRODUCTION".equalsIgnoreCase(mode) ? productionUrl : uatUrl;
    }
}
