package com.example.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private Long id;
    private Customer customer;
    private OrderStatus status;
    private double total;
    private LocalDateTime createdAt;
    private List<OrderItem> items;

    public Order() {
        this.status = OrderStatus.NEW;
        this.createdAt = LocalDateTime.now();
        this.items = new ArrayList<>();
    }

    public Order(Customer customer) {
        this.customer = customer;
        this.status = OrderStatus.NEW;
        this.createdAt = LocalDateTime.now();
        this.items = new ArrayList<>();
    }

    public void addItem(OrderItem item) {
        this.items.add(item);
        calculateTotal();
    }

    public void removeItem(OrderItem item) {
        this.items.remove(item);
        calculateTotal();
    }

    public void calculateTotal() {
        this.total = items.stream()
                .mapToDouble(OrderItem::getLineTotal)
                .sum();
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
        calculateTotal();
    }

    @Override
    public String toString() {
        return String.format("Order #%d - %s - %s - %.2f kr (%d produkter)",
                id, customer.getName(), status, total, items.size());
    }
}