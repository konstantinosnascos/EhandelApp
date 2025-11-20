package com.example.menu;

import com.example.helper.InputHelper;
import com.example.model.*;
import com.example.service.CartService;
import com.example.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class OrderMenu {
    private static final Logger logger = LoggerFactory.getLogger(OrderMenu.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final InputHelper input;
    private final OrderService orderService;
    private final CartService cartService;
    private final CartMenu cartMenu;

    public OrderMenu(InputHelper input, OrderService orderService,
                     CartService cartService, CartMenu cartMenu) {
        this.input = input;
        this.orderService = orderService;
        this.cartService = cartService;
        this.cartMenu = cartMenu;
    }

    public void run() {
        boolean running = true;
        while (running) {
            try {
                printMenu();
                int choice = input.getInt("Välj alternativ: ");
                switch (choice) {
                    case 1 -> checkout();
                    case 2 -> listOrders();
                    case 3 -> showOrder();
                    case 4 -> cancelOrder();
                    case 5 -> running = false;
                    default -> System.out.println("Ogiltigt val, försök igen!");
                }
            } catch (Exception e) {
                System.out.println("Ett fel uppstod: " + e.getMessage());
                logger.error("Fel i OrderMenu", e);
            }
        }
    }

    private void printMenu() {
        System.out.println("\n=== ORDERHANTERING ===");
        System.out.println("1. Checka ut (skapa order från kundvagn)");
        System.out.println("2. Lista alla ordrar");
        System.out.println("3. Visa orderdetaljer");
        System.out.println("4. Avbryt order");
        System.out.println("5. Tillbaka till huvudmeny");
    }

    private void checkout() {
        Customer customer = cartMenu.getCurrentCustomer();

        if (customer == null) {
            System.out.println("Ingen kund vald. Gå till Kundvagn-menyn och välj kund först.");
            return;
        }

        if (cartService.isCartEmpty(customer)) {
            System.out.println("Kundvagnen är tom. Lägg till produkter först.");
            return;
        }

        // Visa kundvagn
        List<OrderItem> items = cartService.getCart(customer);
        double total = cartService.getCartTotal(customer);

        System.out.println("\n=== CHECKOUT ===");
        System.out.println("Kund: " + customer.getName() + " (" + customer.getEmail() + ")");
        System.out.println("\nProdukter:");

        for (OrderItem item : items) {
            System.out.printf("  %dx %s @ %.2f kr = %.2f kr%n",
                    item.getQuantity(),
                    item.getProduct().getName(),
                    item.getUnitPrice(),
                    item.getLineTotal());
        }

        System.out.printf("\nTotalt: %.2f kr%n", total);

        // Välj betalningsmetod
        System.out.println("\nVälj betalningsmetod:");
        System.out.println("1. Kort (CARD)");
        System.out.println("2. Faktura (INVOICE)");
        int methodChoice = input.getInt("Ditt val: ");

        PaymentMethod method = methodChoice == 2 ? PaymentMethod.INVOICE : PaymentMethod.CARD;

        String confirm = input.getString("\nBekräfta köp? (ja/nej): ");
        if (!confirm.equalsIgnoreCase("ja")) {
            System.out.println("Checkout avbruten.");
            return;
        }

        try {
            // Skapa order från kundvagn
            Order order = orderService.createOrder(customer, items);
            System.out.println("\nOrder skapad med ID: " + order.getId());

            // Genomför betalning
            System.out.println("Bearbetar betalning...");
            Order paidOrder = orderService.checkout(order.getId(), method);

            if (paidOrder.getStatus() == OrderStatus.PAID) {
                System.out.println("\nBETALNING GODKÄND!");
                System.out.println("Order #" + paidOrder.getId() + " är betald.");
                System.out.println("Totalt: " + paidOrder.getTotal() + " kr");

                // Töm kundvagn
                cartService.clearCart(customer);
                logger.info("Order {} skapad och betald för kund {}", order.getId(), customer.getEmail());
            } else {
                System.out.println("\nBETALNING NEKAD!");
                System.out.println("Order #" + paidOrder.getId() + " har avbrutits.");
                System.out.println("Vänligen försök igen eller välj annan betalningsmetod.");
                logger.warn("Betalning nekad för order {}", order.getId());
            }

        } catch (Exception e) {
            System.out.println("Checkout misslyckades: " + e.getMessage());
            logger.error("Fel vid checkout", e);
        }
    }

    private void listOrders() {
        System.out.println("\n--- Alla ordrar ---");

        System.out.println("Filtrera efter status?");
        System.out.println("1. Alla ordrar");
        System.out.println("2. Nya (NEW)");
        System.out.println("3. Betalda (PAID)");
        System.out.println("4. Avbrutna (CANCELLED)");
        int choice = input.getInt("Välj: ");

        List<Order> orders;

        switch (choice) {
            case 2 -> orders = orderService.getOrdersByStatus(OrderStatus.NEW);
            case 3 -> orders = orderService.getOrdersByStatus(OrderStatus.PAID);
            case 4 -> orders = orderService.getOrdersByStatus(OrderStatus.CANCELLED);
            default -> orders = orderService.getAllOrders();
        }

        if (orders.isEmpty()) {
            System.out.println("Inga ordrar hittades.");
            return;
        }

        System.out.println("\n┌──────┬────────────────────┬──────────────────┬───────────┬───────────┐");
        System.out.println("│ ID   │ Kund               │ Datum            │ Status    │ Totalt    │");
        System.out.println("├──────┼────────────────────┼──────────────────┼───────────┼───────────┤");

        for (Order o : orders) {
            System.out.printf("│ %-4d │ %-18s │ %-16s │ %-9s │ %9.2f │%n",
                    o.getId(),
                    truncate(o.getCustomer().getName(), 18),
                    o.getCreatedAt().format(FORMATTER),
                    o.getStatus(),
                    o.getTotal());
        }

        System.out.println("└──────┴────────────────────┴──────────────────┴───────────┴───────────┘");
        System.out.printf("Totalt: %d ordrar%n", orders.size());

        logger.info("Listad {} ordrar", orders.size());
    }

    private void showOrder() {
        Long orderId = (long) input.getInt("\nAnge order-ID: ");

        try {
            Order order = orderService.getOrder(orderId);

            System.out.println("\n=== ORDERDETALJER ===");
            System.out.println("Order-ID: " + order.getId());
            System.out.println("Kund: " + order.getCustomer().getName() + " (" + order.getCustomer().getEmail() + ")");
            System.out.println("Status: " + order.getStatus());
            System.out.println("Skapad: " + order.getCreatedAt().format(FORMATTER));

            System.out.println("\n--- Produkter ---");
            System.out.println("┌──────────┬────────────────────────┬─────┬──────────┬───────────┐");
            System.out.println("│ SKU      │ Produkt                │ Ant │ á-pris   │ Summa     │");
            System.out.println("├──────────┼────────────────────────┼─────┼──────────┼───────────┤");

            for (OrderItem item : order.getItems()) {
                System.out.printf("│ %-8s │ %-22s │ %3d │ %8.2f │ %9.2f │%n",
                        item.getProduct().getSku(),
                        truncate(item.getProduct().getName(), 22),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getLineTotal());
            }

            System.out.println("└──────────┴────────────────────────┴─────┴──────────┴───────────┘");
            System.out.printf("\nTOTALT: %.2f kr%n", order.getTotal());

            logger.info("Visade order {}", orderId);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            logger.warn("Kunde inte visa order: {}", e.getMessage());
        }
    }

    private void cancelOrder() {
        Long orderId = (long) input.getInt("\nAnge order-ID att avbryta: ");

        try {
            Order order = orderService.getOrder(orderId);

            System.out.println("\nOrder #" + orderId);
            System.out.println("Kund: " + order.getCustomer().getName());
            System.out.println("Status: " + order.getStatus());
            System.out.println("Totalt: " + order.getTotal() + " kr");

            String confirm = input.getString("\nÄr du säker på att du vill avbryta denna order? (ja/nej): ");

            if (confirm.equalsIgnoreCase("ja")) {
                orderService.cancelOrder(orderId);
                System.out.println("Order avbruten!");
                logger.info("Order {} avbruten", orderId);
            } else {
                System.out.println("Avbokning avbruten.");
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            logger.warn("Kunde inte avbryta order: {}", e.getMessage());
        }
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return "";
        return str.length() > maxLength ? str.substring(0, maxLength - 3) + "..." : str;
    }
}