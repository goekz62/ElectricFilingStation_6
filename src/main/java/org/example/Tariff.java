package org.example;

public record Tariff(
        String tariffId,
        double pricePerKwhAC,
        double pricePerKwhDC,
        double pricePerMinuteAC,
        double pricePerMinuteDC,
        String timePeriod
) {
    @Override
    public String toString() {
        return "Tariff{id='%s', pricePerKwhAC=%s, pricePerKwhDC=%s, parkingPerMinAC=%s, parkingPerMinDC=%s, timePeriod='%s'}"
                .formatted(tariffId, pricePerKwhAC, pricePerKwhDC, pricePerMinuteAC, pricePerMinuteDC, timePeriod);
    }
}
