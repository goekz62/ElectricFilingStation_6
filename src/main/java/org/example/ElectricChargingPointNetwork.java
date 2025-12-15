package org.example;

import java.util.Locale;
import java.util.Scanner;

public class ElectricChargingPointNetwork {
    public static void main(String[] args) {
        LocationManager locationManager = new LocationManager();
        ChargingPointManager chargingPointManager = new ChargingPointManager();
        CustomerManager customerManager = new CustomerManager();

        // create
        locationManager.createLocation("L1", "Vienna Center", "Stephansplatz 1");
        locationManager.createLocation("L2", "Graz East", "Hauptstrasse 5");
        locationManager.createLocation("L3", "Graz North", "Hauptstrasse 7");

        chargingPointManager.createChargingPoint("CP1", "L1", ChargingType.AC);
        chargingPointManager.createChargingPoint("CP2", "L1", ChargingType.DC);
        chargingPointManager.createChargingPoint("CP3", "L2", ChargingType.DC);

        customerManager.createCustomer("Judith", "Muellner");
        customerManager.createCustomer("Katharina", "Weinberger");
        customerManager.createCustomer("Franz", "Steininger");

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\nType a command (show locations | show charging points | show customers | create location | create charging point | create customer | exit):");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Bye!");
                break;
            }

            // SHOW commands
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

                while (parts.length < 5) {
                    System.out.println("Usage: create location <id> <name_with_underscores> <address_with_underscores>");
                    System.out.print("Enter full create location command: ");
                    input = scanner.nextLine().trim();
                    parts = input.split("\\s+");
                }

                String id = parts[2];
                String name = parts[3].replace("_", " ");
                String address = parts[4].replace("_", " ");

                try {
                    locationManager.createLocation(id, name, address);
                    System.out.println("Created: " + locationManager.readLocation(id));
                } catch (IllegalArgumentException e) {
                    System.out.println("Error: " + e.getMessage());
                }

                continue;
            }

            //CREATE CHARGING POINT
            if (input.toLowerCase(Locale.ROOT).startsWith("create charging point")) {

                String[] parts = input.split("\\s+");

                while (parts.length < 6) {
                    System.out.println("Usage: create charging point <id> <locationId> <AC|DC>");
                    System.out.print("Enter full create charging point command: ");
                    input = scanner.nextLine().trim();
                    parts = input.split("\\s+");
                }

                String cpId = parts[3];
                String locationId = parts[4];
                String typeStr = parts[5].toUpperCase(Locale.ROOT);

                try {
                    ChargingType type = ChargingType.valueOf(typeStr);
                    chargingPointManager.createChargingPoint(cpId, locationId, type);
                    System.out.println("Created charging point: " + cpId);
                } catch (IllegalArgumentException e) {
                    System.out.println("Error: " + e.getMessage());
                }

                continue;
            }


            // CREATE CUSTOMER
            if (input.toLowerCase(Locale.ROOT).startsWith("create customer")) {

                String[] parts = input.split("\\s+");

                while (parts.length < 4) {
                    System.out.println("Usage: create customer <firstName> <lastName>");
                    System.out.print("Enter full create customer command: ");
                    input = scanner.nextLine().trim();
                    parts = input.split("\\s+");
                }

                String firstName = parts[2].replace("_", " ");
                String lastName = parts[3].replace("_", " ");

                Customer created = customerManager.createCustomer(firstName, lastName);
                System.out.println("Created: " + created);

                continue;
            }

            System.out.println("Unknown command. Try: show locations | show charging points | show customers | create location | create charging point | create customer | exit");
        }

        scanner.close();
    }
}
