package com.example.repository;

import com.example.model.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class CustomerRepository {
    private static final Logger logger = LoggerFactory.getLogger(CustomerRepository.class);

    private final Map<Long, Customer> customers = new HashMap<>();
    private final Map<String, Customer> customersByEmail = new HashMap<>();
    private Long nextId = 1L;

    public Customer save(Customer customer) {
        if (customer.getId() == null) {
            customer.setId(nextId++);
            logger.info("Ny kund skapad med ID: {}", customer.getId());
        } else {
            logger.info("Kund uppdaterad med ID: {}", customer.getId());
        }

        customers.put(customer.getId(), customer);
        customersByEmail.put(customer.getEmail(), customer);
        return customer;
    }

    public Optional<Customer> findById(Long id) {
        return Optional.ofNullable(customers.get(id));
    }

    public Optional<Customer> findByEmail(String email) {
        return Optional.ofNullable(customersByEmail.get(email));
    }

    public List<Customer> findAll() {
        return new ArrayList<>(customers.values());
    }

    public List<Customer> findByNameContaining(String keyword) {
        return customers.values().stream()
                .filter(c -> c.getName().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }

    public boolean existsByEmail(String email) {
        return customersByEmail.containsKey(email);
    }

    public void delete(Customer customer) {
        customers.remove(customer.getId());
        customersByEmail.remove(customer.getEmail());
        logger.info("Kund raderad: {}", customer.getEmail());
    }

    public long count() {
        return customers.size();
    }

    public void deleteAll() {
        customers.clear();
        customersByEmail.clear();
        nextId = 1L;
        logger.info("Alla kunder raderade");
    }
}