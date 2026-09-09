package com.pal.dipesh.razorpay.payment.controller;

import com.pal.dipesh.razorpay.common.pojo.PaymentSettlementView;
import com.pal.dipesh.razorpay.payment.api.PaymentLookupService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/payments")
public class internalSettlementController {

    private final PaymentLookupService paymentLookupService;

    @GetMapping("/unsettled-captured")
    ResponseEntity<List<PaymentSettlementView>> findUnsettledCaptured(@RequestParam UUID merchantId){
        return ResponseEntity.ok(paymentLookupService.findUnsettledCapturedPaymentsForMerchant(merchantId));
    }

    @PostMapping("/mark-settled")
    ResponseEntity<Void> markSettled(@RequestBody List<UUID> paymentIds){
        paymentLookupService.markPaymentsAsSettled(paymentIds);
        return ResponseEntity.ok().build();
    }
}
