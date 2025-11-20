package com.example.menu;

import com.example.helper.InputHelper;
import com.example.model.Customer;
import com.example.model.OrderItem;
import com.example.model.Product;
import com.example.service.CartService;
import com.example.service.CustomerService;
import com.example.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CartMenu {
    private static final Logger logger = LoggerFactory.getLogger(CartMenu.class);

    private final InputHelper input;
    private final CartService cartService;
    private final ProductService productService;
    private final CustomerService customerService;
    private Customer currentCustomer;

    public CartMenu(InputHelper input, CartService cartService,
                    ProductService productService, CustomerService customerService) {
        this.input = input;
        this.cartService = cartService;
        this.productService = productService;
        this.customerService = customerService;
    }

    public void run() {
        boolean running = true;
        while (running) {
            try {
                printMenu();
                int choice = input.getInt("Välj alternativ: ");
                switch (choice) {
                    case 1 -> selectCustomer();
                    case 2 -> addToCart();
                    case 3 -> removeFromCart();
                    case 4 -> showCart();
                    case 5 -> clearCart();
                    case 6 -> running = false;
                    default -> System.out.println("Ogiltigt val, försök igen!");
                }
            } catch (Exception e) {
                System.out.println("Ett fel uppstod: " + e.getMessage());
                logger.error("Fel i CartMenu", e);
            }
        }
    }

    private void printMenu() {
        System.out.println("\n=== KUNDVAGN ===");
        if (currentCustomer != null) {
            System.out.println("Aktiv kund: " + currentCustomer.getName() + " (" + currentCustomer.getEmail() + ")");
        } else {
            System.out.println("Ingen kund vald");
        }
        System.out.println("\n1. Välj kund");
        System.out.println("2. Lägg till i kundvagn");
        System.out.println("3. Ta bort från kundvagn");
        System.out.println("4. Visa kundvagn");
        System.out.println("5. Töm kundvagn");
        System.out.println("6. Tillbaka till huvudmeny");
    }

    private void selectCustomer() {
        String email = input.getString("\nAnge kundens email: ");

        try {
            currentCustomer = customerService.getCustomerByEmail(email);
            System.out.println("Kund vald: " + currentCustomer.getName());
            logger.info("Valde kund: {}", email);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            currentCustomer = null;
        }
    }

    private void addToCart() {
        if (currentCustomer == null) {
            System.out.println("Du måste välja en kund först!");
            return;
        }

        String sku = input.getString("\nAnge SKU: ").toUpperCase();

        try {
            Product product = productService.getProductBySku(sku);

            if (!product.isActive()) {
                System.out.println("Produkten är inaktiv och kan inte köpas.");
                return;
            }

            System.out.println("\nProdukt: " + product.getName());
            System.out.printf("Pris: %.2f kr%n", product.getPrice());

            int quantity = input.getInt("Antal: ");

            cartService.addToCart(currentCustomer, product, quantity);
            System.out.println("Tillagd i kundvagn!");

            showCartSummary();

        } catch (Exception e) {
            System.out.println(e.getMessage());
            logger.warn("Kunde inte lägga till i kundvagn: {}", e.getMessage());
        }
    }

    private void removeFromCart() {
        if (currentCustomer == null) {
            System.out.println("Du måste välja en kund först!");
            return;
        }

        if (cartService.isCartEmpty(currentCustomer)) {
            System.out.println("Kundvagnen är tom.");
            return;
        }

        showCart();

        String sku = input.getString("\nAnge SKU att ta bort: ").toUpperCase();

        try {
            Product product = productService.getProductBySku(sku);
            cartService.removeFromCart(currentCustomer, product);
            System.out.println("Borttagen från kundvagn!");

        } catch (Exception e) {
            System.out.println( e.getMessage());
        }
    }

    private void showCart() {
        if (currentCustomer == null) {
            System.out.println("Du måste välja en kund först!");
            return;
        }

        List<OrderItem> items = cartService.getCart(currentCustomer);

        if (items.isEmpty()) {
            System.out.println("\nKundvagnen är tom.");
            return;
        }

        System.out.println("\n=== KUNDVAGN ===");
        System.out.println("Kund: " + currentCustomer.getName());
        System.out.println("\n┌──────────┬────────────────────────┬─────┬──────────┬───────────┐");
        System.out.println("│ SKU      │ Produkt                │ Ant │ á-pris   │ Summa     │");
        System.out.println("├──────────┼────────────────────────┼─────┼──────────┼───────────┤");

        for (OrderItem item : items) {
            System.out.printf("│ %-8s │ %-22s │ %3d │ %8.2f │ %9.2f │%n",
                    item.getProduct().getSku(),
                    truncate(item.getProduct().getName(), 22),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getLineTotal());
        }

        System.out.println("└──────────┴────────────────────────┴─────┴──────────┴───────────┘");
        System.out.printf("TOTALT: %.2f kr%n", cartService.getCartTotal(currentCustomer));
    }

    private void showCartSummary() {
        double total = cartService.getCartTotal(currentCustomer);
        int itemCount = cartService.getCart(currentCustomer).size();
        System.out.printf("\nKundvagn: %d artikel(ar), Summa: %.2f kr%n", itemCount, total);
    }

    private void clearCart() {
        if (currentCustomer == null) {
            System.out.println("Du måste välja en kund först!");
            return;
        }

        String confirm = input.getString("\nÄr du säker på att du vill tömma kundvagnen? (ja/nej): ");
        if (confirm.equalsIgnoreCase("ja")) {
            cartService.clearCart(currentCustomer);
            System.out.println("Kundvagnen tömdes!");
        }
    }

    public Customer getCurrentCustomer() {
        return currentCustomer;
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return "";
        return str.length() > maxLength ? str.substring(0, maxLength - 3) + "..." : str;
    }
}