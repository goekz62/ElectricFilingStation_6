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

    public void updateLocation(String id, String name, String address) {
        Location existing = locations.get(id);
        if (existing == null) {
            throw new IllegalArgumentException("Location not found: " + id);
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Location name must not be empty");
        }
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("Location address must not be empty");
        }
        Location updated = new Location(id, name, address, existing.tariff());
        locations.put(id, updated);
    }

    public void deleteLocation(String id) {
        if (!locations.containsKey(id)) {
            throw new IllegalArgumentException("Location not found: " + id);
        }
        locations.remove(id);
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
        defineTariff(locationId, pricePerKwhAC, pricePerKwhDC, pricePerMinuteAC, pricePerMinuteDC, "ALL_DAY");
    }

    public void defineTariff(String locationId,
                             double pricePerKwhAC,
                             double pricePerKwhDC,
                             double pricePerMinuteAC,
                             double pricePerMinuteDC,
                             String timePeriod) {

        Location loc = locations.get(locationId);
        if (loc == null) {
            throw new IllegalArgumentException("Location not found: " + locationId);
        }
        if (timePeriod == null || timePeriod.isBlank()) {
            throw new IllegalArgumentException("timePeriod must not be empty");
        }

        Tariff tariff = new Tariff("T-" + locationId, pricePerKwhAC, pricePerKwhDC, pricePerMinuteAC, pricePerMinuteDC, timePeriod);

        // record is immutable -> replace with updated copy
        Location updated = new Location(loc.id(), loc.name(), loc.address(), tariff);
        locations.put(locationId, updated);
    }
    public void updateTariff(String locationId,
                             double pricePerKwhAC,
                             double pricePerKwhDC,
                             double pricePerMinuteAC,
                             double pricePerMinuteDC) {
        updateTariff(locationId, pricePerKwhAC, pricePerKwhDC, pricePerMinuteAC, pricePerMinuteDC, null);
    }

    public void updateTariff(String locationId,
                             double pricePerKwhAC,
                             double pricePerKwhDC,
                             double pricePerMinuteAC,
                             double pricePerMinuteDC,
                             String timePeriod) {

        Location loc = locations.get(locationId);
        if (loc == null) {
            throw new IllegalArgumentException("Location not found: " + locationId);
        }
        if (loc.tariff() == null) {
            throw new IllegalArgumentException("No tariff defined for location: " + locationId);
        }
        if (timePeriod != null && timePeriod.isBlank()) {
            throw new IllegalArgumentException("timePeriod must not be empty");
        }

        // keep same tariffId, only update prices
        Tariff old = loc.tariff();
        Tariff updatedTariff = new Tariff(
                old.tariffId(),
                pricePerKwhAC,
                pricePerKwhDC,
                pricePerMinuteAC,
                pricePerMinuteDC,
                timePeriod == null ? old.timePeriod() : timePeriod
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
