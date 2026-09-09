package com.pal.dipesh.razorpay.payment.service.impl;

import com.pal.dipesh.razorpay.common.context.CustomRequestContext;
import com.pal.dipesh.razorpay.common.enums.PaymentActor;
import com.pal.dipesh.razorpay.common.enums.PaymentEvent;
import com.pal.dipesh.razorpay.common.enums.PaymentStatus;
import com.pal.dipesh.razorpay.payment.entity.Payment;
import com.pal.dipesh.razorpay.payment.entity.PaymentTransitionLog;
import com.pal.dipesh.razorpay.payment.repository.PaymentTransitionLogRepository;
import com.pal.dipesh.razorpay.payment.service.PaymentTransitionService;
import com.pal.dipesh.razorpay.payment.service.statemachine.PaymentStateMachine;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentTransitionServiceImpl implements PaymentTransitionService {

    private final PaymentTransitionLogRepository paymentTransitionLogRepository;
    private final CustomRequestContext customRequestContext;
    private final PaymentStateMachine paymentStateMachine;

    @Override
    @Transactional
    public PaymentStatus apply(Payment payment, PaymentEvent paymentEvent) {
        PaymentStatus nextState = paymentStateMachine.getNextState(payment.getStatus(), paymentEvent);

        PaymentTransitionLog log = PaymentTransitionLog.builder()
                .payment(payment)
                .fromStatus(payment.getStatus())
                .eventType(paymentEvent)
                .toStatus(nextState)
                .actor(getPaymentActor())
                .occurredAt(LocalDateTime.now())
                .build();

        payment.setStatus(nextState);

        paymentTransitionLogRepository.save(log);

        return nextState;
    }

    private PaymentActor getPaymentActor() {
        String keyId = customRequestContext.getKeyId();
        UUID merchantId = customRequestContext.getMerchantId();

        if (keyId != null) {
            return PaymentActor.CUSTOMER;
        } else if (merchantId != null) {
            return PaymentActor.MERCHANT;
        } else {
            return PaymentActor.SYSTEM;
        }
    }
}
