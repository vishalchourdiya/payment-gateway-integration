package com.vishal.payment.service;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class OrderNumberGenerator {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyMMddHHmmssSSS");

    public String generate() {
        int random = ThreadLocalRandom.current().nextInt(100, 999);
        return "ORD" + LocalDateTime.now().format(FORMATTER) + random;
    }
}
