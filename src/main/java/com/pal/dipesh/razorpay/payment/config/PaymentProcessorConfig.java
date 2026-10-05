package com.pal.dipesh.razorpay.payment.config;

import com.pal.dipesh.razorpay.common.enums.PaymentMethod;
import com.pal.dipesh.razorpay.payment.processor.PaymentProcessor;
import com.pal.dipesh.razorpay.payment.processor.strategy.NetBankingProcessor;
import com.pal.dipesh.razorpay.payment.processor.strategy.UpiPaymentProcessor;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.EnumMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class PaymentProcessorConfig {

    private final UpiPaymentProcessor upiPaymentProcessor;
    private final NetBankingProcessor netBankingPaymentProcessor;

    @Bean
    public Map<PaymentMethod, PaymentProcessor> paymentProcessorMap() {
        Map<PaymentMethod, PaymentProcessor> map = new EnumMap<>(PaymentMethod.class);

        map.put(PaymentMethod.UPI, upiPaymentProcessor);
        map.put(PaymentMethod.NET_BANKING, netBankingPaymentProcessor);
        map.put(PaymentMethod.WALLET, null); // TODO: Implement WalletPaymentProcessor

        return map;
    }
}