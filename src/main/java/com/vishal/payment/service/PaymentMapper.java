package com.vishal.payment.service;

import com.vishal.payment.domain.PaymentTransaction;
import com.vishal.payment.domain.merchant.Merchant;
import com.vishal.payment.dto.*;
import org.springframework.stereotype.Component;
import java.math.RoundingMode;

@Component
public class PaymentMapper {
    public ProviderPaymentPayload toProviderPayload(CreatePaymentRequest request, String orderNo, Merchant merchant) {
        return new ProviderPaymentPayload(
                merchant.getMid(),
                merchant.getEncryptionKey(),
                orderNo,
                request.amount().setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString(),
                request.userId(),
                request.currency(),
                request.txnReqType(),
                request.dateOfReg(),
                merchant.getCallbackUrl(),
                request.customerVpa(),
                request.name(),
                request.emailId(),
                request.mobileNo(),
                request.udf1(), request.udf2(), empty(request.udf3()), empty(request.udf4()), empty(request.udf5()),
                empty(request.udf6()), empty(request.udf7()), empty(request.udf8()), empty(request.udf9()), empty(request.udf10()),
                empty(request.udf11()), empty(request.udf12()), empty(request.udf13()), empty(request.udf14())
        );
    }

    public PaymentResponse toResponse(PaymentTransaction tx) {
        return new PaymentResponse(tx.getOrderNo(), tx.getTxnId(), tx.getAmount(), tx.getCurrency(),
                tx.getStatus(), tx.getQrString(), tx.getCreatedAt(), tx.getUpdatedAt());
    }

    private String empty(String value) { return value == null ? "" : value; }
}
