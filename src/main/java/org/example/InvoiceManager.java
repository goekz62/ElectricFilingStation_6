package org.example;

import java.util.*;

public class InvoiceManager {

    private final Map<String, List<TopUp>> topUpsByCustomer = new LinkedHashMap<>();
    private final Map<String, List<Invoice>> invoicesByCustomer = new LinkedHashMap<>();

    public void addTopUp(String topUpId, String customerId, double amount, Date dateTime) {
        if (topUpId == null || topUpId.isBlank()) {
            throw new IllegalArgumentException("topUpId must not be empty");
        }
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("customerId must not be empty");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Top-up amount must be > 0");
        }
        if (dateTime == null) {
            throw new IllegalArgumentException("dateTime must not be null");
        }

        TopUp topUp = new TopUp(topUpId, customerId, amount, dateTime);
        topUpsByCustomer.computeIfAbsent(customerId, k -> new ArrayList<>()).add(topUp);
    }

    public void addInvoice(String invoiceId, String customerId, ChargingSession session, Date createdAt, InvoiceStatus status) {
        if (invoiceId == null || invoiceId.isBlank()) {
            throw new IllegalArgumentException("invoiceId must not be empty");
        }
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("customerId must not be empty");
        }
        if (session == null) {
            throw new IllegalArgumentException("session must not be null");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt must not be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }

        Invoice invoice = new Invoice(invoiceId, customerId, session, createdAt, status);
        invoicesByCustomer.computeIfAbsent(customerId, k -> new ArrayList<>()).add(invoice);
    }

    public List<TopUp> readTopUps(String customerId) {
        return new ArrayList<>(topUpsByCustomer.getOrDefault(customerId, List.of()));
    }

    public List<Invoice> readInvoices(String customerId) {
        return new ArrayList<>(invoicesByCustomer.getOrDefault(customerId, List.of()));
    }


    public double readBalance(String customerId) {
        double topUpSum = topUpsByCustomer.getOrDefault(customerId, List.of())
                .stream()
                .mapToDouble(TopUp::amount)
                .sum();

        double paidInvoiceSum = invoicesByCustomer.getOrDefault(customerId, List.of())
                .stream()
                .filter(i -> i.status() == InvoiceStatus.PAID)
                .mapToDouble(i -> i.session().totalCost())
                .sum();

        return topUpSum - paidInvoiceSum;
    }

    public boolean canStartCharging(String customerId, double expectedCost) {
        return readBalance(customerId) >= expectedCost;
    }
}
