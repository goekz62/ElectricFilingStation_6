package org.example;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class ElectricChargingPointNetwork {

    private static final DateTimeFormatter ISO_DT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    private static final DateTimeFormatter DISPLAY_DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static Date parseIsoDateTime(String text) {
        LocalDateTime ldt = LocalDateTime.parse(text, ISO_DT);
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    }

    private static String formatDisplayDateTime(Date date) {
        if (date == null) {
            return "N/A";
        }
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .format(DISPLAY_DT);
    }

    private static String money(double v) {
        return String.format(Locale.ROOT, "%.2f", v);
    }

    private static void printNetworkStatus(LocationManager locationManager,
                                           ChargingPointManager chargingPointManager,
                                           Date atTime) {
        System.out.println("Network status at " + atTime + ":");
        for (NetworkStatusEntry entry : locationManager.readNetworkStatus(chargingPointManager, atTime)) {
            Location loc = entry.location();
            System.out.println(loc.id() + " - " + loc.name());
            if (entry.currentTariff() == null) {
                System.out.println("  Current Tariff: NOT DEFINED");
            } else {
                System.out.println("  Current Tariff: " + entry.currentTariff());
            }
            if (entry.chargingPoints().isEmpty()) {
                System.out.println("  Charging Points: (none)");
            } else {
                System.out.println("  Charging Points:");
                entry.chargingPoints().forEach(cp ->
                        System.out.println("    " + cp.id() + " | " + cp.type() + " | " + cp.status()));
            }
        }
    }

    private static void printOperatorMenu() {
        System.out.println("""
                
                OPERATOR MENU (type 'help' to see full command syntax)
                1) View data:    locations | charging points | customers | prices | network status
                2) Sessions:     show sessions | show session <id>
                3) Billing:      show billing <customerId>
                4) Manage:       create/update/delete location | charging point | status
                5) Pricing:      define/update tariff
                6) Filters:      filter charging points
                7) Balance:      correct balance <customerId> <amount> <reason>
                back
                """);
    }

    private static void printCustomerMenu() {
        System.out.println("""
                
                CUSTOMER MENU (type 'help' to see full command syntax)
                1) Account:      create customer | login | logout | delete account
                2) View data:    locations | charging points | prices <locationId> | network status
                3) Filters:      filter charging points
                4) Balance:      topup | show balance | show invoices
                5) Sessions:     start charging session <chargingPointId> | stop charging session <sessionId> | show session <sessionId>
                back
                """);
    }

    private static void printOperatorHelp() {
        System.out.println("""
                
                OPERATOR COMMANDS (full syntax)
                show locations
                show charging points
                show customers
                show prices
                show network status
                
                show sessions
                show session <sessionId>
                
                show billing <customerId>   (US-12)
                
                create location <id> <name_with_underscores> <address_with_underscores>
                update location <id> <name_with_underscores> <address_with_underscores>
                delete location <id>
                
                create charging point <id> <locationId> <AC|DC> <AVAILABLE|OCCUPIED|OUT_OF_ORDER>
                update charging point <id> <locationId> <AC|DC> <AVAILABLE|OCCUPIED|OUT_OF_ORDER>
                delete charging point <id>
                update charging point status <id> <AVAILABLE|OCCUPIED|OUT_OF_ORDER>
                
                define tariff <locationId> <kWhAC> <kWhDC> <parkingMinAC> <parkingMinDC> <timePeriod> <startHH:mm> <endHH:mm>
                update tariff <locationId> <kWhAC> <kWhDC> <parkingMinAC> <parkingMinDC> <timePeriod> [startHH:mm] [endHH:mm]
                
                filter charging points <locationId|*> <AC|DC|*> <AVAILABLE|OCCUPIED|OUT_OF_ORDER|*> <maxPricePerKwh|*>
                  examples:
                    filter charging points L1 AC AVAILABLE 0.30
                    filter charging points * DC * *
                
                correct balance <customerId> <amount> <reason_with_underscores>
                """);
    }

    private static void printCustomerHelp() {
        System.out.println("""
                
                CUSTOMER COMMANDS (full syntax)
                create customer <firstName> <lastName>
                login <firstName> <lastName>
                logout
                delete account
                
                show locations
                show charging points
                show prices <locationId>
                show network status
                
                filter charging points <locationId|*> <AC|DC|*> <AVAILABLE|OCCUPIED|OUT_OF_ORDER|*> <maxPricePerKwh|*>
                  examples:
                    filter charging points L1 AC AVAILABLE 0.30
                    filter charging points * DC * *
                
                topup <amount>
                show balance
                show invoices
                
                start charging session <chargingPointId>
                stop charging session <sessionId>
                show session <sessionId>
                """);
    }

    private static void printInvoiceStatement(Customer customer,
                                              InvoiceManager invoiceManager,
                                              ChargingPointManager chargingPointManager,
                                              LocationManager locationManager) {
        Date createdAt = new Date();
        System.out.println("============================================================");
        System.out.println("INVOICE / BILLING STATEMENT");
        if (customer != null) {
            System.out.println("Customer: " + customer.id() + " - " + customer.firstName() + " " + customer.lastName());
        }
        System.out.println("Created: " + formatDisplayDateTime(createdAt));
        System.out.println("============================================================");

        List<TopUp> topUps = invoiceManager.readTopUps(customer.id());
        topUps.sort(java.util.Comparator.comparing(TopUp::dateTime));
        System.out.println("\nTOP-UPS (sorted by date)");
        if (topUps.isEmpty()) {
            System.out.println("  (none)");
        } else {
            for (TopUp topUp : topUps) {
                System.out.println("  " + formatDisplayDateTime(topUp.dateTime()) + " | " + topUp.id() + " | +" + money(topUp.amount()) + " EUR");
            }
        }

        List<Invoice> invoices = invoiceManager.readInvoices(customer.id());
        invoices.sort(java.util.Comparator.comparing(inv -> inv.sessions().stream()
                .map(ChargingSession::startTime)
                .filter(java.util.Objects::nonNull)
                .min(Date::compareTo)
                .orElse(new Date(0))));

        System.out.println("\nBILLING ITEMS (sorted by session start)");
        if (invoices.isEmpty()) {
            System.out.println("  (none)");
        } else {
            for (Invoice invoice : invoices) {
                List<ChargingSession> sessions = new java.util.ArrayList<>(invoice.sessions());
                sessions.sort(java.util.Comparator.comparing(ChargingSession::startTime));
                for (ChargingSession session : sessions) {
                    ChargingPoint cp = chargingPointManager.readChargingPoint(session.chargingPointId());
                    String locationName = "UNKNOWN";
                    String locationId = "UNKNOWN";
                    String mode = "UNKNOWN";
                    if (cp != null) {
                        mode = cp.type().name();
                        Location loc = locationManager.readLocation(cp.locationId());
                        if (loc != null) {
                            locationName = loc.name();
                            locationId = loc.id();
                        }
                    }

                    long durationMin = 0;
                    if (session.startTime() != null && session.endTime() != null) {
                        durationMin = (session.endTime().getTime() - session.startTime().getTime()) / 60000;
                    }

                    double energyCost = session.kWhCharged() * session.pricePerKwh();
                    double parkingCost = durationMin * session.pricePerMinute();

                    System.out.println("  " + invoice.id() + " | " + formatDisplayDateTime(session.startTime()) +
                            " | " + locationName + " (" + locationId + ") | " + session.chargingPointId() + " | " + mode);
                    System.out.println("     Duration: " + durationMin + " min");
                    System.out.println("     Energy: " + String.format(Locale.ROOT, "%.2f", session.kWhCharged()) + " kWh");
                    System.out.println("     Prices (locked at start):");
                    System.out.println("        " + money(session.pricePerKwh()) + " EUR/kWh (" + mode + ")  |  " +
                            money(session.pricePerMinute()) + " EUR/min (" + mode + ")");
                    System.out.println("     Energy cost:  " + money(energyCost) + " EUR");
                    System.out.println("     Parking cost: " + money(parkingCost) + " EUR");
                    System.out.println("     TOTAL:        " + money(session.totalCost()) + " EUR");
                    System.out.println("     Status: " + invoice.status());
                }
            }
        }

        double topUpTotal = topUps.stream().mapToDouble(TopUp::amount).sum();
        double paidTotal = invoices.stream()
                .filter(i -> i.status() == InvoiceStatus.PAID)
                .mapToDouble(Invoice::totalCost)
                .sum();
        double correctionTotal = invoiceManager.readBalanceAdjustments(customer.id())
                .stream()
                .mapToDouble(BalanceAdjustment::amount)
                .sum();

        System.out.println("\n------------------------------------------------------------");
        System.out.println("Balance:");
        System.out.println("  Top-ups total:        " + money(topUpTotal) + " EUR");
        System.out.println("  Paid billing total:   -" + money(paidTotal) + " EUR");
        System.out.println("  Corrections total:     " + money(correctionTotal) + " EUR");
        System.out.println("  CURRENT BALANCE:      " + money(invoiceManager.readBalance(customer.id())) + " EUR");
        System.out.println("============================================================");
    }

    // ✅ "Login state" for the customer CLI
    private static Customer loggedInCustomer = null;

    public static void main(String[] args) {
        LocationManager locationManager = new LocationManager();
        ChargingPointManager chargingPointManager = new ChargingPointManager();
        CustomerManager customerManager = new CustomerManager();
        ChargingSessionManager chargingSessionManager = new ChargingSessionManager();
        InvoiceManager invoiceManager = new InvoiceManager();

        // Seed data
        locationManager.createLocation("L1", "Vienna Center", "Stephansplatz 1");
        locationManager.createLocation("L2", "Graz East", "Hauptstrasse 5");
        locationManager.createLocation("L3", "Graz North", "Hauptstrasse 7");
        locationManager.createLocation("L4", "Linz Center", "Landstrasse 12");
        locationManager.createLocation("L5", "Salzburg West", "Getreidegasse 8");
        locationManager.createLocation("L6", "Innsbruck Mitte", "Maria-Theresien-Strasse 3");
        locationManager.createLocation("L7", "Klagenfurt Süd", "Villacher Strasse 20");
        locationManager.createLocation("L8", "St. Pölten Zentrum", "Rathausplatz 1");
        locationManager.createLocation("L9", "Wels Nord", "Bahnhofstrasse 9");
        locationManager.createLocation("L10", "Bregenz Hafen", "Seestrasse 4");


        locationManager.defineTariff("L1", 0.20, 0.30, 0.09, 0.14, "DAY", LocalTime.of(6, 0), LocalTime.of(18, 0));
        locationManager.defineTariff("L1", 0.18, 0.28, 0.08, 0.12, "NIGHT", LocalTime.of(18, 0), LocalTime.of(6, 0));
        locationManager.defineTariff("L2", 0.11, 0.50, 0.06, 0.25, "DAY", LocalTime.of(6, 0), LocalTime.of(18, 0));
        locationManager.defineTariff("L2", 0.10, 0.45, 0.05, 0.22, "NIGHT", LocalTime.of(18, 0), LocalTime.of(6, 0));
        locationManager.defineTariff("L3", 0.22, 0.75, 0.08, 0.30, "PEAK", LocalTime.of(8, 0), LocalTime.of(12, 0));
        locationManager.defineTariff("L3", 0.18, 0.55, 0.07, 0.25, "OFF_PEAK", LocalTime.of(12, 0), LocalTime.of(8, 0));
        locationManager.defineTariff("L4", 0.11, 1.00, 0.07, 0.35, "DAY", LocalTime.of(7, 0), LocalTime.of(19, 0));
        locationManager.defineTariff("L4", 0.09, 0.90, 0.06, 0.30, "NIGHT", LocalTime.of(19, 0), LocalTime.of(7, 0));
        locationManager.defineTariff("L5", 0.22, 1.50, 0.09, 0.45, "DAY", LocalTime.of(6, 0), LocalTime.of(18, 0));
        locationManager.defineTariff("L5", 0.19, 1.20, 0.08, 0.40, "NIGHT", LocalTime.of(18, 0), LocalTime.of(6, 0));
        locationManager.defineTariff("L6", 0.07, 0.50, 0.05, 0.22, "DAY", LocalTime.of(6, 0), LocalTime.of(18, 0));
        locationManager.defineTariff("L6", 0.06, 0.45, 0.04, 0.20, "NIGHT", LocalTime.of(18, 0), LocalTime.of(6, 0));
        locationManager.defineTariff("L7", 0.11, 0.75, 0.06, 0.28, "DAY", LocalTime.of(7, 0), LocalTime.of(19, 0));
        locationManager.defineTariff("L7", 0.10, 0.70, 0.05, 0.25, "NIGHT", LocalTime.of(19, 0), LocalTime.of(7, 0));
        locationManager.defineTariff("L8", 0.22, 1.20, 0.08, 0.40, "DAY", LocalTime.of(6, 0), LocalTime.of(18, 0));
        locationManager.defineTariff("L8", 0.20, 1.05, 0.07, 0.35, "NIGHT", LocalTime.of(18, 0), LocalTime.of(6, 0));
        locationManager.defineTariff("L9", 0.11, 0.60, 0.06, 0.26, "DAY", LocalTime.of(6, 0), LocalTime.of(18, 0));
        locationManager.defineTariff("L9", 0.10, 0.55, 0.05, 0.22, "NIGHT", LocalTime.of(18, 0), LocalTime.of(6, 0));
        locationManager.defineTariff("L10", 0.22, 1.80, 0.10, 0.55, "DAY", LocalTime.of(6, 0), LocalTime.of(18, 0));
        locationManager.defineTariff("L10", 0.20, 1.60, 0.09, 0.50, "NIGHT", LocalTime.of(18, 0), LocalTime.of(6, 0));


        chargingPointManager.createChargingPoint("CP1", "L1", ChargingType.AC, ChargingPointStatus.AVAILABLE);
        chargingPointManager.createChargingPoint("CP2", "L1", ChargingType.DC, ChargingPointStatus.OCCUPIED);
        chargingPointManager.createChargingPoint("CP3", "L2", ChargingType.DC, ChargingPointStatus.OUT_OF_ORDER);
        chargingPointManager.createChargingPoint("CP4", "L2", ChargingType.AC, ChargingPointStatus.AVAILABLE);
        chargingPointManager.createChargingPoint("CP5", "L3", ChargingType.DC, ChargingPointStatus.AVAILABLE);
        chargingPointManager.createChargingPoint("CP6", "L3", ChargingType.AC, ChargingPointStatus.AVAILABLE);
        chargingPointManager.createChargingPoint("CP7", "L4", ChargingType.AC, ChargingPointStatus.AVAILABLE);
        chargingPointManager.createChargingPoint("CP8", "L4", ChargingType.DC, ChargingPointStatus.OCCUPIED);
        chargingPointManager.createChargingPoint("CP9", "L5", ChargingType.DC, ChargingPointStatus.AVAILABLE);
        chargingPointManager.createChargingPoint("CP10", "L5", ChargingType.AC, ChargingPointStatus.OUT_OF_ORDER);
        chargingPointManager.createChargingPoint("CP11", "L6", ChargingType.AC, ChargingPointStatus.AVAILABLE);
        chargingPointManager.createChargingPoint("CP12", "L6", ChargingType.DC, ChargingPointStatus.OCCUPIED);
        chargingPointManager.createChargingPoint("CP13", "L7", ChargingType.DC, ChargingPointStatus.OUT_OF_ORDER);
        chargingPointManager.createChargingPoint("CP14", "L7", ChargingType.AC, ChargingPointStatus.AVAILABLE);
        chargingPointManager.createChargingPoint("CP15", "L8", ChargingType.AC, ChargingPointStatus.AVAILABLE);
        chargingPointManager.createChargingPoint("CP16", "L8", ChargingType.DC, ChargingPointStatus.OCCUPIED);
        chargingPointManager.createChargingPoint("CP17", "L9", ChargingType.DC, ChargingPointStatus.AVAILABLE);
        chargingPointManager.createChargingPoint("CP18", "L9", ChargingType.AC, ChargingPointStatus.AVAILABLE);
        chargingPointManager.createChargingPoint("CP19", "L10", ChargingType.AC, ChargingPointStatus.OCCUPIED);
        chargingPointManager.createChargingPoint("CP20", "L10", ChargingType.DC, ChargingPointStatus.AVAILABLE);

        // create customers (auto id: C1..C5)
        Customer c1 = customerManager.createCustomer("Judith", "Muellner");
        customerManager.createCustomer("Katharina", "Weinberger");
        customerManager.createCustomer("Franz", "Steininger");
        customerManager.createCustomer("Nisa", "Yesillik");
        customerManager.createCustomer("Lukas", "Huber");


        // demo session
        chargingSessionManager.createFinishedSession(
                "S1",
                c1.id(),
                "CP1",
                parseIsoDateTime("2026-01-17T10:00"),
                parseIsoDateTime("2026-01-17T10:30"),
                12.5,
                7.80,
                ChargingSessionStatus.FINISHED,
                "T-L1-1",
                0.20,
                0.09,
                "DAY"
        );


        // demo topups + invoice
        invoiceManager.addTopUp("T1", c1.id(), 20.00, parseIsoDateTime("2026-01-17T09:00"));
        invoiceManager.addTopUp("T2", c1.id(), 15.00, parseIsoDateTime("2026-01-17T09:30"));
        ChargingSession s1 = chargingSessionManager.readSession("S1");
        invoiceManager.addInvoice("I1", c1.id(), List.of(s1), parseIsoDateTime("2026-01-17T10:30"), InvoiceStatus.PAID);

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\nWho are you? (operator | customer | exit)");
            String role = scanner.nextLine().trim().toLowerCase(Locale.ROOT);

            if (role.equals("exit")) {
                System.out.println("Bye!");
                break;
            }

            if (role.equals("operator")) {
                runOperatorCLI(scanner, locationManager, chargingPointManager, customerManager, chargingSessionManager, invoiceManager);
                continue;
            }

            if (role.equals("customer")) {
                runCustomerCLI(scanner, locationManager, chargingPointManager, customerManager, chargingSessionManager, invoiceManager);
                continue;
            }

            System.out.println("Unknown role.");
        }

        scanner.close();
    }

    // =========================================================
    // OPERATOR CLI
    // =========================================================
    private static void runOperatorCLI(
            Scanner scanner,
            LocationManager locationManager,
            ChargingPointManager chargingPointManager,
            CustomerManager customerManager,
            ChargingSessionManager chargingSessionManager,
            InvoiceManager invoiceManager
    ) {
        printOperatorMenu();

        while (true) {
            System.out.print("operator> ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("back")) return;
            if (input.equalsIgnoreCase("help")) {
                printOperatorHelp();
                continue;
            }

            if (input.equalsIgnoreCase("show locations")) {
                locationManager.readAllLocations().forEach(System.out::println);
                continue;
            }

            if (input.equalsIgnoreCase("show charging points")) {
                chargingPointManager.readAllChargingPoints().forEach(System.out::println);
                continue;
            }

            if (input.equalsIgnoreCase("show customers")) {
                customerManager.readAllCustomers().forEach(System.out::println);
                continue;
            }

            if (input.equalsIgnoreCase("show prices")) {
                Date now = new Date();
                locationManager.readAllLocations().forEach(loc -> {
                    System.out.println(loc.id() + " - " + loc.name());
                    Tariff current = locationManager.readTariffAt(loc.id(), now);
                    if (current == null) {
                        System.out.println("  Current Tariff: NOT DEFINED");
                    } else {
                        System.out.println("  Current Tariff: " + current);
                    }
                    if (!loc.tariffs().isEmpty()) {
                        System.out.println("  All tariffs:");
                        loc.tariffs().forEach(t -> System.out.println("    " + t));
                    }
                });
                continue;
            }

            if (input.equalsIgnoreCase("show network status")) {
                printNetworkStatus(locationManager, chargingPointManager, new Date());
                continue;
            }

            if (input.equalsIgnoreCase("show sessions")) {
                chargingSessionManager.readAllSessions().forEach(System.out::println);
                continue;
            }

            if (input.toLowerCase(Locale.ROOT).startsWith("show session")) {
                String[] parts = input.split("\\s+");
                if (parts.length < 3) {
                    System.out.println("Usage: show session <sessionId>");
                    continue;
                }
                ChargingSession s = chargingSessionManager.readSession(parts[2]);
                System.out.println(s == null ? "Session not found." : s);
                continue;
            }

            if (input.toLowerCase(Locale.ROOT).startsWith("show billing")) {
                String[] parts = input.split("\\s+");
                if (parts.length < 3) {
                    System.out.println("Usage: show billing <customerId>");
                    continue;
                }

                String customerId = parts[2];
                Customer c = customerManager.readCustomer(customerId);

                if (c == null) {
                    System.out.println("Customer not found: " + customerId);
                    continue;
                }

                printInvoiceStatement(c, invoiceManager, chargingPointManager, locationManager);
                continue;
            }

            if (input.toLowerCase(Locale.ROOT).startsWith("create location")) {
                String[] parts = input.split("\\s+");
                if (parts.length < 5) {
                    System.out.println("Usage: create location <id> <name_with_underscores> <address_with_underscores>");
                    continue;
                }

                try {
                    locationManager.createLocation(parts[2], parts[3].replace("_", " "), parts[4].replace("_", " "));
                    System.out.println("Location created.");
                } catch (IllegalArgumentException e) {
                    System.out.println("Error: " + e.getMessage());
                }
                continue;
            }

            if (input.toLowerCase(Locale.ROOT).startsWith("update location")) {
                String[] parts = input.split("\\s+");
                if (parts.length < 5) {
                    System.out.println("Usage: update location <id> <name_with_underscores> <address_with_underscores>");
                    continue;
                }
                try {
                    locationManager.updateLocation(parts[2], parts[3].replace("_", " "), parts[4].replace("_", " "));
                    System.out.println("Location updated.");
                } catch (IllegalArgumentException e) {
                    System.out.println("Error: " + e.getMessage());
                }
                continue;
            }

            if (input.toLowerCase(Locale.ROOT).startsWith("delete location")) {
                String[] parts = input.split("\\s+");
                if (parts.length < 3) {
                    System.out.println("Usage: delete location <id>");
                    continue;
                }
                try {
                    locationManager.deleteLocation(parts[2]);
                    System.out.println("Location deleted.");
                } catch (IllegalArgumentException e) {
                    System.out.println("Error: " + e.getMessage());
                }
                continue;
            }

            if (input.toLowerCase(Locale.ROOT).startsWith("create charging point")) {
                String[] parts = input.split("\\s+");
                if (parts.length < 7) {
                    System.out.println("Usage: create charging point <id> <locationId> <AC|DC> <AVAILABLE|OCCUPIED|OUT_OF_ORDER>");
                    continue;
                }

                try {
                    // ✅ IMPORTANT: parts[3] is the id because the command is 3 words: create charging point ...
                    chargingPointManager.createChargingPoint(
                            parts[3],
                            parts[4],
                            ChargingType.valueOf(parts[5].toUpperCase(Locale.ROOT)),
                            ChargingPointStatus.valueOf(parts[6].toUpperCase(Locale.ROOT))
                    );
                    System.out.println("Charging point created.");
                } catch (IllegalArgumentException e) {
                    System.out.println("Error: " + e.getMessage());
                }
                continue;
            }

            if (input.toLowerCase(Locale.ROOT).startsWith("update charging point status")) {
                String[] parts = input.split("\\s+");
                if (parts.length < 5) {
                    System.out.println("Usage: update charging point status <id> <AVAILABLE|OCCUPIED|OUT_OF_ORDER>");
                    continue;
                }
                try {
                    chargingPointManager.updateStatus(
                            parts[3],
                            ChargingPointStatus.valueOf(parts[4].toUpperCase(Locale.ROOT))
                    );
                    System.out.println("Charging point status updated.");
                } catch (IllegalArgumentException e) {
                    System.out.println("Error: " + e.getMessage());
                }
                continue;
            }

            if (input.toLowerCase(Locale.ROOT).startsWith("update charging point")) {
                String[] parts = input.split("\\s+");
                if (parts.length < 7) {
                    System.out.println("Usage: update charging point <id> <locationId> <AC|DC> <AVAILABLE|OCCUPIED|OUT_OF_ORDER>");
                    continue;
                }
                try {
                    chargingPointManager.updateChargingPoint(
                            parts[3],
                            parts[4],
                            ChargingType.valueOf(parts[5].toUpperCase(Locale.ROOT)),
                            ChargingPointStatus.valueOf(parts[6].toUpperCase(Locale.ROOT))
                    );
                    System.out.println("Charging point updated.");
                } catch (IllegalArgumentException e) {
                    System.out.println("Error: " + e.getMessage());
                }
                continue;
            }

            if (input.toLowerCase(Locale.ROOT).startsWith("delete charging point")) {
                String[] parts = input.split("\\s+");
                if (parts.length < 4) {
                    System.out.println("Usage: delete charging point <id>");
                    continue;
                }
                try {
                    chargingPointManager.deleteChargingPoint(parts[3]);
                    System.out.println("Charging point deleted.");
                } catch (IllegalArgumentException e) {
                    System.out.println("Error: " + e.getMessage());
                }
                continue;
            }

            if (input.toLowerCase(Locale.ROOT).startsWith("define tariff")) {
                String[] parts = input.split("\\s+");
                if (parts.length < 10) {
                    System.out.println("Usage: define tariff <locationId> <kWhAC> <kWhDC> <parkingMinAC> <parkingMinDC> <timePeriod> <startHH:mm> <endHH:mm>");
                    continue;
                }
                try {
                    locationManager.defineTariff(
                            parts[2],
                            Double.parseDouble(parts[3]),
                            Double.parseDouble(parts[4]),
                            Double.parseDouble(parts[5]),
                            Double.parseDouble(parts[6]),
                            parts[7],
                            LocalTime.parse(parts[8]),
                            LocalTime.parse(parts[9])
                    );
                    System.out.println("Tariff defined for location " + parts[2] + ".");
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
                continue;
            }

            if (input.toLowerCase(Locale.ROOT).startsWith("update tariff")) {
                String[] parts = input.split("\\s+");
                if (parts.length < 8) {
                    System.out.println("Usage: update tariff <locationId> <kWhAC> <kWhDC> <parkingMinAC> <parkingMinDC> <timePeriod> [startHH:mm] [endHH:mm]");
                    continue;
                }
                try {
                    String timePeriod = parts[7];
                    LocalTime start = parts.length >= 9 ? LocalTime.parse(parts[8]) : null;
                    LocalTime end = parts.length >= 10 ? LocalTime.parse(parts[9]) : null;
                    locationManager.updateTariff(
                            parts[2],
                            Double.parseDouble(parts[3]),
                            Double.parseDouble(parts[4]),
                            Double.parseDouble(parts[5]),
                            Double.parseDouble(parts[6]),
                            timePeriod,
                            start,
                            end
                    );
                    System.out.println("Tariff updated for location " + parts[2] + ".");
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
                continue;
            }

            if (input.toLowerCase(Locale.ROOT).startsWith("filter charging points")) {
                String[] parts = input.split("\\s+");
                if (parts.length < 6) {
                    System.out.println("Usage: filter charging points <locationId|*> <AC|DC|*> <AVAILABLE|OCCUPIED|OUT_OF_ORDER|*> <maxPricePerKwh|*>");
                    continue;
                }
                String locationId = parts[3].equals("*") ? null : parts[3];
                ChargingType type = parts[4].equals("*") ? null : ChargingType.valueOf(parts[4].toUpperCase(Locale.ROOT));
                ChargingPointStatus status = parts[5].equals("*") ? null : ChargingPointStatus.valueOf(parts[5].toUpperCase(Locale.ROOT));
                Double maxPrice = null;
                if (parts.length >= 7 && !parts[6].equals("*")) {
                    maxPrice = Double.parseDouble(parts[6]);
                }
                var filtered = chargingPointManager.filterChargingPoints(locationManager, locationId, type, status, maxPrice, new Date());
                if (filtered.isEmpty()) {
                    System.out.println("(no charging points found)");
                } else {
                    filtered.forEach(System.out::println);
                }
                continue;
            }

            if (input.toLowerCase(Locale.ROOT).startsWith("correct balance")) {
                String[] parts = input.split("\\s+");
                if (parts.length < 5) {
                    System.out.println("Usage: correct balance <customerId> <amount> <reason_with_underscores>");
                    continue;
                }
                try {
                    String adjustmentId = "ADJ" + System.currentTimeMillis();
                    String reason = parts[4].replace("_", " ");
                    invoiceManager.addBalanceAdjustment(
                            adjustmentId,
                            parts[2],
                            Double.parseDouble(parts[3]),
                            new Date(),
                            reason
                    );
                    System.out.println("Balance corrected for customer " + parts[2] + ".");
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
                continue;
            }

            System.out.println("Unknown operator command.");
        }
    }

    // =========================================================
    // CUSTOMER CLI (LOGIN + TOPUP + BALANCE + INVOICES + US-9)
    // =========================================================
    private static void runCustomerCLI(
            Scanner scanner,
            LocationManager locationManager,
            ChargingPointManager chargingPointManager,
            CustomerManager customerManager,
            ChargingSessionManager chargingSessionManager,
            InvoiceManager invoiceManager
    ) {
        printCustomerMenu();

        while (true) {
            System.out.print("customer> ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("back")) return;
            if (input.equalsIgnoreCase("help")) {
                printCustomerHelp();
                continue;
            }

            if (input.toLowerCase(Locale.ROOT).startsWith("create customer")) {
                String[] parts = input.split("\\s+");
                if (parts.length < 4) {
                    System.out.println("Usage: create customer <firstName> <lastName>");
                    continue;
                }
                Customer created = customerManager.createCustomer(parts[2].replace("_", " "), parts[3].replace("_", " "));
                System.out.println("Created: " + created);
                continue;
            }

            // ✅ LOGIN by first+last name
            if (input.toLowerCase(Locale.ROOT).startsWith("login")) {
                String[] parts = input.split("\\s+");
                if (parts.length < 3) {
                    System.out.println("Usage: login <firstName> <lastName>");
                    continue;
                }

                String fn = parts[1].replace("_", " ");
                String ln = parts[2].replace("_", " ");

                Customer found = customerManager.readAllCustomers().stream()
                        .filter(c -> c.firstName().equalsIgnoreCase(fn) && c.lastName().equalsIgnoreCase(ln))
                        .findFirst()
                        .orElse(null);

                if (found == null) {
                    System.out.println("No customer found for: " + fn + " " + ln);
                } else {
                    loggedInCustomer = found;
                    System.out.println("Logged in as: " + loggedInCustomer);
                }
                continue;
            }

            if (input.equalsIgnoreCase("logout")) {
                loggedInCustomer = null;
                System.out.println("Logged out.");
                continue;
            }

            if (input.equalsIgnoreCase("delete account")) {
                if (loggedInCustomer == null) {
                    System.out.println("Please login first.");
                    continue;
                }
                String customerId = loggedInCustomer.id();
                customerManager.deleteCustomer(customerId);
                chargingSessionManager.deleteSessionsByCustomer(customerId);
                invoiceManager.deleteCustomerData(customerId);
                loggedInCustomer = null;
                System.out.println("Account deleted.");
                continue;
            }

            if (input.equalsIgnoreCase("show locations")) {
                locationManager.readAllLocations().forEach(System.out::println);
                continue;
            }

            if (input.equalsIgnoreCase("show charging points")) {
                chargingPointManager.readAllChargingPoints().forEach(System.out::println);
                continue;
            }

            if (input.toLowerCase(Locale.ROOT).startsWith("filter charging points")) {
                String[] parts = input.split("\\s+");
                if (parts.length < 6) {
                    System.out.println("Usage: filter charging points <locationId|*> <AC|DC|*> <AVAILABLE|OCCUPIED|OUT_OF_ORDER|*> <maxPricePerKwh|*>");
                    continue;
                }
                String locationId = parts[3].equals("*") ? null : parts[3];
                ChargingType type = parts[4].equals("*") ? null : ChargingType.valueOf(parts[4].toUpperCase(Locale.ROOT));
                ChargingPointStatus status = parts[5].equals("*") ? null : ChargingPointStatus.valueOf(parts[5].toUpperCase(Locale.ROOT));
                Double maxPrice = null;
                if (parts.length >= 7 && !parts[6].equals("*")) {
                    maxPrice = Double.parseDouble(parts[6]);
                }
                var filtered = chargingPointManager.filterChargingPoints(locationManager, locationId, type, status, maxPrice, new Date());
                if (filtered.isEmpty()) {
                    System.out.println("(no charging points found)");
                } else {
                    filtered.forEach(System.out::println);
                }
                continue;
            }

            // US-7 customer: view price for a location
            if (input.toLowerCase(Locale.ROOT).startsWith("show prices")) {
                String[] parts = input.split("\\s+");
                if (parts.length < 3) {
                    System.out.println("Usage: show prices <locationId>");
                    continue;
                }

                String locationId = parts[2];
                Location loc = locationManager.readLocation(locationId);
                if (loc == null) {
                    System.out.println("Location not found: " + locationId);
                    continue;
                }

                System.out.println(loc.id() + " - " + loc.name());
                Tariff current = locationManager.readTariffAt(loc.id(), new Date());
                if (current == null) {
                    System.out.println("  Current Tariff: NOT DEFINED");
                } else {
                    System.out.println("  Current Tariff: " + current);
                }
                continue;
            }

            if (input.equalsIgnoreCase("show network status")) {
                printNetworkStatus(locationManager, chargingPointManager, new Date());
                continue;
            }

            // ✅ US-3 TopUp (requires login)
            if (input.toLowerCase(Locale.ROOT).startsWith("topup")) {
                if (loggedInCustomer == null) {
                    System.out.println("Please login first. (login <firstName> <lastName>)");
                    continue;
                }

                String[] parts = input.split("\\s+");
                if (parts.length < 2) {
                    System.out.println("Usage: topup <amount>");
                    continue;
                }

                try {
                    double amount = Double.parseDouble(parts[1]);
                    String topUpId = "T" + System.currentTimeMillis();
                    invoiceManager.addTopUp(topUpId, loggedInCustomer.id(), amount, new Date());

                    System.out.println("Top-up successful. New balance: " + money(invoiceManager.readBalance(loggedInCustomer.id())));
                } catch (NumberFormatException e) {
                    System.out.println("Amount must be a number (example: 20.00)");
                } catch (IllegalArgumentException e) {
                    System.out.println("Error: " + e.getMessage());
                }
                continue;
            }

            // ✅ Balance (requires login)
            if (input.equalsIgnoreCase("show balance")) {
                if (loggedInCustomer == null) {
                    System.out.println("Please login first. (login <firstName> <lastName>)");
                    continue;
                }
                System.out.println("Balance: " + money(invoiceManager.readBalance(loggedInCustomer.id())));
                continue;
            }

            // ✅ US-4 Invoices (requires login)
            if (input.equalsIgnoreCase("show invoices")) {
                if (loggedInCustomer == null) {
                    System.out.println("Please login first. (login <firstName> <lastName>)");
                    continue;
                }

                printInvoiceStatement(loggedInCustomer, invoiceManager, chargingPointManager, locationManager);
                continue;
            }
            if (input.toLowerCase(Locale.ROOT).startsWith("start charging session")) {
                if (loggedInCustomer == null) {
                    System.out.println("Please login first. Usage: login <firstName> <lastName>");
                    continue;
                }

                String[] parts = input.split("\\s+");
                if (parts.length < 4) {
                    System.out.println("Usage: start charging session <chargingPointId>");
                    continue;
                }

                String cpId = parts[3];
                ChargingPoint cp = chargingPointManager.readChargingPoint(cpId);
                if (cp == null) {
                    System.out.println("Charging point not found: " + cpId);
                    continue;
                }

                if (cp.status() != ChargingPointStatus.AVAILABLE) {
                    System.out.println("Charging point is not available: " + cp.status());
                    continue;
                }

                Location loc = locationManager.readLocation(cp.locationId());
                Tariff currentTariff = locationManager.readTariffAt(cp.locationId(), new Date());
                if (loc == null || currentTariff == null) {
                    System.out.println("No tariff defined for location " + cp.locationId() + ". Cannot start charging.");
                    continue;
                }

                // Optional prepaid check (very simple): require at least 1€ balance
                double balance = invoiceManager.readBalance(loggedInCustomer.id());
                if (balance <= 0) {
                    System.out.println("Not enough balance. Please top up first.");
                    continue;
                }

                double pricePerKwh = (cp.type() == ChargingType.AC) ? currentTariff.pricePerKwhAC() : currentTariff.pricePerKwhDC();
                double pricePerMinute = (cp.type() == ChargingType.AC) ? currentTariff.pricePerMinuteAC() : currentTariff.pricePerMinuteDC();
                ChargingSession session = chargingSessionManager.createSessionAutoId(
                        loggedInCustomer.id(),
                        cpId,
                        currentTariff.tariffId(),
                        pricePerKwh,
                        pricePerMinute,
                        currentTariff.timePeriod()
                );
                System.out.println("Session " + session.id() + " started at " + session.startTime() +
                        " on charging point " + cpId + " (" + cp.type() + "). Tariff=" + currentTariff.timePeriod());
                continue;
            }

            if (input.toLowerCase(Locale.ROOT).startsWith("show session")) {
                String[] parts = input.split("\\s+");
                if (parts.length < 3) {
                    System.out.println("Usage: show session <sessionId>");
                    continue;
                }

                String sessionId = parts[2];
                ChargingSession s = chargingSessionManager.readSession(sessionId);

                if (s == null) {
                    System.out.println("Session not found: " + sessionId);
                    continue;
                }

                System.out.println(s);

                // If ACTIVE -> show live values (duration + estimated kWh + estimated cost)
                if (s.status() == ChargingSessionStatus.ACTIVE) {
                    ChargingPoint cp = chargingPointManager.readChargingPoint(s.chargingPointId());
                    if (cp == null) {
                        System.out.println("Live: charging point missing.");
                        continue;
                    }

                    ChargingSessionManager.Calculation calc =
                            chargingSessionManager.calculateForSession(s, new Date(), cp.type(), s.pricePerKwh(), s.pricePerMinute());

                    System.out.println("Live:");
                    System.out.println("  durationMin=" + calc.durationMinutes());
                    System.out.println("  estKWh=" + String.format(Locale.ROOT, "%.2f", calc.kWhCharged()));
                    System.out.println("  estCost=" + String.format(Locale.ROOT, "%.2f", calc.totalCost()));
                }
                continue;
            }

            if (input.toLowerCase(Locale.ROOT).startsWith("stop charging session")) {
                if (loggedInCustomer == null) {
                    System.out.println("Please login first.");
                    continue;
                }

                String[] parts = input.split("\\s+");
                if (parts.length < 4) {
                    System.out.println("Usage: stop charging session <sessionId>");
                    continue;
                }

                String sessionId = parts[3];
                ChargingSession s = chargingSessionManager.readSession(sessionId);

                if (s == null) {
                    System.out.println("Session not found: " + sessionId);
                    continue;
                }

                if (!s.customerId().equals(loggedInCustomer.id())) {
                    System.out.println("You can only stop your own sessions.");
                    continue;
                }

                if (s.status() != ChargingSessionStatus.ACTIVE) {
                    System.out.println("Session already finished.");
                    continue;
                }

                ChargingPoint cp = chargingPointManager.readChargingPoint(s.chargingPointId());
                if (cp == null) {
                    System.out.println("Charging point not found: " + s.chargingPointId());
                    continue;
                }

                // ✅ calculate inside ChargingSessionManager (no CLI math)
                ChargingSessionManager.Calculation calc =
                        chargingSessionManager.calculateForSession(s, new Date(), cp.type(), s.pricePerKwh(), s.pricePerMinute());

                // prepaid check: balance must cover cost
                double balance = invoiceManager.readBalance(loggedInCustomer.id());
                if (balance < calc.totalCost()) {
                    System.out.println("Not enough balance to stop & bill. Please top up first.");
                    System.out.println("Needed: " + String.format(Locale.ROOT, "%.2f", calc.totalCost()) +
                            " | Balance: " + String.format(Locale.ROOT, "%.2f", balance));
                    continue;
                }

                // ✅ finish using auto-calculation (stores endTime/kWh/cost in session)
                Date now = new Date();
                ChargingSession finished =
                        chargingSessionManager.finishSessionAutoCalculated(sessionId, cp.type());

                System.out.println("Session finished: " + finished);
                System.out.println("Charged kWh=" + String.format(Locale.ROOT, "%.2f", finished.kWhCharged()) +
                        " totalCost=" + String.format(Locale.ROOT, "%.2f", finished.totalCost()));

                // optional: create an invoice automatically (simple MVP)
                String invoiceId = invoiceManager.addInvoiceAutoId(loggedInCustomer.id(), finished, now, InvoiceStatus.PAID);
                System.out.println("Invoice created: " + invoiceId);

                continue;
            }

            System.out.println("Unknown customer command.");
        }
    }
}
