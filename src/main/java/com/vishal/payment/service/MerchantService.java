package com.vishal.payment.service;

import com.vishal.payment.config.PaymentGatewayProperties;
import com.vishal.payment.domain.merchant.Merchant;
import com.vishal.payment.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MerchantService {
    private final MerchantRepository repository;
    private final PaymentGatewayProperties properties;

    @Cacheable(value = "merchantByMid", key = "#mid")
    @Transactional(readOnly = true)
    public Merchant getActiveMerchant(String mid) {
        return repository.findByMidAndActiveTrue(mid).orElseGet(() -> Merchant.builder()
                .mid(properties.mid())
                .merchantName("Default UAT Merchant")
                .encryptionKey(properties.encryptionKey())
                .callbackUrl(properties.callbackUrl())
                .active(true)
                .build());
    }
}
