package com.vishal.payment.dto;

public record CallbackAckResponse(String status) {
    public static CallbackAckResponse accepted() { return new CallbackAckResponse("205"); }
    public static CallbackAckResponse fraudAccepted() { return new CallbackAckResponse("206"); }
    public static CallbackAckResponse fraudRejected() { return new CallbackAckResponse("207"); }
}
