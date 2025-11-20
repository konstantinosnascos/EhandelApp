package com.example.model;

public class Inventory {
    private Long productId;
    private int inStock;

    public Inventory() {
    }

    public Inventory(Long productId, int inStock) {
        this.productId = productId;
        this.inStock = inStock;
    }

    public boolean hasStock(int quantity) {
        return inStock >= quantity;
    }

    public void reserve(int quantity) {
        if (!hasStock(quantity)) {
            throw new IllegalStateException("Inte tillräckligt i lager");
        }
        this.inStock -= quantity;
    }

    public void restock(int quantity) {
        this.inStock += quantity;
    }

    // Getters & Setters
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public int getInStock() {
        return inStock;
    }

    public void setInStock(int inStock) {
        this.inStock = inStock;
    }

    @Override
    public String toString() {
        return "Inventory{" +
                "productId=" + productId +
                ", inStock=" + inStock +
                '}';
    }
}