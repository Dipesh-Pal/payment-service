package com.pal.dipesh.razorpay.payment.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pal.dipesh.razorpay.common.enums.PaymentMethod;
import com.pal.dipesh.razorpay.common.enums.PaymentStatus;
import com.pal.dipesh.razorpay.common.pojo.Money;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PaymentResponse(
        UUID id,
        UUID orderId,
        UUID merchantId,
        Money amount,
        PaymentStatus status,
        PaymentMethod method,
        Map<String, Object> methodDetails,
        String bankReference,
        String errorCode,
        String errorDescription,
        LocalDateTime capturedAt,
        LocalDateTime createdAt
) {
}