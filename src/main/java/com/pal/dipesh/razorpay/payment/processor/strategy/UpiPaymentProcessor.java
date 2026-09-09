package com.pal.dipesh.razorpay.payment.processor.strategy;

import com.pal.dipesh.razorpay.common.util.RandomizerUtil;
import com.pal.dipesh.razorpay.payment.processor.PaymentProcessor;
import com.pal.dipesh.razorpay.common.pojo.PaymentProcessorRequest;
import com.pal.dipesh.razorpay.common.pojo.PaymentProcessorResponse;

import org.springframework.stereotype.Component;

@Component
public class UpiPaymentProcessor implements PaymentProcessor {

    @Override
    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {
        // Call Third Party
        final String VPA_CODE_FAIL = "fail@okaxis";

        String bankCode = request.methodDetails() != null ? request.methodDetails().get("vpa").toString() : null;

        //simulation
        if (bankCode == null || bankCode.isEmpty() || bankCode.equalsIgnoreCase(VPA_CODE_FAIL)) {
            return new PaymentProcessorResponse.Failure("UPI_PAYMENT_FAILED", "Bank rejected the transaction registration");
        }

        String processorRef = "UPI_PAYMENT_PROCESSOR_" + RandomizerUtil.randomBase64(16);

        return new PaymentProcessorResponse.Pending(processorRef);
    }
}
