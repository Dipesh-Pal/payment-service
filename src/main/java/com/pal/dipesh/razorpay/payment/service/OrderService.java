package com.pal.dipesh.razorpay.payment.service;

import com.pal.dipesh.razorpay.payment.dto.request.OrderCreateRequest;
import com.pal.dipesh.razorpay.payment.dto.response.OrderResponse;
import com.pal.dipesh.razorpay.payment.dto.response.PaymentResponse;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    OrderResponse createOrder(UUID merchantId, OrderCreateRequest order);

    OrderResponse getOrderById(UUID merchantId, UUID orderId);

    OrderResponse cancelOrder(UUID merchantId, UUID orderId);

    List<PaymentResponse> listPayments(UUID merchantId, UUID orderId);
}