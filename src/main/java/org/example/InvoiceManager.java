package org.example;

import java.util.*;

public class InvoiceManager {

    private final Map<String, List<TopUp>> topUpsByCustomer = new LinkedHashMap<>();
    private final Map<String, List<Invoice>> invoicesByCustomer = new LinkedHashMap<>();

    // ✅ NEW: invoice ID counter (I1, I2, I3, ...)
    private int nextInvoiceNumber = 1;

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

    // ✅ unchanged (still supported if you want to pass an ID manually)
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

    // ✅ NEW: auto-generate next free invoice ID (I1, I2, I3, ...)
    public String addInvoiceAutoId(String customerId, ChargingSession session, Date createdAt, InvoiceStatus status) {
        String invoiceId = generateNextInvoiceId();
        addInvoice(invoiceId, customerId, session, createdAt, status);
        return invoiceId;
    }

    // ✅ NEW: generates I<number> and increments until it finds a free one
    private String generateNextInvoiceId() {
        while (invoiceIdExists("I" + nextInvoiceNumber)) {
            nextInvoiceNumber++;
        }
        return "I" + (nextInvoiceNumber++);
    }

    // ✅ NEW: checks across all customers
    private boolean invoiceIdExists(String invoiceId) {
        for (List<Invoice> invoices : invoicesByCustomer.values()) {
            for (Invoice inv : invoices) {
                if (inv.id().equals(invoiceId)) {
                    return true;
                }
            }
        }
        return false;
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

    private static void printInvoicesWithDetails(
            String customerId,
            InvoiceManager invoiceManager,
            CustomerManager customerManager,
            ChargingPointManager chargingPointManager,
            LocationManager locationManager
    ) {
        var invoices = invoiceManager.readInvoices(customerId);

        // sort by session start time
        invoices.sort(java.util.Comparator.comparing(i -> i.session().startTime()));

        if (invoices.isEmpty()) {
            System.out.println("(no invoices)");
            return;
        }

        int itemNo = 1;
        for (Invoice inv : invoices) {
            ChargingSession s = inv.session();

            ChargingPoint cp = chargingPointManager.readChargingPoint(s.chargingPointId());

            String chargingPointId = s.chargingPointId();
            String mode = "UNKNOWN";
            String locationName = "UNKNOWN";

            if (cp != null) {
                chargingPointId = cp.id();
                mode = cp.type().name(); // AC/DC

                Location loc = locationManager.readLocation(cp.locationId());
                if (loc != null) {
                    locationName = loc.name();
                }
            }

            long durationMin = 0;
            if (s.startTime() != null && s.endTime() != null) {
                durationMin = (s.endTime().getTime() - s.startTime().getTime()) / 60000;
            }

            System.out.printf(
                    java.util.Locale.ROOT,
                    "%d) invoice=%s | location=%s | cp=%s | mode=%s | duration=%d min | energy=%.2f kWh | price=%.2f | status=%s%n",
                    itemNo++,
                    inv.id(),
                    locationName,
                    chargingPointId,
                    mode,
                    durationMin,
                    s.kWhCharged(),
                    s.totalCost(),
                    inv.status()
            );
        }
    }

}
