package org.example;

import java.util.*;

public class InvoiceManager {

    private final Map<String, List<TopUp>> topUpsByCustomer = new LinkedHashMap<>();
    private final Map<String, List<Invoice>> invoicesByCustomer = new LinkedHashMap<>();

    public void addTopUp(String topUpId, String customerId, double amount, Date dateTime) {
        TopUp topUp = new TopUp(topUpId, customerId, amount, dateTime);
        topUpsByCustomer.computeIfAbsent(customerId, k -> new ArrayList<>()).add(topUp);
    }

    public void addInvoice(String invoiceId, String customerId, ChargingSession session, Date createdAt, InvoiceStatus status) {
        Invoice invoice = new Invoice(invoiceId, customerId, session, createdAt, status);
        invoicesByCustomer.computeIfAbsent(customerId, k -> new ArrayList<>()).add(invoice);
    }

    public List<TopUp> readTopUps(String customerId) {
        return new ArrayList<>(topUpsByCustomer.getOrDefault(customerId, List.of()));
    }

    public List<Invoice> readInvoices(String customerId) {
        return new ArrayList<>(invoicesByCustomer.getOrDefault(customerId, List.of()));
    }
}
