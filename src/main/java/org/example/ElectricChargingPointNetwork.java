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

    public static void main(String[] args) {
        LocationManager locationManager = new LocationManager();
        ChargingPointManager chargingPointManager = new ChargingPointManager();
        CustomerManager customerManager = new CustomerManager();
        ChargingSessionManager chargingSessionManager = new ChargingSessionManager();
        InvoiceManager invoiceManager = new InvoiceManager();

        // ---------------- SEED DATA ----------------
        locationManager.createLocation("L1", "Vienna Center", "Stephansplatz 1");
        locationManager.createLocation("L2", "Graz East", "Hauptstrasse 5");
        locationManager.createLocation("L3", "Graz North", "Hauptstrasse 7");

        chargingPointManager.createChargingPoint("CP1", "L1", ChargingType.AC, ChargingPointStatus.AVAILABLE);
        chargingPointManager.createChargingPoint("CP2", "L1", ChargingType.DC, ChargingPointStatus.OCCUPIED);
        chargingPointManager.createChargingPoint("CP3", "L2", ChargingType.DC, ChargingPointStatus.OUT_OF_ORDER);

        // create customers (auto id: C1, C2, C3)
        Customer c1 = customerManager.createCustomer("Judith", "Muellner");
        customerManager.createCustomer("Katharina", "Weinberger");
        customerManager.createCustomer("Franz", "Steininger");

        locationManager.defineTariff("L1", 0.45, 0.60, 0.05, 0.08);
        locationManager.defineTariff("L2", 0.40, 0.55, 0.04, 0.07);

        // demo session for operator view (US-11)
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

        // seed billing demo (US-12 / customer invoices)
        invoiceManager.addTopUp("T1", c1.id(), 20.00, parseIsoDateTime("2026-01-17T09:00"));
        invoiceManager.addTopUp("T2", c1.id(), 15.00, parseIsoDateTime("2026-01-17T09:30"));

        ChargingSession s1 = chargingSessionManager.readSession("S1");
        invoiceManager.addInvoice("I1", c1.id(), s1, parseIsoDateTime("2026-01-17T10:30"), InvoiceStatus.PAID);
        // -------------------------------------------

        Scanner scanner = new Scanner(System.in);

        // ROLE SELECTION
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
                runCustomerCLI(scanner, locationManager, chargingPointManager, customerManager, invoiceManager);
                continue;
            }

            System.out.println("Unknown role. Type: operator | customer | exit");
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
        printOperatorHelp();

        while (true) {
            System.out.print("operator> ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("back")) return;
            if (input.equalsIgnoreCase("help")) { printOperatorHelp(); continue; }

            // SHOW
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

            // SESSIONS
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

            // BILLING (US-12)
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

                System.out.println("\nBalance: " + String.format(Locale.ROOT, "%.2f", invoiceManager.readBalance(customerId)));
                continue;
            }

            // CREATE LOCATION
            if (input.toLowerCase(Locale.ROOT).startsWith("create location")) {
                String[] parts = input.split("\\s+");
                if (parts.length < 5) {
                    System.out.println("Usage: create location <id> <name_with_underscores> <address_with_underscores>");
                    continue;
                }

                try {
                    locationManager.createLocation(
                            parts[2],
                            parts[3].replace("_", " "),
                            parts[4].replace("_", " ")
                    );
                    System.out.println("Location created.");
                } catch (IllegalArgumentException e) {
                    System.out.println("Error: " + e.getMessage());
                }
                continue;
            }

            // CREATE CHARGING POINT
            if (input.toLowerCase(Locale.ROOT).startsWith("create charging point")) {
                String[] parts = input.split("\\s+");
                if (parts.length < 7) {
                    System.out.println("Usage: create charging point <id> <locationId> <AC|DC> <AVAILABLE|OCCUPIED|OUT_OF_ORDER>");
                    continue;
                }

                try {
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

            // DEFINE TARIFF (US-6)
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

            // UPDATE TARIFF (US-7)
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

            System.out.println("Unknown operator command. Type 'help' to see commands.");
        }
    }

    private static void printOperatorHelp() {
        System.out.println("""

                OPERATOR COMMANDS:
                help
                back
                show locations
                show charging points
                show customers
                show prices
                show sessions
                show session <sessionId>
                show billing <customerId>
                create location <id> <name_with_underscores> <address_with_underscores>
                create charging point <id> <locationId> <AC|DC> <AVAILABLE|OCCUPIED|OUT_OF_ORDER>
                define tariff <locationId> <kWhAC> <kWhDC> <minAC> <minDC>
                update tariff <locationId> <kWhAC> <kWhDC> <minAC> <minDC>
                """);
    }

    // =========================================================
    // CUSTOMER CLI (WITH LOGIN)
    // =========================================================
    private static void runCustomerCLI(
            Scanner scanner,
            LocationManager locationManager,
            ChargingPointManager chargingPointManager,
            CustomerManager customerManager,
            InvoiceManager invoiceManager
    ) {
        Customer loggedIn = null;

        printCustomerHelp();

        while (true) {
            System.out.print("customer> ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("back")) return;
            if (input.equalsIgnoreCase("help")) { printCustomerHelp(); continue; }

            // CREATE CUSTOMER ALWAYS ALLOWED
            if (input.toLowerCase(Locale.ROOT).startsWith("create customer")) {
                String[] parts = input.split("\\s+");
                if (parts.length < 4) {
                    System.out.println("Usage: create customer <firstName> <lastName>");
                    continue;
                }
                Customer created = customerManager.createCustomer(parts[2], parts[3]);
                System.out.println("Created: " + created);
                continue;
            }

            // LOGIN
            if (input.toLowerCase(Locale.ROOT).startsWith("login")) {
                String[] parts = input.split("\\s+");
                if (parts.length < 3) {
                    System.out.println("Usage: login <firstName> <lastName>");
                    continue;
                }

                loggedIn = customerManager.readAllCustomers().stream()
                        .filter(c -> c.firstName().equalsIgnoreCase(parts[1]) &&
                                c.lastName().equalsIgnoreCase(parts[2]))
                        .findFirst()
                        .orElse(null);

                if (loggedIn == null) System.out.println("Customer not found.");
                else System.out.println("Logged in as: " + loggedIn);

                continue;
            }

            // LOGOUT
            if (input.equalsIgnoreCase("logout")) {
                loggedIn = null;
                System.out.println("Logged out.");
                continue;
            }

            // commands below need login
            if (loggedIn == null) {
                System.out.println("Please login first (login <firstName> <lastName>).");
                continue;
            }

            // SHOW LOCATIONS
            if (input.equalsIgnoreCase("show locations")) {
                locationManager.readAllLocations().forEach(System.out::println);
                continue;
            }

            // SHOW CHARGING POINTS
            if (input.equalsIgnoreCase("show charging points")) {
                chargingPointManager.readAllChargingPoints().forEach(System.out::println);
                continue;
            }

            // TOPUP (US-3)
            if (input.toLowerCase(Locale.ROOT).startsWith("topup")) {
                String[] parts = input.split("\\s+");
                if (parts.length < 2) {
                    System.out.println("Usage: topup <amount>");
                    continue;
                }

                try {
                    double amount = Double.parseDouble(parts[1]);
                    String topUpId = "T" + System.currentTimeMillis();
                    invoiceManager.addTopUp(topUpId, loggedIn.id(), amount, new Date());

                    System.out.println("Top-up successful.");
                    System.out.println("Balance: " + String.format(Locale.ROOT, "%.2f", invoiceManager.readBalance(loggedIn.id())));
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
                continue;
            }

            // BALANCE
            if (input.equalsIgnoreCase("show balance")) {
                System.out.println("Balance: " + String.format(Locale.ROOT, "%.2f", invoiceManager.readBalance(loggedIn.id())));
                continue;
            }

            // SHOW INVOICES (US-4)
            if (input.equalsIgnoreCase("show invoices")) {
                var invoices = invoiceManager.readInvoices(loggedIn.id());
                if (invoices.isEmpty()) System.out.println("(no invoices)");
                else invoices.forEach(System.out::println);

                System.out.println("Balance: " + String.format(Locale.ROOT, "%.2f", invoiceManager.readBalance(loggedIn.id())));
                continue;
            }
            if (input.toLowerCase(Locale.ROOT).startsWith("show price")) {
                String[] parts = input.split("\\s+");
                if (parts.length < 3) {
                    System.out.println("Usage: show price <chargingPointId>");
                    continue;
                }

                String cpId = parts[2];

                ChargingPoint cp = chargingPointManager.readAllChargingPoints().stream()
                        .filter(p -> p.id().equalsIgnoreCase(cpId))
                        .findFirst()
                        .orElse(null);

                if (cp == null) {
                    System.out.println("Charging point not found: " + cpId);
                    continue;
                }

                Location loc = locationManager.readLocation(cp.locationId());
                if (loc == null) {
                    System.out.println("Location not found: " + cp.locationId());
                    continue;
                }

                if (loc.tariff() == null) {
                    System.out.println("Tariff not defined for location " + loc.id());
                    continue;
                }

                Tariff t = loc.tariff();
                double priceKwh;
                double priceMin;

                if (cp.type() == ChargingType.AC) {
                    priceKwh = t.pricePerKwhAC();
                    priceMin = t.pricePerMinuteAC();
                } else {
                    priceKwh = t.pricePerKwhDC();
                    priceMin = t.pricePerMinuteDC();
                }

                System.out.println(loc.id() + " - " + loc.name());
                System.out.println("Charging point: " + cp.id() + " (" + cp.type() + ")");
                System.out.println("Tariff: kWh=" + String.format(Locale.ROOT, "%.2f", priceKwh)
                        + ", minute=" + String.format(Locale.ROOT, "%.2f", priceMin));
                continue;
            }

            System.out.println("Unknown customer command. Type 'help' to see commands.");
        }
    }

    private static void printCustomerHelp() {
        System.out.println("""

                CUSTOMER COMMANDS:
                help
                back
                create customer <firstName> <lastName>
                login <firstName> <lastName>
                logout
                show locations
                show charging points
                topup <amount>
                show balance
                show invoices
                """);
    }
}
