package org.example;

import java.util.*;

public class ChargingPointManager {

    private final Map<String, ChargingPoint> points = new LinkedHashMap<>();

    public void createChargingPoint(
            String id,
            String locationId,
            ChargingType type,
            ChargingPointStatus status
    )

    {
        if (points.containsKey(id)) {
            throw new IllegalArgumentException("Charging point already exists: " + id);
        }
        points.put(id, new ChargingPoint(id, locationId, type, status));
    }

    public void updateChargingPoint(
            String id,
            String locationId,
            ChargingType type,
            ChargingPointStatus status
    ) {
        ChargingPoint existing = points.get(id);
        if (existing == null) {
            throw new IllegalArgumentException("Charging point not found: " + id);
        }
        if (locationId == null || locationId.isBlank()) {
            throw new IllegalArgumentException("locationId must not be empty");
        }
        if (type == null) {
            throw new IllegalArgumentException("type must not be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }
        points.put(id, new ChargingPoint(id, locationId, type, status));
    }

    public void deleteChargingPoint(String id) {
        if (!points.containsKey(id)) {
            throw new IllegalArgumentException("Charging point not found: " + id);
        }
        points.remove(id);
    }

    public ChargingPoint readChargingPoint(String id) {
        return points.get(id);
    }

    public List<ChargingPoint> readAllChargingPoints() {
        return new ArrayList<>(points.values());
    }

    public List<ChargingPoint> readByLocation(String locationId) {
        return points.values().stream()
                .filter(point -> point.locationId().equals(locationId))
                .toList();
    }

    public long countByLocation(String locationId) {
        return points.values().stream()
                .filter(p -> p.locationId().equals(locationId))
                .count();
    }

    public List<ChargingPoint> filterChargingPoints(
            LocationManager locationManager,
            String locationId,
            ChargingType type,
            ChargingPointStatus status,
            Double maxPricePerKwh,
            Date atTime
    ) {
        Date time = atTime == null ? new Date() : atTime;
        return points.values().stream()
                .filter(p -> locationId == null || p.locationId().equals(locationId))
                .filter(p -> type == null || p.type() == type)
                .filter(p -> status == null || p.status() == status)
                .filter(p -> {
                    if (maxPricePerKwh == null) return true;
                    if (locationManager == null) return false;
                    Tariff tariff = locationManager.readTariffAt(p.locationId(), time);
                    if (tariff == null) return false;
                    double price = (p.type() == ChargingType.AC)
                            ? tariff.pricePerKwhAC()
                            : tariff.pricePerKwhDC();
                    return price <= maxPricePerKwh;
                })
                .toList();
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
