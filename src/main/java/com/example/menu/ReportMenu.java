package com.example.menu;

import com.example.helper.InputHelper;
import com.example.model.Inventory;
import com.example.model.Product;
import com.example.service.InventoryService;
import com.example.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

public class ReportMenu {
    private static final Logger logger = LoggerFactory.getLogger(ReportMenu.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final InputHelper input;
    private final OrderService orderService;
    private final InventoryService inventoryService;

    public ReportMenu(InputHelper input, OrderService orderService, InventoryService inventoryService) {
        this.input = input;
        this.orderService = orderService;
        this.inventoryService = inventoryService;
    }

    public void run() {
        boolean running = true;
        while (running) {
            try {
                printMenu();
                int choice = input.getInt("Välj alternativ: ");
                switch (choice) {
                    case 1 -> showTopProducts();
                    case 2 -> showLowStock();
                    case 3 -> showRevenue();
                    case 4 -> running = false;
                    default -> System.out.println("Ogiltigt val, försök igen!");
                }
            } catch (Exception e) {
                System.out.println("Ett fel uppstod: " + e.getMessage());
                logger.error("Fel i ReportMenu", e);
            }
        }
    }

    private void printMenu() {
        System.out.println("\n=== RAPPORTER ===");
        System.out.println("1. Top 5 bästsäljande produkter");
        System.out.println("2. Lågt lager");
        System.out.println("3. Omsättningsrapport");
        System.out.println("4. Tillbaka till huvudmeny");
    }

    private void showTopProducts() {
        System.out.println("\n--- Top 5 Bästsäljare ---");

        Map<Product, Long> topProducts = orderService.getTopProducts(5);

        if (topProducts.isEmpty()) {
            System.out.println("Ingen försäljningsdata finns än.");
            return;
        }

        System.out.println("\n┌──────┬────────────────────────────┬──────────┬────────────┐");
        System.out.println("│ Rank │ Produkt                    │ SKU      │ Antal sålda│");
        System.out.println("├──────┼────────────────────────────┼──────────┼────────────┤");

        int rank = 1;
        for (Map.Entry<Product, Long> entry : topProducts.entrySet()) {
            Product product = entry.getKey();
            Long count = entry.getValue();

            System.out.printf("│ %-4d │ %-26s │ %-8s │ %10d │%n",
                    rank++,
                    truncate(product.getName(), 26),
                    product.getSku(),
                    count);
        }

        System.out.println("└──────┴────────────────────────────┴──────────┴────────────┘");

        logger.info("Visade top 5 produkter");
    }

    private void showLowStock() {
        System.out.println("\n--- Lågt Lager ---");

        int threshold = input.getInt("Ange gräns för lågt lager (standard 5): ");
        if (threshold <= 0) {
            threshold = 5;
        }

        var lowStockItems = inventoryService.getLowStockItems(threshold);

        if (lowStockItems.isEmpty()) {
            System.out.println("Inga produkter har lågt lager.");
            return;
        }

        System.out.println("\n┌────────────┬────────────┐");
        System.out.println("│ Produkt-ID │ I lager    │");
        System.out.println("├────────────┼────────────┤");

        for (Inventory inv : lowStockItems) {
            System.out.printf("│ %-10d │ %10d │%n",
                    inv.getProductId(),
                    inv.getInStock());
        }

        System.out.println("└────────────┴────────────┘");
        System.out.printf("Totalt: %d produkter under gränsen%n", lowStockItems.size());

        logger.info("Visade lågt lager (gräns: {})", threshold);
    }

    private void showRevenue() {
        System.out.println("\n--- Omsättningsrapport ---");

        LocalDate startDate = null;
        LocalDate endDate = null;

        while (startDate == null) {
            try {
                String startStr = input.getString("Från datum (yyyy-MM-dd): ");
                startDate = LocalDate.parse(startStr, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("Felaktigt datumformat. Använd yyyy-MM-dd (t.ex. 2025-01-01)");
            }
        }

        while (endDate == null) {
            try {
                String endStr = input.getString("Till datum (yyyy-MM-dd): ");
                endDate = LocalDate.parse(endStr, DATE_FORMATTER);

                if (endDate.isBefore(startDate)) {
                    System.out.println("Slutdatum kan inte vara före startdatum.");
                    endDate = null;
                }
            } catch (DateTimeParseException e) {
                System.out.println("Felaktigt datumformat. Använd yyyy-MM-dd (t.ex. 2025-12-31)");
            }
        }

        double revenue = orderService.getTotalRevenue(startDate, endDate);
        var orders = orderService.getOrdersByDateRange(startDate, endDate);

        System.out.println("\n=== OMSÄTTNING ===");
        System.out.println("Period: " + startDate.format(DATE_FORMATTER) + " till " + endDate.format(DATE_FORMATTER));
        System.out.println("Antal ordrar: " + orders.size());
        System.out.printf("Total omsättning: %.2f kr%n", revenue);

        logger.info("Visade omsättning för period {} - {}", startDate, endDate);
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return "";
        return str.length() > maxLength ? str.substring(0, maxLength - 3) + "..." : str;
    }
}