package com.pal.dipesh.razorpay.payment.service.impl;

import com.pal.dipesh.razorpay.common.enums.PaymentStatus;
import com.pal.dipesh.razorpay.common.pojo.PaymentSettlementView;
import com.pal.dipesh.razorpay.payment.api.PaymentLookupService;
import com.pal.dipesh.razorpay.payment.entity.Payment;
import com.pal.dipesh.razorpay.payment.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentLookupServiceImpl implements PaymentLookupService {

    private final PaymentRepository paymentRepository;

    @Override
    public List<PaymentSettlementView> findUnsettledCapturedPaymentsForMerchant(UUID merchantId) {
        List<Payment> paymentList = paymentRepository.findByMerchantIdAndStatusForUpdate(merchantId, PaymentStatus.CAPTURED);

        return paymentList.stream()
                .map(payment -> new PaymentSettlementView(
                        payment.getId(),
                        payment.getAmount().getAmountUnits(),
                        0, // TODO: Replace with actual refund amount from RefundRepository when implemented
                        payment.getAmount().getCurrency())
                )
                .toList();
    }

    @Override
    @Transactional
    public void markPaymentsAsSettled(List<UUID> paymentIds) {
        LocalDateTime now = LocalDateTime.now();
        List<Payment> paymentsToUpdate = paymentRepository.findAllById(paymentIds);

        for (Payment payment : paymentsToUpdate) {
            payment.setStatus(PaymentStatus.SETTLED);
            payment.setSettledAt(now);
        }

        paymentRepository.saveAll(paymentsToUpdate);
    }
}
