package com.vishal.payment.security;

import com.vishal.payment.dto.PaymentCallbackData;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChecksumServiceTest {
    private final ChecksumService checksumService = new ChecksumService();

    @Test
    void shouldGenerateAndVerifyCrc32SignatureInDocumentedSequence() {
        PaymentCallbackData data = new PaymentCallbackData(
                "TXN123456789", "150.00", "DEMO_MERCHANT_001", "INR", "customer@upi",
                "ORD987654321", "QR", "2025-10-14 15:48:26.540", "200", "success",
                "CUST123456789", "Sample Data 1", "Sample Data 2", "UPI", "QR",
                "https://merchant-callback.example.com/pay/callback"
        );

        String signature = checksumService.generateCallbackSignature(data);

        assertThat(signature).containsOnlyDigits();
        assertThat(checksumService.isValidCallbackSignature(data, signature)).isTrue();
        assertThat(checksumService.isValidCallbackSignature(data, "wrong")).isFalse();
    }
}
