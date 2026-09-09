package com.pal.dipesh.razorpay.payment.gateway.adapter;

import com.pal.dipesh.razorpay.payment.gateway.PaymentAdapter;
import com.pal.dipesh.razorpay.payment.gateway.dto.PaymentRequest;
import com.pal.dipesh.razorpay.payment.gateway.dto.PaymentResult;
import com.pal.dipesh.razorpay.payment.processor.PaymentProcessorRouter;
import com.pal.dipesh.razorpay.common.pojo.PaymentProcessorRequest;
import com.pal.dipesh.razorpay.common.pojo.PaymentProcessorResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NetBankingAdapter implements PaymentAdapter {

    private final PaymentProcessorRouter paymentProcessorRouter;

    @Override
    public PaymentResult initiate(PaymentRequest request) {
        log.info("Initiating payment with NetBankingAdapter, paymentId: {}", request.paymentId());

        try {
            PaymentProcessorRequest processorRequest = PaymentProcessorRequest.nonCard(
                    request.paymentId(),
                    request.method(),
                    request.amount(),
                    request.methodDetails()
            );

            PaymentProcessorResponse paymentProcessorResponse = paymentProcessorRouter.charge(processorRequest);

            return switch (paymentProcessorResponse) {
                case PaymentProcessorResponse.Success success -> new PaymentResult.Success(success.processorReference(), success.bankReference());
                case PaymentProcessorResponse.Pending pending -> new PaymentResult.Pending(pending.processorReference());
                case PaymentProcessorResponse.Failure failure -> new PaymentResult.Failure(failure.errorCode(), failure.errorDescription());
            };
        } catch (Exception e) {
            log.warn("Failed to payment with NetBankingAdapter, paymentId: {}", request.paymentId(), e);
            return new PaymentResult.Failure("NETBANKING_ADAPTER_ERROR", e.getMessage());
        }
    }

    @Override
    public PaymentResult capture(UUID paymentId) {
        return new PaymentResult.Success("NETBANKING_CAPTURE_SUCCESS", "NetBanking payment captured successfully");
    }
}
