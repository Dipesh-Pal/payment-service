package com.pal.dipesh.razorpay.payment.client;

import com.pal.dipesh.razorpay.common.pojo.PaymentProcessorResponse;
import com.pal.dipesh.razorpay.common.pojo.VaultChargeRequest;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "vault-service", path = "/internal/vault")
public interface VaultServiceClient {

    @PostMapping("/charge")
    PaymentProcessorResponse charge(@RequestBody VaultChargeRequest request);
}
