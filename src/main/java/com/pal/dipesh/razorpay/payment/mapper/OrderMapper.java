package com.pal.dipesh.razorpay.payment.mapper;

import com.pal.dipesh.razorpay.payment.dto.response.OrderResponse;
import com.pal.dipesh.razorpay.payment.entity.OrderRecord;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderMapper {
    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

    @Mapping(target = "status", source = "order.orderStatus")
    OrderResponse toOrderResponse(OrderRecord order);
}
