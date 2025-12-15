package org.example;

import java.util.*;

public class CustomerManager {private final Map<String, Customer> customers = new LinkedHashMap<>();

    public void createCustomer(String id, String firstName, String lastName) {
        if (customers.containsKey(id)) {
            throw new IllegalArgumentException("Customer already exists: " + id);
        }
        customers.put(id, new Customer(id, firstName, lastName));
    }

    public Customer readCustomer(String id) {
        return customers.get(id);
    }

    public List<Customer> readAllCustomers() {
        return new ArrayList<>(customers.values());
    }
}
