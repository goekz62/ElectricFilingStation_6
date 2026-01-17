package org.example;

import java.util.*;

public class LocationManager {
    private final Map<String, Location> locations = new LinkedHashMap<>();

    public void createLocation(String id, String name, String address) {
        if (locations.containsKey(id)) {
            throw new IllegalArgumentException("Location already exists: " + id);
        }
        locations.put(id, new Location(id, name, address)); // tariff = null initially
    }

    public Location readLocation(String id) {
        return locations.get(id);
    }

    public List<Location> readAllLocations() {
        return new ArrayList<>(locations.values());
    }

    // MVP2: define pricing (Tariff) for a location
    public void defineTariff(String locationId,
                             double pricePerKwhAC,
                             double pricePerKwhDC,
                             double pricePerMinuteAC,
                             double pricePerMinuteDC) {

        Location loc = locations.get(locationId);
        if (loc == null) {
            throw new IllegalArgumentException("Location not found: " + locationId);
        }

        Tariff tariff = new Tariff("T-" + locationId, pricePerKwhAC, pricePerKwhDC, pricePerMinuteAC, pricePerMinuteDC);

        // record is immutable -> replace with updated copy
        Location updated = new Location(loc.id(), loc.name(), loc.address(), tariff);
        locations.put(locationId, updated);
    }
    public void updateTariff(String locationId,
                             double pricePerKwhAC,
                             double pricePerKwhDC,
                             double pricePerMinuteAC,
                             double pricePerMinuteDC) {

        Location loc = locations.get(locationId);
        if (loc == null) {
            throw new IllegalArgumentException("Location not found: " + locationId);
        }
        if (loc.tariff() == null) {
            throw new IllegalArgumentException("No tariff defined for location: " + locationId);
        }

        // keep same tariffId, only update prices
        Tariff old = loc.tariff();
        Tariff updatedTariff = new Tariff(
                old.tariffId(),
                pricePerKwhAC,
                pricePerKwhDC,
                pricePerMinuteAC,
                pricePerMinuteDC
        );

        Location updatedLocation = new Location(loc.id(), loc.name(), loc.address(), updatedTariff);
        locations.put(locationId, updatedLocation);
    }

    public Map<String, Tariff> readCurrentPricesByLocation() {
        Map<String, Tariff> result = new LinkedHashMap<>();
        for (Location loc : locations.values()) {
            result.put(loc.id(), loc.tariff()); // may be null if not defined yet
        }
        return result;
    }

}
