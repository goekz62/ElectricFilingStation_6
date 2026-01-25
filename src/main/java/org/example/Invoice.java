package org.example;

import java.util.Date;
import java.util.List;

public record Invoice(
        String id,
        String customerId,
        List<ChargingSession> sessions,
        Date createdAt,
        InvoiceStatus status
)

{
    public double totalCost() {
        return sessions.stream()
                .mapToDouble(ChargingSession::totalCost)
                .sum();
    }

    @Override
    public String toString() {
        return "Invoice{id='%s', customerId='%s', sessions=%d, totalCost=%.2f, status=%s}"
                .formatted(id, customerId, sessions.size(), totalCost(), status);
    }
}
