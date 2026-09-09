package com.pal.dipesh.razorpay.payment.controller;

import com.pal.dipesh.razorpay.common.annotation.ResponseMessage;
import com.pal.dipesh.razorpay.common.context.CustomRequestContext;
import com.pal.dipesh.razorpay.payment.dto.request.OrderCreateRequest;
import com.pal.dipesh.razorpay.payment.dto.response.OrderResponse;
import com.pal.dipesh.razorpay.payment.service.OrderService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final CustomRequestContext customRequestContext;
    private final OrderService orderService;

    @PostMapping
    @ResponseMessage("Order created")
    public ResponseEntity<OrderResponse> createOrder(@RequestBody @Valid OrderCreateRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orderService.createOrder(customRequestContext.getMerchantId(), request));
    }

    @GetMapping("/{orderId}")
    @ResponseMessage("Order Retrieved")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable String orderId) {
        return ResponseEntity.ok(orderService.getOrderById(customRequestContext.getMerchantId(), UUID.fromString(orderId)));
    }
}