package com.pal.dipesh.razorpay.payment.saga;

import com.pal.dipesh.razorpay.common.enums.EventAggregateType;
import com.pal.dipesh.razorpay.common.enums.OrderStatus;
import com.pal.dipesh.razorpay.common.enums.PaymentEvent;
import com.pal.dipesh.razorpay.common.enums.PaymentStatus;
import com.pal.dipesh.razorpay.common.exception.BusinessRuleViolationException;
import com.pal.dipesh.razorpay.common.exception.ResourceNotFoundException;
import com.pal.dipesh.razorpay.payment.dto.request.PaymentInitiateRequest;
import com.pal.dipesh.razorpay.payment.dto.response.PaymentResponse;
import com.pal.dipesh.razorpay.payment.entity.OrderRecord;
import com.pal.dipesh.razorpay.payment.entity.Payment;
import com.pal.dipesh.razorpay.payment.gateway.dto.PaymentResult;
import com.pal.dipesh.razorpay.payment.mapper.PaymentMapper;
import com.pal.dipesh.razorpay.payment.outbox.OutboxEventPublisher;
import com.pal.dipesh.razorpay.payment.repository.OrderRepository;
import com.pal.dipesh.razorpay.payment.repository.PaymentRepository;
import com.pal.dipesh.razorpay.payment.service.PaymentTransitionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentAuthorizationRecorder {

    private final PaymentMapper paymentMapper;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final OutboxEventPublisher outboxEventPublisher;
    private final PaymentTransitionService paymentTransitionService;

    @Transactional(readOnly = true)
    public Optional<PaymentResponse> findExistingAttempt(UUID merchantId, String idempotencyKey) {
        return paymentRepository.findByMerchantIdAndIdempotencyKey(merchantId, idempotencyKey)
                .map(paymentMapper::toPaymentResponse);
    }

    @Transactional
    public Payment recordPayment(UUID merchantId, PaymentInitiateRequest request, String idempotencyKey){
        OrderRecord order = orderRepository.findByIdAndMerchantIdForUpdate(request.orderId(), merchantId)
                .orElseThrow(() -> {
                    log.warn("Order with id {} not found for merchant {}", request.orderId(), merchantId);
                    return new ResourceNotFoundException("order", request.orderId());
                });

        if(order.getOrderStatus() != OrderStatus.CREATED && order.getOrderStatus() != OrderStatus.ATTEMPTED) {
            log.warn("Order with id {} is not in a valid state for payment initiation. Current state: {}", request.orderId(), order.getOrderStatus());
            throw new BusinessRuleViolationException("ORDER_NOT_PAYABLE", "Order cannot accept payment in status: " + order.getOrderStatus());
        }

        order.setOrderStatus(OrderStatus.ATTEMPTED);
        order.setAttempts(order.getAttempts() + 1);

        Payment payment = Payment.builder()
                .orderRecord(order)
                .merchantId(merchantId)
                .amount(order.getAmount())
                .status(PaymentStatus.CREATED)
                .method(request.method())
                .idempotencyKey(idempotencyKey != null ? idempotencyKey : UUID.randomUUID().toString())
                .methodDetails(request.methodDetails())
                .build();

        payment = paymentRepository.save(payment);
        orderRepository.save(order);
        paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_ATTEMPT);

        return paymentRepository.save(payment);
    }

    @Transactional
    public PaymentResponse compensateAuthorizationFailure(UUID paymentId, String errorCode, String errorDescription) {
        Payment payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> {
                    log.warn("Payment with id {} not found for compensation", paymentId);
                    return new ResourceNotFoundException("Payment", paymentId);
                });

        paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_FAIL);

        payment.setErrorCode(errorCode);
        payment.setErrorDescription(errorDescription);
        payment = paymentRepository.save(payment);

        publishStatusChangeEvent(payment, "PAYMENT_AUTHORIZATION_COMPENSATED");

        return paymentMapper.toPaymentResponse(payment);
    }

    @Transactional
    public PaymentResponse handleGatewayResponse(UUID paymentId, PaymentResult paymentResult){
        log.debug("Started handling gateway response for paymentId: {}", paymentId);

        Payment payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> {
                    log.warn("Payment with id {} not found for applying gateway result", paymentId);
                    return new ResourceNotFoundException("Payment", paymentId);
                });

        switch (paymentResult) {
            case PaymentResult.Success _ -> log.warn("Invalid State");

            case PaymentResult.Pending(String registrationRef) -> payment.setProcessorReference(registrationRef);

            case PaymentResult.Failure(String errorCode, String errorDescription) -> {
                paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_FAIL);
                payment.setErrorCode(errorCode);
                payment.setErrorDescription(errorDescription);
            }
        }

        payment = paymentRepository.save(payment);
        publishStatusChangeEvent(payment, "PAYMENT_CREATED");

        log.debug("Finished handling gateway response for paymentId: {}", paymentId);

        return paymentMapper.toPaymentResponse(payment);
    }

    private void publishStatusChangeEvent(Payment payment, String eventType) {
        outboxEventPublisher.publish(
                EventAggregateType.PAYMENT,
                payment.getId(),
                eventType,
                Map.of(
                        "orderId", payment.getOrderRecord().getId().toString(),
                        "paymentId", payment.getId().toString(),
                        "merchantId", payment.getMerchantId().toString(),
                        "paymentStatus", payment.getStatus().name(),
                        "amountUnits", payment.getAmount().getAmountUnits(),
                        "amountCurrency", payment.getAmount().getCurrency(),
                        "paymentMethod", payment.getMethod().name()
                )
        );
    }
}
