package com.vishal.payment.security;

import com.vishal.payment.dto.PaymentCallbackData;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;

@Service
public class ChecksumService {
    public String generateCallbackSignature(PaymentCallbackData data) {
        String raw = nullSafe(data.mid()) + nullSafe(data.txnId()) + nullSafe(data.orderNo()) + nullSafe(data.txnStatus());
        CRC32 crc32 = new CRC32();
        crc32.update(raw.getBytes(StandardCharsets.UTF_8));
        return String.valueOf(crc32.getValue());
    }

    public boolean isValidCallbackSignature(PaymentCallbackData data, String receivedSignature) {
        return generateCallbackSignature(data).equals(receivedSignature);
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
