package org.example;

public record ChargingPoint(String id, String locationId, ChargingType type) {
    @Override public String toString() {
        return "ChargingPoint{id='%s', locationId='%s', type=%s}".formatted(id, locationId, type);
    }
}
