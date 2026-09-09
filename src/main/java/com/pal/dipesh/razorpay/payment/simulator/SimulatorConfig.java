package com.pal.dipesh.razorpay.payment.simulator;

import com.pal.dipesh.razorpay.common.enums.ChaosMode;
import com.pal.dipesh.razorpay.common.enums.PaymentMethod;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ConfigurationProperties(prefix = "payment.simulator", ignoreUnknownFields = false)
public class SimulatorConfig {

    private Integer pollIntervalMs = 2000;
    private ChaosMode chaosMode = ChaosMode.NORMAL;
    private Map<String, MethodSimulatorConfig> methods = new HashMap<>();

    public MethodSimulatorConfig configFor(PaymentMethod paymentMethod){
        return methods.getOrDefault(paymentMethod.name(), new MethodSimulatorConfig());
    }

    @Getter
    @Setter
    public static class MethodSimulatorConfig {
        private Integer minDelaySeconds = 1;
        private Integer maxDelaySeconds = 5;
        private Integer successRate = 80;
    }
}
