package org.example;

import java.util.Date;

public record TopUp(
        String id,
        String customerId,
        double amount,
        Date dateTime
) {
    @Override
    public String toString() {
        return "TopUp{id='%s', customerId='%s', amount=%.2f, dateTime=%s}"
                .formatted(id, customerId, amount, dateTime);
    }
}
