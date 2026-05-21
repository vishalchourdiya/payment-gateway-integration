package com.vishal.payment.client;

import com.vishal.payment.config.PaymentGatewayProperties;
import com.vishal.payment.dto.ProviderEncryptedRequest;
import com.vishal.payment.dto.ProviderPaymentResponse;
import com.vishal.payment.exception.PaymentGatewayException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentGatewayClient {
    private final RestTemplate restTemplate;
    private final PaymentGatewayProperties properties;

    @Retry(name = "solwioGateway")
    @CircuitBreaker(name = "solwioGateway", fallbackMethod = "fallbackCreatePayment")
    public ProviderPaymentResponse createPayment(ProviderEncryptedRequest request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            log.info("provider_payment_request mid={} url={}", request.mid(), properties.activeUrl());
            return restTemplate.postForObject(properties.activeUrl(), new HttpEntity<>(request, headers), ProviderPaymentResponse.class);
        } catch (RestClientException ex) {
            throw new PaymentGatewayException("Payment provider call failed", ex);
        }
    }

    public ProviderPaymentResponse fallbackCreatePayment(ProviderEncryptedRequest request, Throwable throwable) {
        log.error("provider_payment_fallback mid={} reason={}", request.mid(), throwable.getMessage());
        throw new PaymentGatewayException("Payment provider unavailable after retry/circuit-breaker", throwable);
    }
}
