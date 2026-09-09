package com.pal.dipesh.razorpay.payment.service;

import com.pal.dipesh.razorpay.payment.dto.request.PaymentInitiateRequest;
import com.pal.dipesh.razorpay.payment.dto.response.PaymentResponse;

import java.util.UUID;

public interface PaymentService {
    PaymentResponse initiate(UUID merchantId, PaymentInitiateRequest request);

    PaymentResponse capture(UUID merchantId, UUID paymentId);

    void resolveAuthorization(UUID paymentId, boolean approve, String bankRef, String errorCode, String errorDescription);
}