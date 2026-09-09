package com.pal.dipesh.razorpay.payment.client;

import com.pal.dipesh.razorpay.common.pojo.FindOrCreateCustomerRequest;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(name = "merchant-service", path = "/internal/customers")
public interface CustomerServiceClient {

    @PostMapping("/find-or-create")
    UUID findOrCreate(@RequestBody FindOrCreateCustomerRequest request);
}
