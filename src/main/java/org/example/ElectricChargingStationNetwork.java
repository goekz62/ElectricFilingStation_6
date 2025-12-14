package org.example;

public class ElectricChargingStationNetwork {
    public static void main(String[] args) {
        LocationManager locationManager = new LocationManager();
        // create
        locationManager.createLocation("L1", "Vienna Center", "Stephansplatz 1");
        locationManager.createLocation("L2", "Graz East", "Hauptstrasse 5");

        // read + toString
        System.out.println("Locations:");
        locationManager.readAllLocations().forEach(System.out::println);


    }
}
