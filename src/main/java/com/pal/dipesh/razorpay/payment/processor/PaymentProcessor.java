package com.pal.dipesh.razorpay.payment.processor;

import com.pal.dipesh.razorpay.common.pojo.PaymentProcessorRequest;
import com.pal.dipesh.razorpay.common.pojo.PaymentProcessorResponse;

public interface PaymentProcessor {
    PaymentProcessorResponse charge(PaymentProcessorRequest request);
}
