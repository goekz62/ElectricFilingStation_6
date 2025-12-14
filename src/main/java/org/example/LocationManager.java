package org.example;

import java.util.*;

public class LocationManager {
    private final Map<String, Location> locations = new LinkedHashMap<>();

    public void createLocation(String id, String name, String address) {
        if (locations.containsKey(id)) {
            throw new IllegalArgumentException("Location already exists: " + id);
        }
        locations.put(id, new Location(id, name, address));
    }

    public Location readLocation(String id) {
        return locations.get(id);
    }

    public List<Location> readAllLocations() {
        return new ArrayList<>(locations.values());
    }
}
