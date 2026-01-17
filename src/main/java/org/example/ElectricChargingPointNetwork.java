package org.example;

import java.util.Locale;
import java.util.Scanner;

public class ElectricChargingPointNetwork {

    public static void main(String[] args) {
        LocationManager locationManager = new LocationManager();
        ChargingPointManager chargingPointManager = new ChargingPointManager();
        CustomerManager customerManager = new CustomerManager();

        // Seed data
        locationManager.createLocation("L1", "Vienna Center", "Stephansplatz 1");
        locationManager.createLocation("L2", "Graz East", "Hauptstrasse 5");
        locationManager.createLocation("L3", "Graz North", "Hauptstrasse 7");

        chargingPointManager.createChargingPoint("CP1", "L1", ChargingType.AC, ChargingPointStatus.AVAILABLE);
        chargingPointManager.createChargingPoint("CP2", "L1", ChargingType.DC, ChargingPointStatus.OCCUPIED);
        chargingPointManager.createChargingPoint("CP3", "L2", ChargingType.DC, ChargingPointStatus.OUT_OF_ORDER);

        customerManager.createCustomer("Judith", "Muellner");
        customerManager.createCustomer("Katharina", "Weinberger");
        customerManager.createCustomer("Franz", "Steininger");

        Scanner scanner = new Scanner(System.in);

        // --- ROLE SELECTION LOOP ---
        while (true) {
            System.out.println("\nWho are you? (operator | customer | exit)");
            String role = scanner.nextLine().trim().toLowerCase(Locale.ROOT);

            if (role.equals("exit")) {
                System.out.println("Bye!");
                break;
            }

            if (role.equals("operator")) {
                runOperatorCLI(scanner, locationManager, chargingPointManager, customerManager);
                continue;
            }

            if (role.equals("customer")) {
                runCustomerCLI(scanner, locationManager, chargingPointManager, customerManager);
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
            CustomerManager customerManager
    ) {
        System.out.println("""

                OPERATOR COMMANDS:
                show locations
                show charging points
                show customers
                create location <id> <name_with_underscores> <address_with_underscores>
                create charging point <id> <locationId> <AC|DC> <AVAILABLE|OCCUPIED|OUT_OF_ORDER>
                define tariff <locationId> <kWhAC> <kWhDC> <minAC> <minDC>
                update tariff <locationId> <kWhAC> <kWhDC> <minAC> <minDC>
                back
                """);

        while (true) {
            System.out.print("operator> ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("back")) {
                return;
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

            // CREATE LOCATION
            if (input.toLowerCase(Locale.ROOT).startsWith("create location")) {
                String[] parts = input.split("\\s+");
                if (parts.length < 5) {
                    System.out.println("Usage: create location <id> <name> <address>");
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
                } catch (NumberFormatException e) {
                    System.out.println("Error: prices must be numbers (example: 0.45 0.60 0.05 0.08)");
                } catch (IllegalArgumentException e) {
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
                } catch (NumberFormatException e) {
                    System.out.println("Error: prices must be numbers (example: 0.50 0.70 0.06 0.10)");
                } catch (IllegalArgumentException e) {
                    System.out.println("Error: " + e.getMessage());
                }
                continue;
            }

            System.out.println("Unknown operator command.");
        }
    }

    // =========================================================
    // CUSTOMER CLI
    // =========================================================
    private static void runCustomerCLI(
            Scanner scanner,
            LocationManager locationManager,
            ChargingPointManager chargingPointManager,
            CustomerManager customerManager
    ) {
        System.out.println("""

                CUSTOMER COMMANDS:
                show locations
                show charging points
                create customer <firstName> <lastName>
                back
                """);

        while (true) {
            System.out.print("customer> ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("back")) {
                return;
            }

            if (input.equalsIgnoreCase("show locations")) {
                locationManager.readAllLocations().forEach(System.out::println);
                continue;
            }

            if (input.equalsIgnoreCase("show charging points")) {
                chargingPointManager.readAllChargingPoints().forEach(System.out::println);
                continue;
            }

            if (input.toLowerCase(Locale.ROOT).startsWith("create customer")) {
                String[] parts = input.split("\\s+");
                if (parts.length < 4) {
                    System.out.println("Usage: create customer <firstName> <lastName>");
                    continue;
                }

                Customer created = customerManager.createCustomer(
                        parts[2].replace("_", " "),
                        parts[3].replace("_", " ")
                );
                System.out.println("Created: " + created);
                continue;
            }

            System.out.println("Unknown customer command.");
        }
    }
}
