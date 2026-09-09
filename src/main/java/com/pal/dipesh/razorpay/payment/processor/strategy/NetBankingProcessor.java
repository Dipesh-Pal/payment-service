package com.pal.dipesh.razorpay.payment.processor.strategy;

import com.pal.dipesh.razorpay.common.util.RandomizerUtil;
import com.pal.dipesh.razorpay.payment.processor.PaymentProcessor;
import com.pal.dipesh.razorpay.common.pojo.PaymentProcessorRequest;
import com.pal.dipesh.razorpay.common.pojo.PaymentProcessorResponse;

import org.springframework.stereotype.Component;

@Component
public class NetBankingProcessor implements PaymentProcessor {

    @Override
    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {
        // Call Third Party
        final String BANK_CODE_FAIL = "BANK_CODE_FAIL";

        String bankCode = request.methodDetails() != null ? request.methodDetails().get("bank").toString() : null;

        //simulation
        if (bankCode == null || bankCode.isEmpty() || bankCode.equalsIgnoreCase(BANK_CODE_FAIL)) {
            return new PaymentProcessorResponse.Failure("NET_BANKING_FAILED", "Bank rejected the transaction registration");
        }

        String processorRef = "NET_BANKING_PROCESSOR_" + RandomizerUtil.randomBase64(16);
        // String redirectRef = "https://REDIRECT_BANK.com/" + processorRef; // TODO: Need to handle it Differently

        return new PaymentProcessorResponse.Pending(processorRef);
    }
}
