package com.pal.dipesh.razorpay.payment.service.impl;

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
import com.pal.dipesh.razorpay.payment.gateway.PaymentGatewayRouter;
import com.pal.dipesh.razorpay.payment.gateway.dto.PaymentRequest;
import com.pal.dipesh.razorpay.payment.gateway.dto.PaymentResult;
import com.pal.dipesh.razorpay.payment.mapper.PaymentMapper;
import com.pal.dipesh.razorpay.payment.outbox.OutboxEventPublisher;
import com.pal.dipesh.razorpay.payment.repository.OrderRepository;
import com.pal.dipesh.razorpay.payment.repository.PaymentRepository;
import com.pal.dipesh.razorpay.payment.service.PaymentService;
import com.pal.dipesh.razorpay.payment.service.PaymentTransitionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentServiceImpl implements PaymentService {

    private final PaymentTransitionService paymentTransitionService;
    private final PaymentGatewayRouter paymentGatewayRouter;
    private final OutboxEventPublisher outboxEventPublisher;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentMapper paymentMapper;

    @Override
    @Transactional
    public PaymentResponse initiate(UUID merchantId, PaymentInitiateRequest request) {
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
                .idempotencyKey(UUID.randomUUID().toString()) // TODO: Handle Idempotency
                .methodDetails(request.methodDetails())
                .build();

        payment = paymentRepository.save(payment);

        PaymentRequest paymentRequest = new PaymentRequest(
                payment.getId(),
                order.getId(),
                merchantId,
                order.getAmount(),
                request.method(),
                request.methodDetails()
        );

        paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_ATTEMPT);
        PaymentResult paymentResult = paymentGatewayRouter.routePayment(paymentRequest);

        switch (paymentResult) {
            case PaymentResult.Success success -> {
                log.warn("Invalid State");
                return null;
            }

            case PaymentResult.Pending pending -> payment.setProcessorReference(pending.registrationRef());

            case PaymentResult.Failure failure -> {
                paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_FAIL);
                payment.setErrorCode(failure.errorCode());
                payment.setErrorDescription(failure.errorDescription());
            }
        }

        payment = paymentRepository.save(payment);
        orderRepository.save(order);

        outboxEventPublisher.publish(
                EventAggregateType.PAYMENT,
                payment.getId(),
                "PAYMENT_CREATED",
                Map.of("orderId", payment.getOrderRecord().getId().toString(),
                        "paymentId", payment.getId().toString(),
                        "merchantId", payment.getMerchantId().toString(),
                        "paymentStatus", payment.getStatus().name(),
                        "amountUnits", payment.getAmount().getAmountUnits(),
                        "amountCurrency", payment.getAmount().getCurrency(),
                        "paymentMethod", payment.getMethod().name()
                )
        );

        return paymentMapper.toPaymentResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse capture(UUID merchantId, UUID paymentId) {
        Payment payment = paymentRepository.findByIdAndMerchantIdForUpdate(paymentId, merchantId)
                .orElseThrow(() -> {
                    log.warn("Payment with id {} not found for merchant {}", paymentId, merchantId);
                    return new ResourceNotFoundException("Payment", paymentId);
                });

        OrderRecord orderRecord = payment.getOrderRecord();

        paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_REQUEST);

        PaymentResult captureResult = paymentGatewayRouter.capture(payment.getMethod(), paymentId);

        if(captureResult instanceof PaymentResult.Success) {
            paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_SUCCESS);
            payment.setCapturedAt(LocalDateTime.now());
            orderRecord.setOrderStatus(OrderStatus.PAID);

            log.info("Payment with id {} has been captured", paymentId);
        } else if(captureResult instanceof PaymentResult.Failure(String code, String description)) {
            paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_FAIL);
            payment.setErrorCode(code);
            payment.setErrorDescription(description);
        }

        payment = paymentRepository.save(payment);

        outboxEventPublisher.publish(
                EventAggregateType.PAYMENT,
                payment.getId(),
                "PAYMENT_STATUS_UPDATED",
                Map.of("orderId", payment.getOrderRecord().getId().toString(),
                        "paymentId", payment.getId().toString(),
                        "merchantId", payment.getMerchantId().toString(),
                        "paymentStatus", payment.getStatus().name(),
                        "amountUnits", payment.getAmount().getAmountUnits(),
                        "amountCurrency", payment.getAmount().getCurrency(),
                        "paymentMethod", payment.getMethod().name()
                )
        );

        return paymentMapper.toPaymentResponse(payment);
    }

    @Override
    @Transactional
    public void resolveAuthorization(UUID paymentId, boolean approve, String bankRef, String errorCode, String errorDescription) {
        Payment payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> {
                    log.warn("Payment with id {} not found", paymentId);
                    return new ResourceNotFoundException("Payment", paymentId);
                });

        if(payment.getStatus() != PaymentStatus.AUTHORIZING){
            log.warn("Payment is not in Authorizing state, paymentID: {}, status: {}", paymentId, payment.getStatus());
            return;
        }

        OrderRecord orderRecord = payment.getOrderRecord();

        if(approve){
            paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_SUCCESS);
            payment.setBankReference(bankRef);
            payment.setAuthorizedAt(LocalDateTime.now());

            // Auto-Capture
            paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_REQUEST);
            PaymentResult captureResult = paymentGatewayRouter.capture(payment.getMethod(), paymentId);

            if(captureResult instanceof PaymentResult.Success) {
                paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_SUCCESS);
                payment.setCapturedAt(LocalDateTime.now());
                orderRecord.setOrderStatus(OrderStatus.PAID);

                log.info("Payment with id {} has been captured", paymentId);
            } else if(captureResult instanceof PaymentResult.Failure(String code, String description)) {
                paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_FAIL);
                payment.setErrorCode(code);
                payment.setErrorDescription(description);
            }
        } else {
            paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_FAIL);
            payment.setErrorCode(errorCode);
            payment.setErrorDescription(errorDescription);
        }

        paymentRepository.save(payment);
        orderRepository.save(orderRecord);

        outboxEventPublisher.publish(
                EventAggregateType.PAYMENT,
                payment.getId(),
                "PAYMENT_STATUS_UPDATED",
                Map.of("orderId", payment.getOrderRecord().getId().toString(),
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