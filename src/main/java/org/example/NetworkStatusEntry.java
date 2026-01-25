package org.example;

import java.util.List;

public record NetworkStatusEntry(
        Location location,
        Tariff currentTariff,
        List<ChargingPoint> chargingPoints
) {
}
