package org.example;

public record Location(String id, String name, String address, Tariff tariff) {

    // convenience constructor so older MVP1 code still works
    public Location(String id, String name, String address) {
        this(id, name, address, null);
    }

    @Override
    public String toString() {
        return "Location{id='%s', name='%s', address='%s', tariff=%s}"
                .formatted(id, name, address, tariff);
    }
}
