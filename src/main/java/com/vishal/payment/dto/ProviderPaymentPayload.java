package com.vishal.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProviderPaymentPayload(
        String mid,
        String enckey,
        String orderNo,
        String amount,
        String userId,
        String currency,
        String txnReqType,
        String dateOfReg,
        String respUrl,
        String customerVpa,
        String name,
        String emailId,
        String mobileNo,
        String udf1,
        String udf2,
        String udf3,
        String udf4,
        String udf5,
        String udf6,
        String udf7,
        String udf8,
        String udf9,
        String udf10,
        String udf11,
        String udf12,
        String udf13,
        String udf14
) {}
