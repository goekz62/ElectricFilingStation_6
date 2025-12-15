package org.example;

public record Customer(String id, String firstName, String lastName) {
    @Override public String toString() {
        return "Customer{id='%s', firstName='%s', lastName='%s'}".formatted(id, firstName, lastName);
    }
}
