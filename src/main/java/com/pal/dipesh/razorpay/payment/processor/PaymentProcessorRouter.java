package com.pal.dipesh.razorpay.payment.processor;

import com.pal.dipesh.razorpay.common.enums.PaymentMethod;
import com.pal.dipesh.razorpay.common.pojo.PaymentProcessorRequest;
import com.pal.dipesh.razorpay.common.pojo.PaymentProcessorResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaymentProcessorRouter {

    private final Map<PaymentMethod, PaymentProcessor> paymentProcessorMap;

    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {
        PaymentProcessor paymentProcessor = paymentProcessorMap.get(request.method());

        if (paymentProcessor == null) {
            throw new IllegalArgumentException("No payment processor found for method: " + request.method());
        }

        return paymentProcessor.charge(request);
    }
}