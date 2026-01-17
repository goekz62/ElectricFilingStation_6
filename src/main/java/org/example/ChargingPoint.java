package org.example;

public record ChargingPoint(
        String id,
        String locationId,
        ChargingType type,
        ChargingPointStatus status
) {
    @Override
    public String toString() {
        return "ChargingPoint{id='%s', locationId='%s', type=%s, status=%s}"
                .formatted(id, locationId, type, status);
    }
}
