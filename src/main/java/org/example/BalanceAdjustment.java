package org.example;

import java.util.Date;

public record BalanceAdjustment(
        String id,
        String customerId,
        double amount,
        Date dateTime,
        String reason
) {
    @Override
    public String toString() {
        return "BalanceAdjustment{id='%s', customerId='%s', amount=%.2f, dateTime=%s, reason='%s'}"
                .formatted(id, customerId, amount, dateTime, reason);
    }
}
