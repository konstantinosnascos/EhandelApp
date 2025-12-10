package com.example.menu;

import com.example.helper.InputHelper;
import com.example.repository.*;
import com.example.service.CSVImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemMenu {
    private static final Logger logger = LoggerFactory.getLogger(SystemMenu.class);

    private final InputHelper input;
    private final CSVImportService csvImportService;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;

    public SystemMenu(InputHelper input,
                      CSVImportService csvImportService,
                      ProductRepository productRepository,
                      CustomerRepository customerRepository,
                      OrderRepository orderRepository,
                      InventoryRepository inventoryRepository) {
        this.input = input;
        this.csvImportService = csvImportService;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.inventoryRepository = inventoryRepository;
    }

    public void run() {
        boolean running = true;
        while (running) {
            try {
                printMenu();
                int choice = input.getInt("Välj alternativ: ");
                switch (choice) {
                    case 1 -> csvImportService.importFromMenu();
                    case 2 -> showDataSummary();
                    case 3 -> resetAllData();
                    case 4 -> running = false;
                    default -> System.out.println("Ogiltigt val, försök igen!");
                }
            } catch (Exception e) {
                System.out.println("Ett fel uppstod: " + e.getMessage());
                logger.error("Fel i SystemMenu", e);
            }
        }
    }

    private void printMenu() {
        System.out.println("\n=== SYSTEMHANTERING ===");
        System.out.println("1. Importera från CSV");
        System.out.println("2. Visa datamängd");
        System.out.println("3. Rensa all data");
        System.out.println("4. Tillbaka till huvudmeny");
    }

    private void showDataSummary() {
        System.out.println("\n--- Aktuell datamängd ---");
        System.out.println("Produkter: " + productRepository.count());
        System.out.println("Kunder: " + customerRepository.count());
        System.out.println("Ordrar: " + orderRepository.count());
        logger.info("Visade datamängd");
    }

    private void resetAllData() {
        String confirm = input.getString("\nVarning! Detta raderar ALL data. Skriv 'RADERA' för att bekräfta: ");

        if (confirm.equals("RADERA")) {
            orderRepository.deleteAll();
            inventoryRepository.deleteAll();
            productRepository.deleteAll();
            customerRepository.deleteAll();

            System.out.println("All data raderad!");
            logger.warn("All data raderad via SystemMenu");
        } else {
            System.out.println("Radering avbruten.");
        }
    }
}