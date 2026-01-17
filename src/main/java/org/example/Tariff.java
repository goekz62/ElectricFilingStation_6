package org.example;

public record Tariff(
        String tariffId,
        double pricePerKwhAC,
        double pricePerKwhDC,
        double pricePerMinuteAC,
        double pricePerMinuteDC
) {
    @Override
    public String toString() {
        return "Tariff{id='%s', kWhAC=%s, kWhDC=%s, minAC=%s, minDC=%s}"
                .formatted(tariffId, pricePerKwhAC, pricePerKwhDC, pricePerMinuteAC, pricePerMinuteDC);
    }
}
