package com.pal.dipesh.razorpay.payment.service.impl;

import com.pal.dipesh.razorpay.common.enums.EventAggregateType;
import com.pal.dipesh.razorpay.common.enums.OrderStatus;
import com.pal.dipesh.razorpay.common.exception.BusinessRuleViolationException;
import com.pal.dipesh.razorpay.common.exception.DuplicateResourceException;
import com.pal.dipesh.razorpay.common.exception.ResourceNotFoundException;
import com.pal.dipesh.razorpay.common.pojo.FindOrCreateCustomerRequest;
import com.pal.dipesh.razorpay.payment.client.CustomerServiceClient;
import com.pal.dipesh.razorpay.payment.dto.request.OrderCreateRequest;
import com.pal.dipesh.razorpay.payment.dto.response.OrderResponse;
import com.pal.dipesh.razorpay.payment.dto.response.PaymentResponse;
import com.pal.dipesh.razorpay.payment.entity.OrderRecord;
import com.pal.dipesh.razorpay.payment.entity.Payment;
import com.pal.dipesh.razorpay.payment.mapper.OrderMapper;
import com.pal.dipesh.razorpay.payment.mapper.PaymentMapper;
import com.pal.dipesh.razorpay.payment.outbox.OutboxEventPublisher;
import com.pal.dipesh.razorpay.payment.repository.OrderRepository;
import com.pal.dipesh.razorpay.payment.repository.PaymentRepository;
import com.pal.dipesh.razorpay.payment.service.OrderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OutboxEventPublisher outboxEventPublisher;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final CustomerServiceClient customerServiceClient;
    private final PaymentMapper paymentMapper;
    private final OrderMapper orderMapper;

    @Value("${payment.order.default-expiry-minutes:30}")
    private int defaultOrderExpiryMinutes;

    @Override
    @Transactional
    public OrderResponse createOrder(UUID merchantId, OrderCreateRequest request) {
        if(request.receipt() != null && orderRepository.existsByMerchantIdAndReceipt(merchantId, request.receipt())) {
            log.warn("Order with receipt {} already exists for merchant {}", request.receipt(), merchantId);
            throw new DuplicateResourceException("DUPLICATE_ORDER_RECEIPT", "Order with receipt " + request.receipt() + " already exists for merchant " + merchantId);
        }

        UUID customerId = null;

        if (request.customer() != null) {
            customerId = customerServiceClient.findOrCreate(new FindOrCreateCustomerRequest(
                    merchantId,
                    request.customer().email(),
                    request.customer().name(),
                    request.customer().phone()
            ));
        }

        OrderRecord newOrder = OrderRecord
                .builder()
                .amount(request.amount())
                .notes(request.notes())
                .customerId(customerId)
                .merchantId(merchantId)
                .receipt(request.receipt())
                .orderStatus(OrderStatus.CREATED)
                .expiresAt(request.expiresAt() != null ? request.expiresAt() : LocalDateTime.now().plusMinutes(defaultOrderExpiryMinutes))
                .build();

        newOrder = orderRepository.save(newOrder);

        outboxEventPublisher.publish(
                EventAggregateType.ORDER,
                newOrder.getId(),
                "ORDER_CREATED",
                Map.of("orderId", newOrder.getId().toString(),
                        "merchantId", newOrder.getMerchantId().toString(),
                        "orderStatus", newOrder.getOrderStatus().name(),
                        "amountUnits", newOrder.getAmount().getAmountUnits(),
                        "amountCurrency", newOrder.getAmount().getCurrency()
                )
        );

        return orderMapper.toOrderResponse(newOrder);
    }

    @Override
    public OrderResponse getOrderById(UUID merchantId, UUID orderId) {
        OrderRecord order = orderRepository.findByIdAndMerchantId(orderId, merchantId)
                .orElseThrow(() -> {
                    log.warn("Order with id {} not found for merchant {}", orderId, merchantId);
                    return new ResourceNotFoundException("order", orderId);
                });

        return orderMapper.toOrderResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(UUID merchantId, UUID orderId) {
        OrderRecord order = orderRepository.findByIdAndMerchantId(orderId, merchantId)
                .orElseThrow(() -> {
                    log.warn("Order with id {} not found for merchant {}", orderId, merchantId);
                    return new ResourceNotFoundException("order", orderId);
                });

        if(order.getOrderStatus() == OrderStatus.CANCELLED) {
            log.warn("Order {} is already cancelled", orderId);
            throw new BusinessRuleViolationException("ORDER_CANNOT_CANCEL", "Order " + orderId + " is already cancelled");
        }

        if(order.getOrderStatus() == OrderStatus.PAID) {
            log.warn("Order {} is already paid and cannot be cancelled", orderId);
            throw new BusinessRuleViolationException("ORDER_CANNOT_CANCEL", "Order " + orderId + " is already paid and cannot be cancelled");
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        order = orderRepository.save(order);

        outboxEventPublisher.publish(
                EventAggregateType.ORDER,
                order.getId(),
                "ORDER_CANCELLED",
                Map.of("orderId", order.getId().toString(),
                        "merchantId", order.getMerchantId().toString(),
                        "orderStatus", order.getOrderStatus().name(),
                        "amountUnits", order.getAmount().getAmountUnits(),
                        "amountCurrency", order.getAmount().getCurrency()
                )
        );

        return orderMapper.toOrderResponse(order);
    }

    @Override
    public List<PaymentResponse> listPayments(UUID merchantId, UUID orderId) {
        OrderRecord order = orderRepository.findByIdAndMerchantId(orderId, merchantId)
                .orElseThrow(() -> {
                    log.warn("Order with id {} not found for merchant {}", orderId, merchantId);
                    return new ResourceNotFoundException("order", orderId);
                });

        List<Payment> paymentList = paymentRepository.findByOrderRecord_Id(orderId).orElse(List.of());

        return paymentMapper.toPaymentResponse(paymentList);
    }
}