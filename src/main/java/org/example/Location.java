package org.example;

import java.util.List;

public record Location(String id, String name, String address, List<Tariff> tariffs) {

    // convenience constructor so older MVP1 code still works
    public Location(String id, String name, String address) {
        this(id, name, address, List.of());
    }

    @Override
    public String toString() {
        return "Location{id='%s', name='%s', address='%s', tariffs=%d}"
                .formatted(id, name, address, tariffs == null ? 0 : tariffs.size());
    }
}
