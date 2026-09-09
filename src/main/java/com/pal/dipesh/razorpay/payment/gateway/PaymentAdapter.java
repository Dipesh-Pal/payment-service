package com.pal.dipesh.razorpay.payment.gateway;

import com.pal.dipesh.razorpay.payment.gateway.dto.PaymentRequest;
import com.pal.dipesh.razorpay.payment.gateway.dto.PaymentResult;

import java.util.UUID;

public interface PaymentAdapter {
    PaymentResult initiate(PaymentRequest request);

    PaymentResult capture(UUID paymentId);
}