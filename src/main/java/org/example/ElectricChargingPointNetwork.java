package org.example;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;

public class ElectricChargingPointNetwork {

    private static final DateTimeFormatter ISO_DT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private static Date parseIsoDateTime(String text) {
        LocalDateTime ldt = LocalDateTime.parse(text, ISO_DT);
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    }

    private static String money(double v) {
        return String.format(Locale.ROOT, "%.2f", v);
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
        locationManager.createLocation("L11", "Villach Ost", "Italiener Strasse 15");
        locationManager.createLocation("L12", "Leoben City", "Hauptplatz 6");
        locationManager.createLocation("L13", "Krems Altstadt", "Obere Landstrasse 22");


        locationManager.defineTariff("L1", 20, 15, 0.09, 0.01);        locationManager.defineTariff("L2", 11, 50, 0.06, 0.25);   // City AC slow / DC fast
        locationManager.defineTariff("L3", 22, 75, 0.08, 0.30);   // Shopping center
        locationManager.defineTariff("L4", 11, 100, 0.07, 0.35);  // Highway charger
        locationManager.defineTariff("L5", 22, 150, 0.09, 0.45);  // Fast DC hub
        locationManager.defineTariff("L6", 7.4, 50, 0.05, 0.22);  // Residential area
        locationManager.defineTariff("L7", 11, 75, 0.06, 0.28);   // Office parking
        locationManager.defineTariff("L8", 22, 120, 0.08, 0.40);  // Premium location
        locationManager.defineTariff("L9", 11, 60, 0.06, 0.26);   // Regional charger
        locationManager.defineTariff("L10", 22, 180, 0.10, 0.55); // Ultra-fast DC


        chargingPointManager.createChargingPoint("CP1", "L1", ChargingType.AC, ChargingPointStatus.AVAILABLE);
        chargingPointManager.createChargingPoint("CP2", "L1", ChargingType.DC, ChargingPointStatus.OCCUPIED);
        chargingPointManager.createChargingPoint("CP3", "L2", ChargingType.DC, ChargingPointStatus.OUT_OF_ORDER);
        chargingPointManager.createChargingPoint("CP4", "L2", ChargingType.AC, ChargingPointStatus.AVAILABLE);
        chargingPointManager.createChargingPoint("CP5", "L3", ChargingType.DC, ChargingPointStatus.AVAILABLE);
        chargingPointManager.createChargingPoint("CP6", "L4", ChargingType.AC, ChargingPointStatus.AVAILABLE);
        chargingPointManager.createChargingPoint("CP7", "L5", ChargingType.DC, ChargingPointStatus.OCCUPIED);
        chargingPointManager.createChargingPoint("CP8", "L6", ChargingType.AC, ChargingPointStatus.AVAILABLE);
        chargingPointManager.createChargingPoint("CP9", "L7", ChargingType.DC, ChargingPointStatus.OUT_OF_ORDER);
        chargingPointManager.createChargingPoint("CP10", "L8", ChargingType.AC, ChargingPointStatus.AVAILABLE);
        chargingPointManager.createChargingPoint("CP11", "L9", ChargingType.DC, ChargingPointStatus.AVAILABLE);
        chargingPointManager.createChargingPoint("CP12", "L10", ChargingType.AC, ChargingPointStatus.OCCUPIED);
        chargingPointManager.createChargingPoint("CP13", "L11", ChargingType.DC, ChargingPointStatus.AVAILABLE);

        // create customers (auto id: C1, C2, C3)
        Customer c1 = customerManager.createCustomer("Judith", "Muellner");
        customerManager.createCustomer("Katharina", "Weinberger");
        customerManager.createCustomer("Franz", "Steininger");
        customerManager.createCustomer("Nisa", "Yesillik");
        customerManager.createCustomer("Lukas", "Huber");
        customerManager.createCustomer("Anna", "Mayer");
        customerManager.createCustomer("Paul", "Gruber");
        customerManager.createCustomer("Sophie", "Wagner");
        customerManager.createCustomer("David", "Fischer");
        customerManager.createCustomer("Laura", "Bauer");
        customerManager.createCustomer("Max", "Schneider");


        // demo session
        chargingSessionManager.createFinishedSession(
                "S1",
                c1.id(),
                "CP1",
                parseIsoDateTime("2026-01-17T10:00"),
                parseIsoDateTime("2026-01-17T10:30"),
                12.5,
                7.80,
                ChargingSessionStatus.FINISHED
        );


        // demo topups + invoice
        invoiceManager.addTopUp("T1", c1.id(), 20.00, parseIsoDateTime("2026-01-17T09:00"));
        invoiceManager.addTopUp("T2", c1.id(), 15.00, parseIsoDateTime("2026-01-17T09:30"));
        ChargingSession s1 = chargingSessionManager.readSession("S1");
        invoiceManager.addInvoice("I1", c1.id(), s1, parseIsoDateTime("2026-01-17T10:30"), InvoiceStatus.PAID);

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
        System.out.println("""
                
                OPERATOR COMMANDS:
                show locations
                show charging points
                show customers
                show prices
                
                show sessions
                show session <sessionId>
                
                show billing <customerId>   (US-12)
                
                create location <id> <name_with_underscores> <address_with_underscores>
                create charging point <id> <locationId> <AC|DC> <AVAILABLE|OCCUPIED|OUT_OF_ORDER>
                define tariff <locationId> <kWhAC> <kWhDC> <minAC> <minDC>
                update tariff <locationId> <kWhAC> <kWhDC> <minAC> <minDC>
                back
                """);

        while (true) {
            System.out.print("operator> ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("back")) return;

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
                locationManager.readAllLocations().forEach(loc -> {
                    System.out.println(loc.id() + " - " + loc.name());
                    if (loc.tariff() == null) {
                        System.out.println("  Tariff: NOT DEFINED");
                    } else {
                        System.out.println("  Tariff: " + loc.tariff());
                    }
                });
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

                System.out.println("Customer: " + c);

                System.out.println("\nTop-Ups:");
                var topUps = invoiceManager.readTopUps(customerId);
                if (topUps.isEmpty()) System.out.println("  (none)");
                else topUps.forEach(t -> System.out.println("  " + t));

                System.out.println("\nInvoices:");
                var invoices = invoiceManager.readInvoices(customerId);
                if (invoices.isEmpty()) System.out.println("  (none)");
                else invoices.forEach(inv -> System.out.println("  " + inv));

                System.out.println("\nBalance: " + money(invoiceManager.readBalance(customerId)));
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

            if (input.toLowerCase(Locale.ROOT).startsWith("define tariff")) {
                String[] parts = input.split("\\s+");
                if (parts.length < 7) {
                    System.out.println("Usage: define tariff <locationId> <kWhAC> <kWhDC> <minAC> <minDC>");
                    continue;
                }
                try {
                    locationManager.defineTariff(
                            parts[2],
                            Double.parseDouble(parts[3]),
                            Double.parseDouble(parts[4]),
                            Double.parseDouble(parts[5]),
                            Double.parseDouble(parts[6])
                    );
                    System.out.println("Tariff defined for location " + parts[2] + ".");
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
                continue;
            }

            if (input.toLowerCase(Locale.ROOT).startsWith("update tariff")) {
                String[] parts = input.split("\\s+");
                if (parts.length < 7) {
                    System.out.println("Usage: update tariff <locationId> <kWhAC> <kWhDC> <minAC> <minDC>");
                    continue;
                }
                try {
                    locationManager.updateTariff(
                            parts[2],
                            Double.parseDouble(parts[3]),
                            Double.parseDouble(parts[4]),
                            Double.parseDouble(parts[5]),
                            Double.parseDouble(parts[6])
                    );
                    System.out.println("Tariff updated for location " + parts[2] + ".");
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
        System.out.println("""
                
                CUSTOMER COMMANDS:
                create customer <firstName> <lastName>
                login <firstName> <lastName>
                logout
                show locations
                show charging points
                show prices <locationId>
                topup <amount>
                show balance
                show invoices
                start charging session <chargingPointId>
                stop charging session <sessionId>
                show session
                back
                """);

        while (true) {
            System.out.print("customer> ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("back")) return;

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

            if (input.equalsIgnoreCase("show locations")) {
                locationManager.readAllLocations().forEach(System.out::println);
                continue;
            }

            if (input.equalsIgnoreCase("show charging points")) {
                chargingPointManager.readAllChargingPoints().forEach(System.out::println);
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
                if (loc.tariff() == null) {
                    System.out.println("  Tariff: NOT DEFINED");
                } else {
                    System.out.println("  Tariff: " + loc.tariff());
                }
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

                var invoices = invoiceManager.readInvoices(loggedInCustomer.id());

                if (invoices.isEmpty()) {
                    System.out.println("(no invoices)");
                } else {

                    // sort by session start time
                    invoices.sort(java.util.Comparator.comparing(i -> i.session().startTime()));

                    int itemNo = 1;
                    for (Invoice inv : invoices) {

                        ChargingSession s = inv.session();
                        ChargingPoint cp = chargingPointManager.readChargingPoint(s.chargingPointId());

                        String locationName = "UNKNOWN";
                        String mode = "UNKNOWN";

                        if (cp != null) {
                            mode = cp.type().name(); // AC / DC
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
                                Locale.ROOT,
                                "%d) invoice=%s | location=%s | cp=%s | mode=%s | duration=%d min | energy=%.2f kWh | price=%.2f | status=%s%n",
                                itemNo++,
                                inv.id(),
                                locationName,
                                s.chargingPointId(),
                                mode,
                                durationMin,
                                s.kWhCharged(),
                                s.totalCost(),
                                inv.status()
                        );
                    }
                }

                System.out.println("Current balance: " + money(invoiceManager.readBalance(loggedInCustomer.id())));
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
                if (loc == null || loc.tariff() == null) {
                    System.out.println("No tariff defined for location " + cp.locationId() + ". Cannot start charging.");
                    continue;
                }

                // Optional prepaid check (very simple): require at least 1€ balance
                double balance = invoiceManager.readBalance(loggedInCustomer.id());
                if (balance <= 0) {
                    System.out.println("Not enough balance. Please top up first.");
                    continue;
                }

                ChargingSession session = chargingSessionManager.createSessionAutoId(loggedInCustomer.id(), cpId);
                System.out.println("Session " + session.id() + " started at " + session.startTime() +
                        " on charging point " + cpId + " (" + cp.type() + ").");
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
                    Location loc = locationManager.readLocation(cp.locationId());

                    long minutes = (new Date().getTime() - s.startTime().getTime()) / 60000;

                    double kw = (cp.type() == ChargingType.AC) ? 11.0 : 50.0; // FIXED POWER
                    double hours = minutes / 60.0;
                    double kWh = kw * hours;

                    Tariff t = loc.tariff();
                    double cost = kWh * (cp.type() == ChargingType.AC ? t.pricePerKwhAC() : t.pricePerKwhDC())
                            + minutes * (cp.type() == ChargingType.AC ? t.pricePerMinuteAC() : t.pricePerMinuteDC());

                    System.out.println("Live:");
                    System.out.println("  durationMin=" + minutes);
                    System.out.println("  estKWh=" + String.format(Locale.ROOT, "%.2f", kWh));
                    System.out.println("  estCost=" + String.format(Locale.ROOT, "%.2f", cost));
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

                Location loc = locationManager.readLocation(cp.locationId());
                if (loc == null || loc.tariff() == null) {
                    System.out.println("Tariff missing. Cannot calculate costs.");
                    continue;
                }

                // ✅ calculate inside ChargingSessionManager (no CLI math)
                Tariff t = loc.tariff();
                ChargingSessionManager.Calculation calc =
                        chargingSessionManager.calculateForSession(s, new Date(), cp.type(), t);

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
                        chargingSessionManager.finishSessionAutoCalculated(sessionId, cp.type(), t);

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
