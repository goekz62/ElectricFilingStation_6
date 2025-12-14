package org.example;
import java.util.*;

public class ChargingPointManager {
    private final Map<String, ChargingPoint> points = new LinkedHashMap<>();

    public void createChargingPoint(String id, String locationId, ChargingType type) {
        if (points.containsKey(id)) {
            throw new IllegalArgumentException("Charging point already exists: " + id);
        }
        points.put(id, new ChargingPoint(id, locationId, type));
    }

    public long countByLocation(String locationId) {
        return points.values().stream()
                .filter(p -> p.locationId().equals(locationId))
                .count();
    }

}
