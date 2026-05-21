package com.vishal.payment;

import com.vishal.payment.config.PaymentGatewayProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties(PaymentGatewayProperties.class)
@EnableScheduling
@EnableAsync
@EnableCaching
public class SolwioPaymentGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(SolwioPaymentGatewayApplication.class, args);
    }
}
