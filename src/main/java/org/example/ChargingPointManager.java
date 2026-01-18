package org.example;

import java.util.*;

public class ChargingPointManager {

    private final Map<String, ChargingPoint> points = new LinkedHashMap<>();

    public void createChargingPoint(
            String id,
            String locationId,
            ChargingType type,
            ChargingPointStatus status
    ) {
        if (points.containsKey(id)) {
            throw new IllegalArgumentException("Charging point already exists: " + id);
        }
        points.put(id, new ChargingPoint(id, locationId, type, status));
    }

    public ChargingPoint readChargingPoint(String id) {
        return points.get(id);
    }

    public List<ChargingPoint> readAllChargingPoints() {
        return new ArrayList<>(points.values());
    }

    public long countByLocation(String locationId) {
        return points.values().stream()
                .filter(p -> p.locationId().equals(locationId))
                .count();
    }

    // ✅ REQUIRED FOR US-9
    public void updateStatus(String chargingPointId, ChargingPointStatus newStatus) {
        ChargingPoint cp = points.get(chargingPointId);
        if (cp == null) {
            throw new IllegalArgumentException("Charging point not found: " + chargingPointId);
        }

        points.put(
                chargingPointId,
                new ChargingPoint(cp.id(), cp.locationId(), cp.type(), newStatus)
        );
    }
}
