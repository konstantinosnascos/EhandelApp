package com.example.repository;

import com.example.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class ProductRepository {
    private static final Logger logger = LoggerFactory.getLogger(ProductRepository.class);

    private final Map<Long, Product> products = new HashMap<>();
    private final Map<String, Product> productsBySku = new HashMap<>();
    private Long nextId = 1L;

    public Product save(Product product) {
        if (product.getId() == null) {
            product.setId(nextId++);
            logger.info("Ny produkt skapad med ID: {}", product.getId());
        } else {
            logger.info("Produkt uppdaterad med ID: {}", product.getId());
        }

        products.put(product.getId(), product);
        productsBySku.put(product.getSku(), product);
        return product;
    }

    public Optional<Product> findById(Long id) {
        return Optional.ofNullable(products.get(id));
    }

    public Optional<Product> findBySku(String sku) {
        return Optional.ofNullable(productsBySku.get(sku));
    }

    public List<Product> findAll() {
        return new ArrayList<>(products.values());
    }

    public List<Product> findByNameContaining(String keyword) {
        return products.values().stream()
                .filter(p -> p.getName().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<Product> findByActive(boolean active) {
        return products.values().stream()
                .filter(p -> p.isActive() == active)
                .collect(Collectors.toList());
    }

    public boolean existsBySku(String sku) {
        return productsBySku.containsKey(sku);
    }

    public void delete(Product product) {
        products.remove(product.getId());
        productsBySku.remove(product.getSku());
        logger.info("Produkt raderad: {}", product.getSku());
    }

    public long count() {
        return products.size();
    }

    public void deleteAll() {
        products.clear();
        productsBySku.clear();
        nextId = 1L;
        logger.info("Alla produkter raderade");
    }
}