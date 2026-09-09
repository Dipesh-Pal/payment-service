package com.pal.dipesh.razorpay.payment.gateway.adapter;

import com.pal.dipesh.razorpay.common.pojo.VaultChargeRequest;
import com.pal.dipesh.razorpay.payment.client.VaultServiceClient;
import com.pal.dipesh.razorpay.payment.gateway.PaymentAdapter;
import com.pal.dipesh.razorpay.payment.gateway.dto.PaymentRequest;
import com.pal.dipesh.razorpay.payment.gateway.dto.PaymentResult;
import com.pal.dipesh.razorpay.common.pojo.PaymentProcessorResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CardPaymentAdapter implements PaymentAdapter {

    private final VaultServiceClient vaultServiceClient;

    @Override
    public PaymentResult initiate(PaymentRequest request) {
        String token = (String) request.methodDetails().get("token");

        PaymentProcessorResponse response = vaultServiceClient.charge(new VaultChargeRequest(request.paymentId(), token, request.amount(), request.methodDetails()));

        return switch (response) {
            case PaymentProcessorResponse.Failure failure -> new PaymentResult.Failure(failure.errorCode(), failure.errorDescription());
            case PaymentProcessorResponse.Pending pending -> new PaymentResult.Pending(pending.processorReference());
            case PaymentProcessorResponse.Success success -> new PaymentResult.Success(success.processorReference(), success.bankReference());
        };
    }

    @Override
    public PaymentResult capture(UUID paymentId) {
        return new PaymentResult.Success("CARD_PAYMENT_CAPTURE_SUCCESS", "Card payment captured successfully");
    }
}