package com.example.service;

import com.example.model.Order;
import com.example.model.Payment;
import com.example.model.PaymentMethod;
import com.example.model.PaymentStatus;
import com.example.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class PaymentService {
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final Random random = new Random();
    private static final double APPROVAL_RATE = 0.9; // 90% godkännandegrad

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Payment processPayment(Order order, PaymentMethod method) {
        Payment payment = new Payment(order, method);

        // Simulera betalningsbearbetning
        boolean approved = simulatePaymentProcessing();

        if (approved) {
            payment.setStatus(PaymentStatus.APPROVED);
            logger.info("Betalning godkänd för order {} via {}", order.getId(), method);
        } else {
            payment.setStatus(PaymentStatus.DECLINED);
            logger.warn("Betalning nekad för order {} via {}", order.getId(), method);
        }

        return paymentRepository.save(payment);
    }

    private boolean simulatePaymentProcessing() {
        // 90% chans att bli godkänd
        return random.nextDouble() < APPROVAL_RATE;
    }

    public Payment getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElse(null);
    }

    public PaymentStatus getPaymentStatus(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .map(Payment::getStatus)
                .orElse(null);
    }
}