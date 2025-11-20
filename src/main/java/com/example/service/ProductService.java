package com.example.service;

import com.example.exception.ProductNotFoundException;
import com.example.model.Product;
import com.example.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product createProduct(String sku, String name, String description, double price) {
        if (productRepository.existsBySku(sku)) {
            throw new IllegalArgumentException("Produkt med SKU " + sku + " finns redan");
        }

        if (price < 0) {
            throw new IllegalArgumentException("Pris kan inte vara negativt");
        }

        Product product = new Product(sku, name, description, price);
        Product saved = productRepository.save(product);
        logger.info("Produkt skapad: {}", saved.getSku());
        return saved;
    }

    public Product updateProduct(String sku, String name, String description, Double price) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException("Produkt med SKU " + sku + " hittades inte"));

        if (name != null && !name.trim().isEmpty()) {
            product.setName(name);
        }
        if (description != null) {
            product.setDescription(description);
        }
        if (price != null && price >= 0) {
            product.setPrice(price);
        }

        Product updated = productRepository.save(product);
        logger.info("Produkt uppdaterad: {}", sku);
        return updated;
    }

    public void disableProduct(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException("Produkt med SKU " + sku + " hittades inte"));

        product.setActive(false);
        productRepository.save(product);
        logger.info("Produkt inaktiverad: {}", sku);
    }

    public Product getProductBySku(String sku) {
        return productRepository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException("Produkt med SKU " + sku + " hittades inte"));
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getActiveProducts() {
        return productRepository.findByActive(true);
    }

    public List<Product> searchProducts(String keyword) {
        return productRepository.findByNameContaining(keyword);
    }
}