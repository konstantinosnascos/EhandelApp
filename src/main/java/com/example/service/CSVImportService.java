package com.example.service;

import com.example.model.*;
import com.example.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.*;
import com.example.model.OrderStatus;



public class CSVImportService {
    private static final Logger logger = LoggerFactory.getLogger(CSVImportService.class);

    private final OrderService orderService;
    private final ProductService productService;
    private final CustomerService customerService;
    private final InventoryService inventoryService;

    public CSVImportService(OrderService orderService,
                            ProductService productService,
                            CustomerService customerService,
                            InventoryService inventoryService) {
        this.orderService = orderService;
        this.productService = productService;
        this.customerService = customerService;
        this.inventoryService = inventoryService;
    }

    /**
     * Visa meny och importera vald fil
     */
    public void importFromMenu() {
        Path filePath = getFilePathFromUser();

        if (filePath == null) {
            logger.info("Ingen fil valdes.");
            return;
        }

        String fileName = filePath.getFileName().toString().toLowerCase();

        if (fileName.contains("product")) {
            importProducts(filePath);
        } else if (fileName.contains("customer")) {
            importCustomers(filePath);
        } else if (fileName.contains("order")) {
            importOrders(filePath);
        } else {
            System.out.println("Okänd filtyp. Filnamnet måste innehålla 'product' eller 'customer'.");
            logger.warn("Okänd filtyp: {}", fileName);
        }
    }

    /**
     * Visa meny och låt användaren välja fil
     */
    private Path getFilePathFromUser() {
        Path incomingDir = Paths.get("incoming");
        List<Path> validFiles = new ArrayList<>();

        if (!Files.exists(incomingDir)) {
            System.out.println("Mappen 'incoming/' finns inte. Skapar den...");
            try {
                Files.createDirectories(incomingDir);
                System.out.println("Mapp skapad. Lägg CSV-filer där och försök igen.");
            } catch (IOException e) {
                logger.error("Kunde inte skapa mapp incoming/", e);
            }
            return null;
        }

        // Samla alla CSV-filer
        try (Stream<Path> files = Files.list(incomingDir)) {
            validFiles = files
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().toLowerCase().endsWith(".csv"))
                    .sorted()
                    .toList();
        } catch (IOException e) {
            logger.error("Fel vid läsning av mappen incoming/", e);
            return null;
        }

        if (validFiles.isEmpty()) {
            System.out.println("Inga CSV-filer funna i 'incoming/'.");
            return null;
        }

        // Visa meny
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n==================================================");
        System.out.println("         TILLGÄNGLIGA FILER FÖR IMPORT");
        System.out.println("==================================================");

        for (int i = 0; i < validFiles.size(); i++) {
            Path file = validFiles.get(i);
            long lineCount = countLines(file);
            System.out.printf("[%d] %-30s (%d rader)%n",
                    i + 1,
                    file.getFileName(),
                    lineCount);
        }
        System.out.println("==================================================");

        // Be om val
        while (true) {
            System.out.printf("Välj en fil (1-%d) eller 0 för att avbryta: ", validFiles.size());
            if (!scanner.hasNextInt()) {
                System.out.println("Ange en siffra!");
                scanner.next();
                continue;
            }
            int choice = scanner.nextInt();
            if (choice == 0) {
                return null;
            }
            if (choice >= 1 && choice <= validFiles.size()) {
                return validFiles.get(choice - 1);
            }
            System.out.println("Ogiltigt val. Försök igen.");
        }
    }

    private long countLines(Path file) {
        try {
            return Files.lines(file).count();
        } catch (IOException e) {
            return 0;
        }
    }

    /**
     * Importera produkter från CSV
     * Format: sku;name;description;price;stock
     */
    public void importProducts(Path filePath) {
        logger.info("Startar import av produkter från: {}", filePath.getFileName());

        List<Product> products = new ArrayList<>();
        int lineNumber = 0;
        int skipped = 0;

        try (Stream<String> lines = Files.lines(filePath)) {
            Iterator<String> iterator = lines.iterator();

            // Skippa header om den finns
            if (iterator.hasNext()) {
                String firstLine = iterator.next();
                lineNumber++;
                if (!firstLine.toLowerCase().startsWith("sku")) {
                    // Första raden är inte header, parsa den
                    Product product = parseProductLine(firstLine, lineNumber);
                    if (product != null) {
                        products.add(product);
                    } else {
                        skipped++;
                    }
                }
            }

            // Läs resten av raderna
            while (iterator.hasNext()) {
                lineNumber++;
                String line = iterator.next();
                Product product = parseProductLine(line, lineNumber);
                if (product != null) {
                    products.add(product);
                } else {
                    skipped++;
                }
            }

        } catch (IOException e) {
            logger.error("Fel vid läsning av fil: {}", filePath, e);
            System.out.println("Fel vid läsning av fil: " + e.getMessage());
            return;
        }

        // Spara produkter
        int saved = 0;
        for (Product product : products) {
            try {
                Product savedProduct = productService.createProduct(
                        product.getSku(),
                        product.getName(),
                        product.getDescription(),
                        product.getPrice()
                );

                // Lägg till lager om det finns (sparas i Product temporärt)
                if (product.getCreatedAt() != null) { // Hack: använder createdAt för att lagra stock temporärt
                    // Extrahera stock från beskrivningen (vi måste fixa detta)
                }

                saved++;
            } catch (Exception e) {
                logger.warn("Kunde inte spara produkt {}: {}", product.getSku(), e.getMessage());
            }
        }

        System.out.printf("\nImport klar: %d produkter importerade, %d rader skippade%n", saved, skipped);
        logger.info("Import av produkter klar: {} sparade, {} skippade", saved, skipped);
    }

    private Product parseProductLine(String line, int lineNumber) {
        try {
            String[] parts = line.split(",", -1);
            if (parts.length < 4) {
                logger.warn("Rad {}: För få fält ({}). Förväntade minst 4.", lineNumber, parts.length);
                return null;
            }

            String sku = parts[0].trim();
            String name = parts[1].trim();
            String description = parts[2].trim();
            String priceStr = parts[3].trim();

            if (sku.isEmpty() || name.isEmpty() || priceStr.isEmpty()) {
                logger.warn("Rad {}: Tomma obligatoriska fält", lineNumber);
                return null;
            }

            double price = Double.parseDouble(priceStr);

            if (price < 0) {
                logger.warn("Rad {}: Negativt pris ({})", lineNumber, price);
                return null;
            }

            Product product = new Product(sku, name, description, price);

            // Om det finns stock-kolumn (kolumn 5)
            if (parts.length >= 5 && !parts[4].trim().isEmpty()) {
                try {
                    int stock = Integer.parseInt(parts[4].trim());
                    // Vi måste spara stock separat via InventoryService
                    // För nu, lägg till det efter att produkten skapats
                } catch (NumberFormatException e) {
                    logger.warn("Rad {}: Ogiltigt lagervärde", lineNumber);
                }
            }

            return product;

        } catch (NumberFormatException e) {
            logger.warn("Rad {}: Ogiltigt nummerformat", lineNumber);
            return null;
        } catch (Exception e) {
            logger.warn("Rad {}: Okänt fel vid parsning", lineNumber);
            return null;
        }
    }

    /**
     * Importera kunder från CSV
     * Format: email;name
     */
    public void importCustomers(Path filePath) {
        logger.info("Startar import av kunder från: {}", filePath.getFileName());

        List<Customer> customers = new ArrayList<>();
        int lineNumber = 0;
        int skipped = 0;

        try (Stream<String> lines = Files.lines(filePath)) {
            Iterator<String> iterator = lines.iterator();

            // Skippa header om den finns
            if (iterator.hasNext()) {
                String firstLine = iterator.next();
                lineNumber++;
                if (!firstLine.toLowerCase().startsWith("email")) {
                    Customer customer = parseCustomerLine(firstLine, lineNumber);
                    if (customer != null) {
                        customers.add(customer);
                    } else {
                        skipped++;
                    }
                }
            }

            while (iterator.hasNext()) {
                lineNumber++;
                String line = iterator.next();
                Customer customer = parseCustomerLine(line, lineNumber);
                if (customer != null) {
                    customers.add(customer);
                } else {
                    skipped++;
                }
            }

        } catch (IOException e) {
            logger.error("Fel vid läsning av fil: {}", filePath, e);
            System.out.println("Fel vid läsning av fil: " + e.getMessage());
            return;
        }

        // Spara kunder
        int saved = 0;
        for (Customer customer : customers) {
            try {
                customerService.createCustomer(customer.getEmail(), customer.getName());
                saved++;
            } catch (Exception e) {
                logger.warn("Kunde inte spara kund {}: {}", customer.getEmail(), e.getMessage());
            }
        }

        System.out.printf("\nImport klar: %d kunder importerade, %d rader skippade%n", saved, skipped);
        logger.info("Import av kunder klar: {} sparade, {} skippade", saved, skipped);
    }

    private Customer parseCustomerLine(String line, int lineNumber) {
        try {
            String[] parts = line.split(",", -1);
            if (parts.length < 2) {
                logger.warn("Rad {}: För få fält ({}). Förväntade 2.", lineNumber, parts.length);
                return null;
            }

            String email = parts[0].trim();
            String name = parts[1].trim();

            if (email.isEmpty() || name.isEmpty()) {
                logger.warn("Rad {}: Tomma fält", lineNumber);
                return null;
            }

            if (!email.contains("@")) {
                logger.warn("Rad {}: Ogiltig email ({})", lineNumber, email);
                return null;
            }

            return new Customer(email, name);

        } catch (Exception e) {
            logger.warn("Rad {}: Okänt fel vid parsning", lineNumber);
            return null;
        }
    }

    public void importOrders(Path filePath) {
        logger.info("Startar import av order från: {}", filePath.getFileName());

        List<Order> orders = new ArrayList<>();
        int lineNumber = 0;
        int skipped = 0;

        try (Stream<String> lines = Files.lines(filePath)) {
            Iterator<String> iterator = lines.iterator();

            // ── Header? ────────────────────────────────
            if (iterator.hasNext()) {
                String first = iterator.next();
                lineNumber++;
                if (!first.toLowerCase().startsWith("ordernumber")) {
                    Order order = parseOrderLine(first, lineNumber);
                    if (order != null) orders.add(order); else skipped++;
                }
            }

            // ── Övriga rader ───────────────────────────
            while (iterator.hasNext()) {
                lineNumber++;
                String line = iterator.next();
                Order order = parseOrderLine(line, lineNumber);
                if (order != null) orders.add(order); else skipped++;
            }

        } catch (IOException e) {
            logger.error("Fel vid läsning av fil: {}", filePath, e);
            System.out.println("Fel vid läsning av fil: " + e.getMessage());
            return;
        }

        // ── Spara ─────────────────────────────────────
        int saved = 0;
        for (Order o : orders) {
            try {
                orderService.createImportedOrder(o);   // egen metod du skriver
                saved++;
            } catch (Exception e) {
                logger.warn("Kunde inte spara order {}: {}",  o.getId(), e.getMessage());
            }
        }

        System.out.printf("\nImport klar: %d order importerade, %d rader skippade%n",
                saved, skipped);
        logger.info("Import av order klar: {} sparade, {} skippade", saved, skipped);
    }

    private Order parseOrderLine(String line, int lineNumber) {
        try {
            String[] parts = line.split(",", 5);   // orderNumber,email,status,createdAt,items
            if (parts.length < 5) {
                logger.warn("Rad {}: För få fält ({}). Förväntade 5.", lineNumber, parts.length);
                return null;
            }

            // 1. Rada ut kolumner
            /* String orderNo = */ parts[0].trim();   // vi sparar inte orderNumber i modellen
            String email     = parts[1].trim();
            String statusStr = parts[2].trim();
            String createdAt = parts[3].trim();
            String itemsStr  = parts[4].trim();

            if (email.isEmpty() || itemsStr.isEmpty()) {
                logger.warn("Rad {}: Tomma fält", lineNumber);
                return null;
            }

            // 2. Kund
            Customer cust = customerService.findByEmail(email).orElse(null);
            if (cust == null) {
                logger.warn("Rad {}: Okänd kund: {}", lineNumber, email);
                return null;
            }

            OrderStatus status;
            try {
                status = OrderStatus.valueOf(statusStr);
            } catch (IllegalArgumentException ex) {
                logger.warn("Rad {}: Ogiltig status: {}", lineNumber, statusStr);
                return null;
            }

            // 3. Bygg Order
            Order order = new Order();
            order.setCustomer(cust);
            order.setStatus(status);
            order.setCreatedAt(LocalDateTime.parse(createdAt));

            List<OrderItem> orderItems = new ArrayList<>();
            double total = 0.0;

            for (String part : itemsStr.split("\\|")) {
                String[] kv = part.split(":");
                if (kv.length != 2) {
                    logger.warn("Rad {}: Felaktigt item-format: {}", lineNumber, part);
                    return null;
                }
                String sku = kv[0].trim();
                int qty    = Integer.parseInt(kv[1].trim());

                Product prod = productService.findBySku(sku).orElse(null);
                if (prod == null) {
                    logger.warn("Rad {}: Okänd produkt-SKU: {}", lineNumber, sku);
                    return null;
                }

                OrderItem oi = new OrderItem(prod, qty);  // konstruktör finns redan
                orderItems.add(oi);
                total += oi.getLineTotal();
            }

            order.setItems(orderItems);
            order.setTotal(total);

            return order;

        } catch (Exception e) {
            logger.warn("Rad {}: Okänt fel vid parsning ({})", lineNumber, e.getMessage());
            return null;
        }
    }
}