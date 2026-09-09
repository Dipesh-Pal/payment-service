package com.pal.dipesh.razorpay.payment.service.statemachine;

import com.pal.dipesh.razorpay.common.enums.PaymentEvent;
import com.pal.dipesh.razorpay.common.enums.PaymentStatus;
import com.pal.dipesh.razorpay.common.exception.InvalidStateTransitionException;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PaymentStateMachine {

    private record Transition(PaymentStatus from, PaymentEvent transitionEvent) { }

    private static final Map<Transition, PaymentStatus> STATE_TRANSITIONS = Map.ofEntries(
            Map.entry(new Transition(PaymentStatus.CREATED, PaymentEvent.AUTHORIZE_ATTEMPT), PaymentStatus.AUTHORIZING),
            Map.entry(new Transition(PaymentStatus.AUTHORIZING, PaymentEvent.AUTHORIZE_SUCCESS), PaymentStatus.AUTHORIZED),
            Map.entry(new Transition(PaymentStatus.AUTHORIZING, PaymentEvent.AUTHORIZE_FAIL), PaymentStatus.FAILED),
            Map.entry(new Transition(PaymentStatus.AUTHORIZED, PaymentEvent.CAPTURE_REQUEST), PaymentStatus.CAPTURING),
            Map.entry(new Transition(PaymentStatus.CAPTURING, PaymentEvent.CAPTURE_SUCCESS), PaymentStatus.CAPTURED),
            Map.entry(new Transition(PaymentStatus.CAPTURING, PaymentEvent.CAPTURE_FAIL), PaymentStatus.AUTHORIZED),
            Map.entry(new Transition(PaymentStatus.CAPTURED, PaymentEvent.REFUND_INIT), PaymentStatus.PARTIALLY_REFUNDED),
            Map.entry(new Transition(PaymentStatus.PARTIALLY_REFUNDED, PaymentEvent.REFUND_COMPLETE), PaymentStatus.REFUNDED),
            Map.entry(new Transition(PaymentStatus.CAPTURED, PaymentEvent.REFUND_COMPLETE), PaymentStatus.REFUNDED),
            Map.entry(new Transition(PaymentStatus.CAPTURED, PaymentEvent.SETTLE), PaymentStatus.SETTLED),
            Map.entry(new Transition(PaymentStatus.SETTLED, PaymentEvent.REFUND_INIT), PaymentStatus.PARTIALLY_REFUNDED),
            Map.entry(new Transition(PaymentStatus.SETTLED, PaymentEvent.REFUND_COMPLETE), PaymentStatus.REFUNDED),
            Map.entry(new Transition(PaymentStatus.CREATED, PaymentEvent.CANCEL), PaymentStatus.CANCELED),
            Map.entry(new Transition(PaymentStatus.AUTHORIZING, PaymentEvent.CANCEL), PaymentStatus.CANCELED),
            Map.entry(new Transition(PaymentStatus.AUTHORIZED, PaymentEvent.CAPTURE_TIMEOUT), PaymentStatus.AUTH_EXPIRED)
    );

    public PaymentStatus getNextState(PaymentStatus currentState, PaymentEvent event) {
        Transition transition = new Transition(currentState, event);

        PaymentStatus nextStatus = STATE_TRANSITIONS.get(transition);

        if (nextStatus == null) {
            throw new InvalidStateTransitionException(currentState.name(), event.name());
        }

        return nextStatus;
    }
}
