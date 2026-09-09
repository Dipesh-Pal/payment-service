package com.pal.dipesh.razorpay.payment.api;

import com.pal.dipesh.razorpay.common.pojo.PaymentSettlementView;

import java.util.List;
import java.util.UUID;

public interface PaymentLookupService {
    List<PaymentSettlementView> findUnsettledCapturedPaymentsForMerchant(UUID merchantId);
    void markPaymentsAsSettled(List<UUID> paymentIds);
}
