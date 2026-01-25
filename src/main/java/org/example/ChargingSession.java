package org.example;

import java.util.Date;

public record ChargingSession(
        String id,
        String customerId,
        String chargingPointId,
        Date startTime,
        Date endTime,
        double kWhCharged,
        double totalCost,
        ChargingSessionStatus status,
        String tariffId,
        double pricePerKwh,
        double pricePerMinute,
        String timePeriod
) {
    @Override
    public String toString() {
        return "ChargingSession{id='%s', customerId='%s', chargingPointId='%s', startTime=%s, endTime=%s, kWhCharged=%.2f, totalCost=%.2f, status=%s, tariffId=%s, pricePerKwh=%.2f, pricePerMinute=%.2f, timePeriod=%s}"
                .formatted(id, customerId, chargingPointId, startTime, endTime, kWhCharged, totalCost, status, tariffId, pricePerKwh, pricePerMinute, timePeriod);
    }
}
