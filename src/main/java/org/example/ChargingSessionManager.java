package org.example;

import java.util.*;

public class ChargingSessionManager {

    private final Map<String, ChargingSession> sessions = new LinkedHashMap<>();

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
}
