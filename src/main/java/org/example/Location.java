package org.example;

public record Location(String id, String name, String address) {
    @Override public String toString() {
        return "Location{id='%s', name='%s', address='%s'}".formatted(id, name, address);
    }
}
