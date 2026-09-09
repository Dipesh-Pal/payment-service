package com.pal.dipesh.razorpay.payment.mapper;

import com.pal.dipesh.razorpay.payment.dto.response.PaymentResponse;
import com.pal.dipesh.razorpay.payment.entity.Payment;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentMapper {
    PaymentMapper INSTANCE = Mappers.getMapper(PaymentMapper.class);

    @Mapping(target = "orderId", source = "payment.orderRecord.id")
    PaymentResponse toPaymentResponse(Payment payment);

    List<PaymentResponse> toPaymentResponse(List<Payment> payments);
}