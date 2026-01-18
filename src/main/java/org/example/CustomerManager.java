package org.example;

import java.util.*;

public class CustomerManager {

    private final Map<String, Customer> customers = new LinkedHashMap<>();
    private int nextId = 1; // auto-increment counter

    // SYSTEM generates the ID
    public Customer createCustomer(String firstName, String lastName) {
        String id = "C" + nextId++;
        Customer customer = new Customer(id, firstName, lastName);
        customers.put(id, customer);
        return customer;
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
