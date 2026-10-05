package com.pal.dipesh.razorpay.payment.gateway.adapter;

import com.pal.dipesh.razorpay.common.pojo.VaultChargeRequest;
import com.pal.dipesh.razorpay.payment.client.VaultServiceClient;
import com.pal.dipesh.razorpay.payment.gateway.PaymentAdapter;
import com.pal.dipesh.razorpay.payment.gateway.dto.PaymentRequest;
import com.pal.dipesh.razorpay.payment.gateway.dto.PaymentResult;
import com.pal.dipesh.razorpay.common.pojo.PaymentProcessorResponse;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CardPaymentAdapter implements PaymentAdapter {

    private final VaultServiceClient vaultServiceClient;

    @Override
    @CircuitBreaker(name = "vault-service")
    @Retry(name = "vault-service")
    public PaymentResult initiate(PaymentRequest request) {
        // TODO: Validate the request.methodDetails() to ensure it contains the required fields for card payment
        String token = (String) request.methodDetails().get("token");

        PaymentProcessorResponse response = vaultServiceClient.charge(new VaultChargeRequest(request.paymentId(), token, request.amount(), request.methodDetails()));

        return switch (response) {
            case PaymentProcessorResponse.Failure(var errorCode, var errorDescription) -> new PaymentResult.Failure(errorCode, errorDescription);
            case PaymentProcessorResponse.Pending(var processorReference) -> new PaymentResult.Pending(processorReference);
            case PaymentProcessorResponse.Success(var processorReference, var bankReference) -> new PaymentResult.Success(processorReference, bankReference);
        };
    }

    @Override
    public PaymentResult capture(UUID paymentId) {
        return new PaymentResult.Success("CARD_PAYMENT_CAPTURE_SUCCESS", "Card payment captured successfully");
    }
}