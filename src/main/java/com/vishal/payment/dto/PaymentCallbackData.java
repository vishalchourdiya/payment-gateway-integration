package com.vishal.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PaymentCallbackData(
        String txnId,
        String amount,
        String mid,
        String currency,
        String userVpa,
        String orderNo,
        String paymentType,
        String txnDate,
        String txnRespCode,
        String txnStatus,
        String custRefNo,
        String udf1,
        String udf2,
        String udf3,
        String udf4,
        String respUrl
) {}
