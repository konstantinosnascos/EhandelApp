package com.example.repository;

import com.example.model.Inventory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class InventoryRepository {
    private static final Logger logger = LoggerFactory.getLogger(InventoryRepository.class);

    private final Map<Long, Inventory> inventory = new HashMap<>();

    public Inventory save(Inventory inv) {
        inventory.put(inv.getProductId(), inv);
        logger.info("Lager uppdaterat för produkt {}: {} st", inv.getProductId(), inv.getInStock());
        return inv;
    }

    public Optional<Inventory> findByProductId(Long productId) {
        return Optional.ofNullable(inventory.get(productId));
    }

    public List<Inventory> findAll() {
        return new ArrayList<>(inventory.values());
    }

    public List<Inventory> findLowStock(int threshold) {
        return inventory.values().stream()
                .filter(inv -> inv.getInStock() < threshold)
                .collect(Collectors.toList());
    }

    public void delete(Long productId) {
        inventory.remove(productId);
        logger.info("Lagerpost raderad för produkt: {}", productId);
    }

    public void deleteAll() {
        inventory.clear();
        logger.info("Alla lagerposter raderade");
    }
}