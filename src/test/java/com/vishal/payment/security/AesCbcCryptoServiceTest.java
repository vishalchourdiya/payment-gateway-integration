package com.vishal.payment.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AesCbcCryptoServiceTest {
    private final AesCbcCryptoService service = new AesCbcCryptoService();

    @Test
    void shouldEncryptAndDecryptUsingDocumentedAesCbcFormat() {
        String key = "0123456789abcdef0123456789abcdef";
        String json = "{\"mid\":\"DEMO_MERCHANT_001\",\"orderNo\":\"ORD123\"}";

        String encrypted = service.encrypt(json, key);
        String decrypted = service.decrypt(encrypted, key);

        assertThat(encrypted).isNotBlank().isNotEqualTo(json);
        assertThat(decrypted).isEqualTo(json);
    }
}
