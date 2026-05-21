package com.vishal.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProviderPaymentResponse(
        String message,
        String statusCode,
        ProviderResponseData responseData
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ProviderResponseData(
            String amount,
            String orderNo,
            String created_on,
            String qrString,
            String mid,
            String txnId,
            String userId,
            String userVpa,
            String dateOfReg,
            String customerVpa
    ) {}
}
