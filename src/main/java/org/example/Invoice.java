package org.example;

import java.util.Date;

public record Invoice(
        String id,
        String customerId,
        ChargingSession session,
        Date createdAt,
        InvoiceStatus status
) {
    @Override
    public String toString() {
        return "Invoice{id='%s', customerId='%s', sessionId='%s', totalCost=%.2f, status=%s}"
                .formatted(id, customerId, session.id(), session.totalCost(), status);
    }
}
