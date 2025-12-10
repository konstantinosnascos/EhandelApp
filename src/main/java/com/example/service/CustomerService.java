package com.example.service;

import com.example.exception.CustomerNotFoundException;
import com.example.model.Customer;
import com.example.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CustomerService {
    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer createCustomer(String email, String name) {
        if (customerRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Kund med email " + email + " finns redan");
        }

        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Ogiltig email-adress");
        }

        Customer customer = new Customer(email, name);
        Customer saved = customerRepository.save(customer);
        logger.info("Kund skapad: {}", saved.getEmail());
        return saved;
    }

    public Customer getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new CustomerNotFoundException("Kund med email " + email + " hittades inte"));
    }

    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Kund med ID " + id + " hittades inte"));
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public List<Customer> searchCustomers(String keyword) {
        return customerRepository.findByNameContaining(keyword);
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    }

    public java.util.Optional<Customer> findByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    public Customer findOrCreate(String email, String name) {
        return customerRepository.findByEmail(email)
                .orElseGet(() -> {
                    return createCustomer(email, name);
                });
    }
}