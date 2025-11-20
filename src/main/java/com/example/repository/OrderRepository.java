package com.example.repository;

import com.example.model.Order;
import com.example.model.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class OrderRepository {
    private static final Logger logger = LoggerFactory.getLogger(OrderRepository.class);

    private final Map<Long, Order> orders = new HashMap<>();
    private Long nextId = 1L;

    public Order save(Order order) {
        if (order.getId() == null) {
            order.setId(nextId++);
            logger.info("Ny order skapad med ID: {}", order.getId());
        } else {
            logger.info("Order uppdaterad med ID: {}", order.getId());
        }

        orders.put(order.getId(), order);
        return order;
    }

    public Optional<Order> findById(Long id) {
        return Optional.ofNullable(orders.get(id));
    }

    public List<Order> findAll() {
        return new ArrayList<>(orders.values());
    }

    public List<Order> findByStatus(OrderStatus status) {
        return orders.values().stream()
                .filter(o -> o.getStatus() == status)
                .collect(Collectors.toList());
    }

    public List<Order> findByCustomerId(Long customerId) {
        return orders.values().stream()
                .filter(o -> o.getCustomer().getId().equals(customerId))
                .collect(Collectors.toList());
    }

    public List<Order> findByDateBetween(LocalDate start, LocalDate end) {
        return orders.values().stream()
                .filter(o -> {
                    LocalDate orderDate = o.getCreatedAt().toLocalDate();
                    return !orderDate.isBefore(start) && !orderDate.isAfter(end);
                })
                .collect(Collectors.toList());
    }

    public void delete(Order order) {
        orders.remove(order.getId());
        logger.info("Order raderad: {}", order.getId());
    }

    public long count() {
        return orders.size();
    }

    public void deleteAll() {
        orders.clear();
        nextId = 1L;
        logger.info("Alla orders raderade");
    }
}