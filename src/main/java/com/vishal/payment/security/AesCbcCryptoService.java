package com.vishal.payment.security;

import com.vishal.payment.exception.PaymentGatewayException;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Service
public class AesCbcCryptoService {
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String ALGORITHM = "AES";
    private static final int IV_LENGTH = 16;
    private final SecureRandom secureRandom = new SecureRandom();

    public String encrypt(String plaintext, String encryptKey) {
        try {
            validateKey(encryptKey);
            byte[] ivBytes = new byte[IV_LENGTH];
            secureRandom.nextBytes(ivBytes);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key(encryptKey), new IvParameterSpec(ivBytes));
            byte[] encryptedData = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[ivBytes.length + encryptedData.length];
            System.arraycopy(ivBytes, 0, combined, 0, ivBytes.length);
            System.arraycopy(encryptedData, 0, combined, ivBytes.length, encryptedData.length);
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception ex) {
            throw new PaymentGatewayException("Unable to encrypt payment payload", ex);
        }
    }

    public String decrypt(String encryptedString, String encryptKey) {
        try {
            validateKey(encryptKey);
            byte[] encryptedData = Base64.getDecoder().decode(encryptedString);
            if (encryptedData.length <= IV_LENGTH) {
                throw new IllegalArgumentException("Encrypted payload is too short");
            }
            byte[] ivBytes = Arrays.copyOfRange(encryptedData, 0, IV_LENGTH);
            byte[] encryptedBytes = Arrays.copyOfRange(encryptedData, IV_LENGTH, encryptedData.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key(encryptKey), new IvParameterSpec(ivBytes));
            return new String(cipher.doFinal(encryptedBytes), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new PaymentGatewayException("Unable to decrypt payment callback", ex);
        }
    }

    private SecretKeySpec key(String encryptKey) {
        return new SecretKeySpec(encryptKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
    }

    private void validateKey(String encryptKey) {
        int length = encryptKey == null ? 0 : encryptKey.getBytes(StandardCharsets.UTF_8).length;
        if (length != 16 && length != 24 && length != 32) {
            throw new IllegalArgumentException("AES key must be 16, 24, or 32 bytes. Documentation UAT key is 32 bytes.");
        }
    }
}
