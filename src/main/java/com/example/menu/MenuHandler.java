package com.example.menu;

import com.example.helper.InputHelper;
import com.example.repository.*;
import com.example.service.*;

import java.util.Scanner;

public class MenuHandler {

    private final InputHelper input;
    private final ProductMenu productMenu;
    private final CustomerMenu customerMenu;
    private final CartMenu cartMenu;
    private final OrderMenu orderMenu;
    private final ReportMenu reportMenu;
    private final SystemMenu systemMenu;

    public MenuHandler(Scanner scanner,
                       ProductService productService,
                       CustomerService customerService,
                       CartService cartService,
                       OrderService orderService,
                       InventoryService inventoryService,
                       CSVImportService csvImportService,
                       ProductRepository productRepository,
                       CustomerRepository customerRepository,
                       OrderRepository orderRepository,
                       InventoryRepository inventoryRepository) {

        this.input = new InputHelper(scanner);
        this.productMenu = new ProductMenu(input, productService);
        this.customerMenu = new CustomerMenu(input, customerService);
        this.cartMenu = new CartMenu(input, cartService, productService, customerService);
        this.orderMenu = new OrderMenu(input, orderService, cartService, cartMenu);
        this.reportMenu = new ReportMenu(input, orderService, inventoryService);
        this.systemMenu = new SystemMenu(input, csvImportService, productRepository,
                customerRepository, orderRepository, inventoryRepository);
    }

    public void runMainMenu() {
        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = input.getInt("Välj alternativ: ");
            switch (choice) {
                case 1 -> productMenu.run();
                case 2 -> customerMenu.run();
                case 3 -> cartMenu.run();
                case 4 -> orderMenu.run();
                case 5 -> reportMenu.run();
                case 6 -> systemMenu.run();
                case 7 -> running = false;
                default -> System.out.println("Ogiltigt val.");
            }
        }
        System.out.println("Tack för att du använde E-Handelsapplikationen!");
    }

    private void printMainMenu() {
        System.out.println("\n========================================");
        System.out.println("    E-HANDELSAPPLIKATION");
        System.out.println("========================================");
        System.out.println("1. Produkthantering");
        System.out.println("2. Kundhantering");
        System.out.println("3. Kundvagn");
        System.out.println("4. Orderhantering");
        System.out.println("5. Rapporter");
        System.out.println("6. Systemhantering");
        System.out.println("7. Avsluta");
        System.out.println("========================================");
    }
}