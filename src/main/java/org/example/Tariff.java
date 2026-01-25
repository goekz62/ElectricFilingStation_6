package org.example;

import java.time.LocalTime;

public record Tariff(
        String tariffId,
        double pricePerKwhAC,
        double pricePerKwhDC,
        double pricePerMinuteAC,
        double pricePerMinuteDC,
        String timePeriod,
        LocalTime startTime,
        LocalTime endTime
) {
    @Override
    public String toString() {
        return "Tariff{id='%s', pricePerKwhAC=%s, pricePerKwhDC=%s, parkingPerMinAC=%s, parkingPerMinDC=%s, timePeriod='%s', start=%s, end=%s}"
                .formatted(tariffId, pricePerKwhAC, pricePerKwhDC, pricePerMinuteAC, pricePerMinuteDC, timePeriod, startTime, endTime);
    }
}
