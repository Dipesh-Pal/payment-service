package com.pal.dipesh.razorpay.payment;

import com.pal.dipesh.razorpay.payment.simulator.SimulatorConfig;

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableFeignClients
@SpringBootApplication
@EnableSchedulerLock(defaultLockAtMostFor = "10m")
@EnableConfigurationProperties(SimulatorConfig.class)
public class PaymentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentServiceApplication.class, args);
	}

}
