package com.example.service;

import com.example.exception.InsufficientStockException;
import com.example.model.Inventory;
import com.example.model.Product;
import com.example.repository.InventoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class InventoryService {
    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public void addStock(Product product, int quantity) {
        Inventory inventory = inventoryRepository.findByProductId(product.getId())
                .orElse(new Inventory(product.getId(), 0));

        inventory.restock(quantity);
        inventoryRepository.save(inventory);
        logger.info("Lager påfyllt för {}: +{} (totalt: {})",
                product.getSku(), quantity, inventory.getInStock());
    }

    public void reserveStock(Product product, int quantity) {
        Inventory inventory = inventoryRepository.findByProductId(product.getId())
                .orElseThrow(() -> new InsufficientStockException(
                        "Ingen lagerpost finns för produkt: " + product.getSku()));

        if (!inventory.hasStock(quantity)) {
            throw new InsufficientStockException(
                    String.format("Otillräckligt lager för %s. I lager: %d, Begärt: %d",
                            product.getSku(), inventory.getInStock(), quantity));
        }

        inventory.reserve(quantity);
        inventoryRepository.save(inventory);
        logger.info("Lager reserverat för {}: -{} (kvar: {})",
                product.getSku(), quantity, inventory.getInStock());
    }

    public int getStock(Product product) {
        return inventoryRepository.findByProductId(product.getId())
                .map(Inventory::getInStock)
                .orElse(0);
    }

    public List<Inventory> getLowStockItems(int threshold) {
        return inventoryRepository.findLowStock(threshold);
    }

    public boolean hasStock(Product product, int quantity) {
        return inventoryRepository.findByProductId(product.getId())
                .map(inv -> inv.hasStock(quantity))
                .orElse(false);
    }
}