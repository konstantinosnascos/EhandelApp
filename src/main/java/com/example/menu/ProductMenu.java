package com.example.menu;

import com.example.helper.InputHelper;
import com.example.model.Product;
import com.example.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ProductMenu {
    private static final Logger logger = LoggerFactory.getLogger(ProductMenu.class);

    private final InputHelper input;
    private final ProductService productService;

    public ProductMenu(InputHelper input, ProductService productService) {
        this.input = input;
        this.productService = productService;
    }

    public void run() {
        boolean running = true;
        while (running) {
            try {
                printMenu();
                int choice = input.getInt("Välj alternativ: ");
                switch (choice) {
                    case 1 -> listProducts();
                    case 2 -> searchProducts();
                    case 3 -> addProduct();
                    case 4 -> updateProduct();
                    case 5 -> disableProduct();
                    case 6 -> running = false;
                    default -> System.out.println("Ogiltigt val, försök igen!");
                }
            } catch (Exception e) {
                System.out.println("Ett fel uppstod: " + e.getMessage());
                logger.error("Fel i ProductMenu", e);
            }
        }
    }

    private void printMenu() {
        System.out.println("\n=== PRODUKTHANTERING ===");
        System.out.println("1. Lista alla produkter");
        System.out.println("2. Sök produkt");
        System.out.println("3. Lägg till produkt");
        System.out.println("4. Uppdatera produkt");
        System.out.println("5. Inaktivera produkt");
        System.out.println("6. Tillbaka till huvudmeny");
    }

    private void listProducts() {
        System.out.println("\n--- Alla produkter ---");
        List<Product> products = productService.getAllProducts();

        if (products.isEmpty()) {
            System.out.println("Inga produkter finns.");
            return;
        }

        System.out.println("\n┌──────────┬────────────────────────────┬──────────┬────────┐");
        System.out.println("│ SKU      │ Namn                       │ Pris     │ Status │");
        System.out.println("├──────────┼────────────────────────────┼──────────┼────────┤");

        for (Product p : products) {
            System.out.printf("│ %-8s │ %-26s │ %8.2f │ %-6s │%n",
                    p.getSku(),
                    truncate(p.getName(), 26),
                    p.getPrice(),
                    p.isActive() ? "Aktiv" : "Inaktiv");
        }

        System.out.println("└──────────┴────────────────────────────┴──────────┴────────┘");
        System.out.printf("Totalt: %d produkter%n", products.size());

        logger.info("Listad {} produkter", products.size());
    }

    private void searchProducts() {
        String keyword = input.getString("\nSök efter produkt (namn): ");
        List<Product> results = productService.searchProducts(keyword);

        if (results.isEmpty()) {
            System.out.println("Inga produkter hittades.");
            return;
        }

        System.out.printf("\nHittade %d produkt(er):%n", results.size());
        results.forEach(System.out::println);

        logger.info("Sökning '{}' gav {} resultat", keyword, results.size());
    }

    private void addProduct() {
        System.out.println("\n--- Lägg till ny produkt ---");

        String sku = input.getString("SKU (ex: PROD001): ").toUpperCase();
        String name = input.getString("Produktnamn: ");
        String description = input.getString("Beskrivning: ");
        double price = input.getDouble("Pris: ");

        try {
            Product product = productService.createProduct(sku, name, description, price);
            System.out.println("\n Produkt skapad!");
            System.out.println(product);
            logger.info("Ny produkt skapad: {}", sku);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            logger.warn("Kunde inte skapa produkt: {}", e.getMessage());
        }
    }

    private void updateProduct() {
        String sku = input.getString("\nAnge SKU för produkt att uppdatera: ").toUpperCase();

        try {
            Product existing = productService.getProductBySku(sku);
            System.out.println("\nNuvarande produkt:");
            System.out.println(existing);

            System.out.println("\nAnge nya värden (lämna tomt för att behålla):");
            String name = input.getOptionalString("Namn [" + existing.getName() + "]: ");
            String description = input.getOptionalString("Beskrivning [" + existing.getDescription() + "]: ");
            String priceStr = input.getOptionalString("Pris [" + existing.getPrice() + "]: ");

            Double price = null;
            if (!priceStr.isEmpty()) {
                price = Double.parseDouble(priceStr);
            }

            Product updated = productService.updateProduct(
                    sku,
                    name.isEmpty() ? null : name,
                    description.isEmpty() ? null : description,
                    price
            );

            System.out.println("\n Produkt uppdaterad!");
            System.out.println(updated);
            logger.info("Produkt uppdaterad: {}", sku);

        } catch (Exception e) {
            System.out.println(" " + e.getMessage());
            logger.warn("Kunde inte uppdatera produkt: {}", e.getMessage());
        }
    }

    private void disableProduct() {
        String sku = input.getString("\nAnge SKU för produkt att inaktivera: ").toUpperCase();

        try {
            productService.disableProduct(sku);
            System.out.println(" Produkt inaktiverad: " + sku);
            logger.info("Produkt inaktiverad: {}", sku);
        } catch (Exception e) {
            System.out.println( e.getMessage());
            logger.warn("Kunde inte inaktivera produkt: {}", e.getMessage());
        }
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return "";
        return str.length() > maxLength ? str.substring(0, maxLength - 3) + "..." : str;
    }
}