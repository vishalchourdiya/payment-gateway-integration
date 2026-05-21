package com.vishal.payment.dto;

import jakarta.validation.constraints.NotBlank;

public record CallbackRequest(
        @NotBlank String mid,
        @NotBlank String data,
        @NotBlank String signature
) {}
