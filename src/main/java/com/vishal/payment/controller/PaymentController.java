package com.vishal.payment.controller;

import com.vishal.payment.dto.*;
import com.vishal.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/intents")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create encrypted UPI intent payment")
    public PaymentResponse createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        return paymentService.createPayment(request);
    }

    @GetMapping("/{orderNo}")
    @Operation(summary = "Get payment transaction by order number")
    public PaymentResponse getPayment(@PathVariable String orderNo) {
        return paymentService.getByOrderNo(orderNo);
    }

    @PostMapping("/callback")
    @Operation(summary = "Receive encrypted payment callback and acknowledge quickly")
    public CallbackAckResponse callback(@Valid @RequestBody CallbackRequest request, HttpServletRequest servletRequest) {
        return paymentService.handleCallback(request, clientIp(servletRequest));
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) return forwarded.split(",")[0].trim();
        return request.getRemoteAddr();
    }
}
