package com.example.model;

import java.time.LocalDateTime;

public class Payment {
    private Long id;
    private Order order;
    private PaymentMethod method;
    private PaymentStatus status;
    private LocalDateTime timestamp;

    public Payment() {
        this.status = PaymentStatus.PENDING;
        this.timestamp = LocalDateTime.now();
    }

    public Payment(Order order, PaymentMethod method) {
        this.order = order;
        this.method = method;
        this.status = PaymentStatus.PENDING;
        this.timestamp = LocalDateTime.now();
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return String.format("Payment{id=%d, method=%s, status=%s, timestamp=%s}",
                id, method, status, timestamp);
    }
}