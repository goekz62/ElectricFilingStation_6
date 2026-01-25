package org.example;

import java.util.*;

public class ChargingSessionManager {

    private final Map<String, ChargingSession> sessions = new LinkedHashMap<>();
    private int nextId = 1;

    // Create an ACTIVE session (endTime/cost/kWh unknown yet)
    public void createSession(String id, String customerId, String chargingPointId, Date startTime) {
        if (sessions.containsKey(id)) {
            throw new IllegalArgumentException("Session already exists: " + id);
        }
        sessions.put(id, new ChargingSession(
                id,
                customerId,
                chargingPointId,
                startTime,
                null,
                0.0,
                0.0,
                ChargingSessionStatus.ACTIVE
        ));
    }

    // End session and store final values
    public void endSession(String id, Date endTime, double kWhCharged, double totalCost) {
        ChargingSession s = sessions.get(id);
        if (s == null) {
            throw new IllegalArgumentException("Session not found: " + id);
        }
        sessions.put(id, new ChargingSession(
                s.id(),
                s.customerId(),
                s.chargingPointId(),
                s.startTime(),
                endTime,
                kWhCharged,
                totalCost,
                ChargingSessionStatus.FINISHED
        ));
    }

    // ✅ auto-generate id (S1, S2, ...) and start time = now
    public ChargingSession createSessionAutoId(String customerId, String chargingPointId) {
        String id;
        do {
            id = "S" + nextId++;
        } while (sessions.containsKey(id));

        createSession(id, customerId, chargingPointId, new Date());
        return sessions.get(id);
    }

    // ✅ NEW: Calculation result record (keeps it simple, no new "big" classes)
    public record Calculation(long durationMinutes, double kWhCharged, double totalCost) {}

    /**
     * ✅ NEW: Calculate live/finish values for a session based on:
     * - session start time
     * - endTime (e.g. new Date() for "now")
     * - charging point type (AC/DC) to pick the right tariff values
     *
     * IMPORTANT: Tariff meaning:
     *   tariff.pricePerKwhAC/DC = price per kWh
     *   tariff.pricePerMinuteAC/DC = parking price per minute
     */
    public Calculation calculateForSession(
            ChargingSession session,
            Date endTime,
            ChargingType type,
            Tariff tariff
    ) {
        if (session == null) throw new IllegalArgumentException("session must not be null");
        if (endTime == null) throw new IllegalArgumentException("endTime must not be null");
        if (type == null) throw new IllegalArgumentException("type must not be null");
        if (tariff == null) throw new IllegalArgumentException("tariff must not be null");

        if (session.startTime() == null) {
            throw new IllegalArgumentException("session.startTime must not be null");
        }

        long minutes = (endTime.getTime() - session.startTime().getTime()) / 60000;
        if (minutes < 0) minutes = 0;

        double pricePerKwh = (type == ChargingType.AC) ? tariff.pricePerKwhAC() : tariff.pricePerKwhDC();
        double pricePerMin = (type == ChargingType.AC) ? tariff.pricePerMinuteAC() : tariff.pricePerMinuteDC();

        double powerKw = (type == ChargingType.AC) ? 11.0 : 50.0;

        double hours = minutes / 60.0;
        double kWh = powerKw * hours;
        double cost = (kWh * pricePerKwh) + (minutes * pricePerMin);

        return new Calculation(minutes, kWh, cost);
    }

    /**
     * ✅ NEW: Finish session with auto-calculation, no CLI math.
     *
     * You pass the needed info from managers:
     * - chargingPointType (from ChargingPoint)
     * - tariff (from Location)
     */
    public ChargingSession finishSessionAutoCalculated(
            String sessionId,
            ChargingType chargingPointType,
            Tariff tariff
    ) {
        ChargingSession s = sessions.get(sessionId);
        if (s == null) {
            throw new IllegalArgumentException("Session not found: " + sessionId);
        }
        if (s.status() != ChargingSessionStatus.ACTIVE) {
            throw new IllegalArgumentException("Session is not ACTIVE: " + sessionId);
        }

        Date now = new Date();
        Calculation calc = calculateForSession(s, now, chargingPointType, tariff);

        endSession(sessionId, now, calc.kWhCharged(), calc.totalCost());
        return sessions.get(sessionId);
    }

    // Helper for tests: directly create finished session
    public void createFinishedSession(String id, String customerId, String chargingPointId,
                                      Date startTime, Date endTime,
                                      double kWhCharged, double totalCost,
                                      ChargingSessionStatus status) {

        if (sessions.containsKey(id)) {
            throw new IllegalArgumentException("Session already exists: " + id);
        }

        sessions.put(id, new ChargingSession(
                id,
                customerId,
                chargingPointId,
                startTime,
                endTime,
                kWhCharged,
                totalCost,
                status
        ));
    }

    public ChargingSession readSession(String id) {
        return sessions.get(id);
    }

    public List<ChargingSession> readAllSessions() {
        return new ArrayList<>(sessions.values());
    }

    public void deleteSessionsByCustomer(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("customerId must not be empty");
        }
        sessions.values().removeIf(session -> customerId.equals(session.customerId()));
    }
}
