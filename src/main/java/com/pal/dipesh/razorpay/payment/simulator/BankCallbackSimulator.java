package com.pal.dipesh.razorpay.payment.simulator;

import com.pal.dipesh.razorpay.common.enums.ChaosMode;
import com.pal.dipesh.razorpay.common.enums.PaymentStatus;
import com.pal.dipesh.razorpay.common.util.RandomizerUtil;
import com.pal.dipesh.razorpay.payment.entity.Payment;
import com.pal.dipesh.razorpay.payment.repository.PaymentRepository;
import com.pal.dipesh.razorpay.payment.service.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BankCallbackSimulator {

    private final PaymentRepository paymentRepository;
    private final SimulatorConfig simulatorConfig;
    private final PaymentService paymentService;

    @Scheduled(fixedDelayString = "${payment.simulator.poll-interval-ms:5000}")
    public void processCallbacks(){
        LocalDateTime globalWindow = LocalDateTime.now().minusSeconds(1);

        List<Payment> candidates = paymentRepository.findByStatusAndCreatedAtBefore(PaymentStatus.AUTHORIZING, globalWindow);

        log.info("Simulating payments for {} payments", candidates.size());

        if(candidates.isEmpty()){
            log.info("No payments found for given status");
            return;
        }

        candidates.forEach(this::simulateCallback);
    }

    private void simulateCallback(Payment payment){
        log.info("Simulating callback for payment: {}", payment.getId());

        SimulatorConfig.MethodSimulatorConfig methodSimulatorConfig = simulatorConfig.configFor(payment.getMethod());

        LocalDateTime dueAt = dueAt(payment, methodSimulatorConfig);

        if(LocalDateTime.now().isBefore(dueAt)){
            log.info("Simulated payment processing is due-at: {}", dueAt);
            return;
        }

        ChaosMode chaosMode = simulatorConfig.getChaosMode();

        switch (chaosMode){
            case SUCCESS -> resolve(payment, true);
            case FAILURE -> resolve(payment, false);
            case TIMEOUT -> log.debug("BankCallbackSimulator: Payment timed out");
            case NORMAL, SLOW -> resolve(payment, shouldApprove(payment, methodSimulatorConfig));
        }
    }

    private void resolve(Payment payment, boolean approve){
        if(approve){
            String bankRef = "SIM_BANK_REF" + RandomizerUtil.randomBase64(8);
            paymentService.resolveAuthorization(payment.getId(), true, bankRef, null, null);
        } else {
            paymentService.resolveAuthorization(payment.getId(), false, null, "SIM_BANK_ERROR_CODE", "Simulated Bank Decline");
        }
    }

    private boolean shouldApprove(Payment  payment, SimulatorConfig.MethodSimulatorConfig  methodSimulatorConfig){
        int bucket = Math.abs(payment.getId().hashCode()) % 100;
        return bucket < methodSimulatorConfig.getSuccessRate();
    }

    private LocalDateTime dueAt(Payment payment, SimulatorConfig.MethodSimulatorConfig methodSimulatorConfig){
        LocalDateTime createdAt = payment.getCreatedAt();

        int minDelay = methodSimulatorConfig.getMinDelaySeconds();
        int maxDelay = methodSimulatorConfig.getMaxDelaySeconds();

        int delayRange = maxDelay - minDelay + 1;
        int delay = minDelay + (Math.abs(payment.getId().hashCode()) % delayRange);

        if(simulatorConfig.getChaosMode() == ChaosMode.SLOW){
            delay *= 2;
        }

        return createdAt.plusSeconds(delay);
    }
}
