package com.pal.dipesh.razorpay.payment.gateway;

import com.pal.dipesh.razorpay.common.enums.PaymentMethod;
import com.pal.dipesh.razorpay.payment.gateway.dto.PaymentRequest;
import com.pal.dipesh.razorpay.payment.gateway.dto.PaymentResult;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentGatewayRouter {

    private final Map<PaymentMethod, PaymentAdapter> paymentAdapterMap;

    public PaymentResult initiate(PaymentRequest request) {
        PaymentAdapter paymentAdapter = paymentAdapterMap.get(request.method());

        if (paymentAdapter == null) {
            throw new IllegalArgumentException("No payment adapter found for method: " + request.method());
        }

        return paymentAdapter.initiate(request);
    }

    public PaymentResult capture(PaymentMethod method, UUID paymentId) {
        PaymentAdapter paymentAdapter = paymentAdapterMap.get(method);

        if (paymentAdapter == null) {
            throw new IllegalArgumentException("No payment adapter found for method: " + method);
        }

        return paymentAdapter.capture(paymentId);
    }
}