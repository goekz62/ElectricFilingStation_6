package org.example;

import java.util.*;

public class CustomerManager {

    private final Map<String, Customer> customers = new LinkedHashMap<>();
    private int nextId = 1; // auto-increment counter

    // SYSTEM generates the ID
    public Customer createCustomer(String firstName, String lastName) {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("first name must not be empty");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("last name must not be empty");
        }
        String id = "C" + nextId++;
        Customer customer = new Customer(id, firstName, lastName);
        customers.put(id, customer);
        return customer;
    }

    public void updateCustomer(String id, String firstName, String lastName) {
        Customer existing = customers.get(id);
        if (existing == null) {
            throw new IllegalArgumentException("Customer not found: " + id);
        }
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("first name must not be empty");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("last name must not be empty");
        }
        customers.put(id, new Customer(id, firstName, lastName));
    }

    public void deleteCustomer(String id) {
        if (!customers.containsKey(id)) {
            throw new IllegalArgumentException("Customer not found: " + id);
        }
        customers.remove(id);
    }

    public Customer readCustomer(String id) {
        return customers.get(id);
    }

    public List<Customer> readAllCustomers() {
        return new ArrayList<>(customers.values());
    }

    public Customer findByName(String firstName, String lastName) {
        return customers.values().stream()
                .filter(c -> c.firstName().equalsIgnoreCase(firstName)
                        && c.lastName().equalsIgnoreCase(lastName))
                .findFirst()
                .orElse(null);
    }

}
