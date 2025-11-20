package com.example.repository;

import com.example.model.Payment;
import com.example.model.PaymentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class PaymentRepository {
    private static final Logger logger = LoggerFactory.getLogger(PaymentRepository.class);

    private final Map<Long, Payment> payments = new HashMap<>();
    private Long nextId = 1L;

    public Payment save(Payment payment) {
        if (payment.getId() == null) {
            payment.setId(nextId++);
            logger.info("Ny betalning skapad med ID: {}", payment.getId());
        } else {
            logger.info("Betalning uppdaterad med ID: {}", payment.getId());
        }

        payments.put(payment.getId(), payment);
        return payment;
    }

    public Optional<Payment> findById(Long id) {
        return Optional.ofNullable(payments.get(id));
    }

    public Optional<Payment> findByOrderId(Long orderId) {
        return payments.values().stream()
                .filter(p -> p.getOrder().getId().equals(orderId))
                .findFirst();
    }

    public List<Payment> findAll() {
        return new ArrayList<>(payments.values());
    }

    public List<Payment> findByStatus(PaymentStatus status) {
        return payments.values().stream()
                .filter(p -> p.getStatus() == status)
                .collect(Collectors.toList());
    }

    public void delete(Payment payment) {
        payments.remove(payment.getId());
        logger.info("Betalning raderad: {}", payment.getId());
    }

    public void deleteAll() {
        payments.clear();
        nextId = 1L;
        logger.info("Alla betalningar raderade");
    }
}