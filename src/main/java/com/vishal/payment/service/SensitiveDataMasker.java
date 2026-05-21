package com.vishal.payment.service;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class SensitiveDataMasker {
    private static final Pattern EMAIL = Pattern.compile("([a-zA-Z0-9._%+-]{2})[a-zA-Z0-9._%+-]*(@[a-zA-Z0-9.-]+)");
    private static final Pattern MOBILE = Pattern.compile("(?<!\\d)(\\d{2})\\d{6,9}(\\d{2})(?!\\d)");
    private static final Pattern VPA = Pattern.compile("([a-zA-Z0-9._-]{2})[a-zA-Z0-9._-]*(@[a-zA-Z][a-zA-Z0-9._-]*)");
    private static final Pattern ENCRYPTION_KEY_JSON = Pattern.compile("(\\\"(?:enckey|encryptionKey|encryption-key)\\\"\\s*:\\s*\\\")[^\\\"]+(\\\")", Pattern.CASE_INSENSITIVE);

    public String mask(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        String masked = ENCRYPTION_KEY_JSON.matcher(value).replaceAll("$1****$2");
        masked = EMAIL.matcher(masked).replaceAll("$1****$2");
        masked = VPA.matcher(masked).replaceAll("$1****$2");
        masked = MOBILE.matcher(masked).replaceAll("$1******$2");
        return masked;
    }
}
