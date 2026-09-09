package com.pal.dipesh.razorpay.payment.service;

import com.pal.dipesh.razorpay.common.enums.PaymentEvent;
import com.pal.dipesh.razorpay.common.enums.PaymentStatus;
import com.pal.dipesh.razorpay.payment.entity.Payment;

public interface PaymentTransitionService {
    PaymentStatus apply(Payment payment, PaymentEvent paymentEvent);
}
