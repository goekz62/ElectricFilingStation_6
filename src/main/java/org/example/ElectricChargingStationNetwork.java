package org.example;

public class ElectricChargingStationNetwork {
    public static void main(String[] args) {
        LocationManager locationManager = new LocationManager();
        ChargingPointManager chargingPointManager = new ChargingPointManager();
        // create
        locationManager.createLocation("L1", "Vienna Center", "Stephansplatz 1");
        locationManager.createLocation("L2", "Graz East", "Hauptstrasse 5");
        locationManager.createLocation("L3", "Graz North", "Hauptstrasse 7");

        chargingPointManager.createChargingPoint("CP1", "L1", ChargingType.AC);
        chargingPointManager.createChargingPoint("CP2", "L1", ChargingType.DC);
        chargingPointManager.createChargingPoint("CP3", "L2", ChargingType.DC);

        // read + toString
        System.out.println("Locations:");
        locationManager.readAllLocations().forEach(System.out::println);






    }
}
