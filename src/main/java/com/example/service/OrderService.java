package com.example.service;

import com.example.exception.OrderNotFoundException;
import com.example.model.*;
import com.example.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;
    private final PaymentService paymentService;

    public OrderService(OrderRepository orderRepository,
                        InventoryService inventoryService,
                        PaymentService paymentService) {
        this.orderRepository = orderRepository;
        this.inventoryService = inventoryService;
        this.paymentService = paymentService;
    }

    public Order createImportedOrder(Order imported) {
        Order saved = orderRepository.save(imported);

        if (saved.getStatus() == OrderStatus.PAID) {
            for (OrderItem item : saved.getItems()) {
                inventoryService.reserveStock(item.getProduct(), item.getQuantity());
            }
            logger.info("Importerad order {} sparad som PAID – lager reducerat.", saved.getId());
        } else {
            logger.info("Importerad order {} sparad med status {}.", saved.getId(), saved.getStatus());
        }

        return saved;
    }

    public Order createOrder(Customer customer, List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order måste innehålla minst en produkt");
        }

        // Validera lager för alla produkter
        for (OrderItem item : items) {
            if (!inventoryService.hasStock(item.getProduct(), item.getQuantity())) {
                throw new IllegalStateException(
                        String.format("Otillräckligt lager för %s", item.getProduct().getName()));
            }
        }

        Order order = new Order(customer);
        for (OrderItem item : items) {
            item.setOrder(order);
            order.addItem(item);
        }

        Order saved = orderRepository.save(order);
        logger.info("Order skapad: {} för kund: {}", saved.getId(), customer.getEmail());
        return saved;
    }

    public Order checkout(Long orderId, PaymentMethod paymentMethod) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order " + orderId + " hittades inte"));

        if (order.getStatus() != OrderStatus.NEW) {
            throw new IllegalStateException("Order kan endast betalas om status är NEW");
        }

        // Reservera lager
        try {
            for (OrderItem item : order.getItems()) {
                inventoryService.reserveStock(item.getProduct(), item.getQuantity());
            }
        } catch (Exception e) {
            logger.error("Kunde inte reservera lager för order {}", orderId, e);
            throw new IllegalStateException("Checkout misslyckades: " + e.getMessage());
        }

        // Skapa betalning
        Payment payment = paymentService.processPayment(order, paymentMethod);

        // Uppdatera orderstatus baserat på betalning
        if (payment.getStatus() == PaymentStatus.APPROVED) {
            order.setStatus(OrderStatus.PAID);
            logger.info("Order {} betald via {}", orderId, paymentMethod);
        } else {
            order.setStatus(OrderStatus.CANCELLED);
            // Återställ lager vid misslyckad betalning
            for (OrderItem item : order.getItems()) {
                inventoryService.addStock(item.getProduct(), item.getQuantity());
            }
            logger.warn("Order {} avbröts - betalning nekad", orderId);
        }

        return orderRepository.save(order);
    }

    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order " + orderId + " hittades inte"));

        if (order.getStatus() == OrderStatus.PAID) {
            throw new IllegalStateException("Kan inte avbryta en betald order");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        logger.info("Order {} avbröts", orderId);
    }

    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order " + orderId + " hittades inte"));
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    public List<Order> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    public List<Order> getOrdersByDateRange(LocalDate start, LocalDate end) {
        return orderRepository.findByDateBetween(start, end);
    }

    public double getTotalRevenue(LocalDate start, LocalDate end) {
        return orderRepository.findByDateBetween(start, end).stream()
                .filter(o -> o.getStatus() == OrderStatus.PAID)
                .mapToDouble(Order::getTotal)
                .sum();
    }

    public Map<Product, Long> getTopProducts(int limit) {
        return orderRepository.findAll().stream()
                .filter(o -> o.getStatus() == OrderStatus.PAID)
                .flatMap(o -> o.getItems().stream())
                .collect(Collectors.groupingBy(
                        OrderItem::getProduct,
                        Collectors.summingLong(OrderItem::getQuantity)
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<Product, Long>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        java.util.LinkedHashMap::new
                ));
    }
}