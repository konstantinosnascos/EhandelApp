package com.example;

import com.example.menu.MenuHandler;
import com.example.model.*;
import com.example.repository.*;
import com.example.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    private final Scanner scanner = new Scanner(System.in);

    // Repositories
    private final ProductRepository productRepository = new ProductRepository();
    private final CustomerRepository customerRepository = new CustomerRepository();
    private final CategoryRepository categoryRepository = new CategoryRepository();
    private final OrderRepository orderRepository = new OrderRepository();
    private final InventoryRepository inventoryRepository = new InventoryRepository();
    private final PaymentRepository paymentRepository = new PaymentRepository();

    // Services
    private final ProductService productService = new ProductService(productRepository);
    private final CustomerService customerService = new CustomerService(customerRepository);
    private final InventoryService inventoryService = new InventoryService(inventoryRepository);
    private final PaymentService paymentService = new PaymentService(paymentRepository);
    private final OrderService orderService = new OrderService(orderRepository, inventoryService, paymentService);
    private final CartService cartService = new CartService(inventoryService);
    private final CSVImportService csvImportService =
            new CSVImportService(orderService, productService, customerService, inventoryService);

    // Menu Handler
    private final MenuHandler menuHandler;

    public Application() {
        logger.info("Applikationen startar. Initierar testdata...");

        // Ladda testdata
        initializeTestData();

        // Skapa MenuHandler
        this.menuHandler = new MenuHandler(
                scanner,
                productService,
                customerService,
                cartService,
                orderService,
                inventoryService,
                csvImportService,
                productRepository,
                customerRepository,
                orderRepository,
                inventoryRepository
        );
    }

    private void initializeTestData() {
        try {
            // Skapa produkter
            Product p1 = productService.createProduct("LAPTOP001", "Dell XPS 13", "Bärbar dator 13 tum", 12999.00);
            Product p2 = productService.createProduct("MOUSE001", "Logitech MX Master", "Trådlös mus", 899.00);
            Product p3 = productService.createProduct("KEYBOARD001", "Keychron K2", "Mekaniskt tangentbord", 1299.00);
            Product p4 = productService.createProduct("MONITOR001", "Samsung 27 tum", "4K skärm", 3499.00);
            Product p5 = productService.createProduct("HEADSET001", "Sony WH-1000XM5", "Noise-cancelling hörlurar", 3999.00);
            Product p6 = productService.createProduct("CHAIR001", "Herman Miller Aeron", "Ergonomisk kontorsstol", 8999.00);
            Product p7 = productService.createProduct("DESK001", "IKEA Bekant", "Höj och sänkbart skrivbord", 4999.00);
            Product p8 = productService.createProduct("WEBCAM001", "Logitech Brio", "4K webbkamera", 1799.00);
            Product p9 = productService.createProduct("SPEAKER001", "Bose SoundLink", "Bluetooth högtalare", 1499.00);
            Product p10 = productService.createProduct("TABLET001", "iPad Air", "Surfplatta 10.9 tum", 7999.00);

            // Lägg till lager
            inventoryService.addStock(p1, 15);
            inventoryService.addStock(p2, 50);
            inventoryService.addStock(p3, 30);
            inventoryService.addStock(p4, 20);
            inventoryService.addStock(p5, 25);
            inventoryService.addStock(p6, 10);
            inventoryService.addStock(p7, 8);
            inventoryService.addStock(p8, 35);
            inventoryService.addStock(p9, 40);
            inventoryService.addStock(p10, 18);

            // Skapa kunder
            Customer c1 = customerService.createCustomer("anna.andersson@email.com", "Anna Andersson");
            Customer c2 = customerService.createCustomer("erik.svensson@email.com", "Erik Svensson");
            Customer c3 = customerService.createCustomer("maria.larsson@email.com", "Maria Larsson");
            Customer c4 = customerService.createCustomer("johan.berg@email.com", "Johan Berg");
            Customer c5 = customerService.createCustomer("linda.nilsson@email.com", "Linda Nilsson");

            logger.info("Testdata Small scenario startat: 10 produkter, 5 kunder");
            System.out.println("\n========================================");
            System.out.println("Testdata inlagd: 10 produkter, 5 kunder");            System.out.println("========================================");
            System.out.println("Scenario: Small");
            System.out.println("Produkter: 10");
            System.out.println("Kunder: 5");
            System.out.println("========================================");
            System.out.println("\nTips: Gå till Systemhantering för att importera");
            System.out.println("      Medium eller Large scenarios från CSV.");
            System.out.println("========================================\n");

        } catch (Exception e) {
            System.out.println("Ett fel uppstod vid inläsning av testdata: " + e.getMessage());
            logger.error("Fel vid initialisering av testdata", e);
        }
    }

    public void run() {
        System.out.println("\n========================================");
        System.out.println("  Välkommen till E-Handelsapplikationen!");
        System.out.println("========================================");

        menuHandler.runMainMenu();

        scanner.close();
        logger.info("Programmet avslutas. Scanner stängd.");
    }

    public static void main(String[] args) {
        Application app = new Application();
        app.run();
    }
}