package com.pal.dipesh.razorpay.payment.controller;

import com.pal.dipesh.razorpay.common.annotation.ResponseMessage;
import com.pal.dipesh.razorpay.common.context.CustomRequestContext;
import com.pal.dipesh.razorpay.payment.dto.request.PaymentInitiateRequest;
import com.pal.dipesh.razorpay.payment.dto.response.PaymentResponse;
import com.pal.dipesh.razorpay.payment.service.PaymentService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final CustomRequestContext customRequestContext;
    private final PaymentService paymentService;

    @PostMapping
    @ResponseMessage("Payment initiated")
    public ResponseEntity<PaymentResponse> initiate(@RequestBody @Valid PaymentInitiateRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(paymentService.initiate(customRequestContext.getMerchantId(), request));
    }

    @PostMapping("/{paymentId}/capture")
    @ResponseMessage("Payment captured")
    public ResponseEntity<PaymentResponse> capture(@PathVariable UUID paymentId) {
        return ResponseEntity.ok(paymentService.capture(customRequestContext.getMerchantId(), paymentId));
    }
}