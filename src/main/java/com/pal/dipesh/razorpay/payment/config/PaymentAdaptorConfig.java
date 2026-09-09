package com.pal.dipesh.razorpay.payment.config;

import com.pal.dipesh.razorpay.common.enums.PaymentMethod;
import com.pal.dipesh.razorpay.payment.gateway.PaymentAdapter;
import com.pal.dipesh.razorpay.payment.gateway.adapter.CardPaymentAdapter;
import com.pal.dipesh.razorpay.payment.gateway.adapter.NetBankingAdapter;
import com.pal.dipesh.razorpay.payment.gateway.adapter.UpiPaymentAdapter;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class PaymentAdaptorConfig {

    private final CardPaymentAdapter cardPaymentAdapter;
    private final UpiPaymentAdapter upiPaymentAdapter;
    private final NetBankingAdapter netBankingAdapter;

    @Bean
    public Map<PaymentMethod, PaymentAdapter> paymentAdapterMap() {
        Map<PaymentMethod, PaymentAdapter> map = new HashMap<>();

        map.put(PaymentMethod.CARD, cardPaymentAdapter);
        map.put(PaymentMethod.UPI, upiPaymentAdapter);
        map.put(PaymentMethod.NET_BANKING, netBankingAdapter);
        map.put(PaymentMethod.WALLET, null); // TODO: Implement WalletPaymentAdapter

        return map;
    }
}