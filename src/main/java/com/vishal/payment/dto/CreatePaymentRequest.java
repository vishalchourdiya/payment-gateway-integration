package com.vishal.payment.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreatePaymentRequest(
        @NotNull @DecimalMin(value = "1.00") @Digits(integer = 11, fraction = 2) BigDecimal amount,
        @NotBlank @Size(max = 10) String currency,
        @NotBlank @Size(max = 30) String userId,
        @NotBlank @Size(max = 50) String customerVpa,
        @NotBlank @Size(max = 20) String dateOfReg,
        @NotBlank @Size(max = 10) String txnReqType,
        @NotBlank @Size(max = 100) String name,
        @Email @Size(max = 50) String emailId,
        @Pattern(regexp = "^[0-9]{10,13}$", message = "mobileNo must contain 10 to 13 digits") String mobileNo,
        @NotBlank @Size(max = 50) String udf1,
        @NotBlank @Size(max = 50) String udf2,
        @Size(max = 50) String udf3,
        @Size(max = 50) String udf4,
        @Size(max = 50) String udf5,
        @Size(max = 50) String udf6,
        @Size(max = 50) String udf7,
        @Size(max = 50) String udf8,
        @Size(max = 50) String udf9,
        @Size(max = 50) String udf10,
        @Size(max = 50) String udf11,
        @Size(max = 50) String udf12,
        @Size(max = 50) String udf13,
        @Size(max = 50) String udf14
) {}
