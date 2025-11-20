package com.example.model;

public class OrderItem {
    private Long id;
    private Order order;
    private Product product;
    private int quantity;
    private double unitPrice;
    private double lineTotal;

    public OrderItem() {
    }

    public OrderItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = product.getPrice();
        calculateLineTotal();
    }

    public void calculateLineTotal() {
        this.lineTotal = this.unitPrice * this.quantity;
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

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
        this.unitPrice = product.getPrice();
        calculateLineTotal();
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        calculateLineTotal();
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
        calculateLineTotal();
    }

    public double getLineTotal() {
        return lineTotal;
    }

    @Override
    public String toString() {
        return String.format("%dx %s @ %.2f kr = %.2f kr",
                quantity, product.getName(), unitPrice, lineTotal);
    }
}