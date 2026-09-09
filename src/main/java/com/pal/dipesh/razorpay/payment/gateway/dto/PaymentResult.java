package com.pal.dipesh.razorpay.payment.gateway.dto;

public sealed interface PaymentResult permits
        PaymentResult.Success,
        PaymentResult.Failure,
        PaymentResult.Pending {

    record Success(String registrationRef, String bankReference) implements PaymentResult {
    }

    record Pending(String registrationRef) implements PaymentResult {
    }

    record Failure(String errorCode, String errorDescription) implements PaymentResult {
    }
}