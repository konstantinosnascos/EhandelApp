package com.example.menu;

import com.example.helper.EmailValidator;
import com.example.helper.InputHelper;
import com.example.model.Customer;
import com.example.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CustomerMenu {
    private static final Logger logger = LoggerFactory.getLogger(CustomerMenu.class);

    private final InputHelper input;
    private final CustomerService customerService;
    private final EmailValidator emailValidator;

    public CustomerMenu(InputHelper input, CustomerService customerService) {
        this.input = input;
        this.customerService = customerService;
        this.emailValidator = new EmailValidator();
    }

    public void run() {
        boolean running = true;
        while (running) {
            try {
                printMenu();
                int choice = input.getInt("Välj alternativ: ");
                switch (choice) {
                    case 1 -> listCustomers();
                    case 2 -> searchCustomer();
                    case 3 -> addCustomer();
                    case 4 -> running = false;
                    default -> System.out.println("Ogiltigt val, försök igen!");
                }
            } catch (Exception e) {
                System.out.println("Ett fel uppstod: " + e.getMessage());
                logger.error("Fel i CustomerMenu", e);
            }
        }
    }

    private void printMenu() {
        System.out.println("\n=== KUNDHANTERING ===");
        System.out.println("1. Lista alla kunder");
        System.out.println("2. Sök kund");
        System.out.println("3. Lägg till kund");
        System.out.println("4. Tillbaka till huvudmeny");
    }

    private void listCustomers() {
        System.out.println("\n--- Alla kunder ---");
        List<Customer> customers = customerService.getAllCustomers();

        if (customers.isEmpty()) {
            System.out.println("Inga kunder finns.");
            return;
        }

        System.out.println("\n┌──────┬────────────────────────────┬──────────────────────────┐");
        System.out.println("│ ID   │ Namn                       │ Email                    │");
        System.out.println("├──────┼────────────────────────────┼──────────────────────────┤");

        for (Customer c : customers) {
            System.out.printf("│ %-4d │ %-26s │ %-24s │%n",
                    c.getId(),
                    truncate(c.getName(), 26),
                    truncate(c.getEmail(), 24));
        }

        System.out.println("└──────┴────────────────────────────┴──────────────────────────┘");
        System.out.printf("Totalt: %d kunder%n", customers.size());

        logger.info("Listad {} kunder", customers.size());
    }

    private void searchCustomer() {
        System.out.println("\n1. Sök på email");
        System.out.println("2. Sök på namn");
        int choice = input.getInt("Välj: ");

        try {
            if (choice == 1) {
                String email = input.getString("Email: ");
                Customer customer = customerService.getCustomerByEmail(email);
                System.out.println("\nKund hittad:");
                System.out.println(customer);
            } else if (choice == 2) {
                String keyword = input.getString("Namn (söksträng): ");
                List<Customer> results = customerService.searchCustomers(keyword);

                if (results.isEmpty()) {
                    System.out.println("Inga kunder hittades.");
                } else {
                    System.out.printf("\nHittade %d kund(er):%n", results.size());
                    results.forEach(System.out::println);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            logger.warn("Sökning misslyckades: {}", e.getMessage());
        }
    }

    private void addCustomer() {
        System.out.println("\n--- Lägg till ny kund ---");

        String email;
        do {
            email = input.getString("Email: ");
            if (!emailValidator.isValid(email)) {
                System.out.println("Felaktig email-format. Försök igen.");
            }
        } while (!emailValidator.isValid(email));

        String name = input.getString("Namn: ");

        try {
            Customer customer = customerService.createCustomer(email, name);
            System.out.println("\nKund skapad!");
            System.out.println(customer);
            logger.info("Ny kund skapad: {}", email);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            logger.warn("Kunde inte skapa kund: {}", e.getMessage());
        }
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return "";
        return str.length() > maxLength ? str.substring(0, maxLength - 3) + "..." : str;
    }
}