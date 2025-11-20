package com.example.service;

import com.example.model.Customer;
import com.example.model.OrderItem;
import com.example.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartService {
    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    // Håller kundvagnar i minnet (per kund)
    private final Map<Long, List<OrderItem>> carts = new HashMap<>();
    private final InventoryService inventoryService;

    public CartService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    public void addToCart(Customer customer, Product product, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Kvantitet måste vara större än 0");
        }

        if (!inventoryService.hasStock(product, quantity)) {
            throw new IllegalStateException(
                    String.format("Otillräckligt lager för %s. I lager: %d",
                            product.getName(), inventoryService.getStock(product)));
        }

        List<OrderItem> cart = carts.computeIfAbsent(customer.getId(), k -> new ArrayList<>());

        // Kolla om produkten redan finns i kundvagnen
        OrderItem existingItem = cart.stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            int newQuantity = existingItem.getQuantity() + quantity;
            if (!inventoryService.hasStock(product, newQuantity)) {
                throw new IllegalStateException("Kan inte lägga till mer - otillräckligt lager");
            }
            existingItem.setQuantity(newQuantity);
            logger.info("Uppdaterad kvantitet för {} i kundvagn: {}", product.getSku(), newQuantity);
        } else {
            OrderItem newItem = new OrderItem(product, quantity);
            cart.add(newItem);
            logger.info("Lade till {} x{} i kundvagn", product.getSku(), quantity);
        }
    }

    public void removeFromCart(Customer customer, Product product) {
        List<OrderItem> cart = carts.get(customer.getId());
        if (cart == null) {
            return;
        }

        cart.removeIf(item -> item.getProduct().getId().equals(product.getId()));
        logger.info("Tog bort {} från kundvagn", product.getSku());
    }

    public List<OrderItem> getCart(Customer customer) {
        return carts.getOrDefault(customer.getId(), new ArrayList<>());
    }

    public void clearCart(Customer customer) {
        carts.remove(customer.getId());
        logger.info("Tömde kundvagn för {}", customer.getEmail());
    }

    public double getCartTotal(Customer customer) {
        return getCart(customer).stream()
                .mapToDouble(OrderItem::getLineTotal)
                .sum();
    }

    public boolean isCartEmpty(Customer customer) {
        List<OrderItem> cart = carts.get(customer.getId());
        return cart == null || cart.isEmpty();
    }
}