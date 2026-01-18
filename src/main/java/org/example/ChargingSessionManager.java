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

    // ✅ NEW: auto-generate id (S1, S2, ...) and start time = now
    public ChargingSession createSessionAutoId(String customerId, String chargingPointId) {
        String id;
        do {
            id = "S" + nextId++;
        } while (sessions.containsKey(id));

        createSession(id, customerId, chargingPointId, new Date());
        return sessions.get(id);
    }

    // ✅ NEW: finish session using existing session data
    public ChargingSession finishSession(String sessionId, double kWhCharged, double totalCost) {
        ChargingSession s = sessions.get(sessionId);
        if (s == null) {
            throw new IllegalArgumentException("Session not found: " + sessionId);
        }
        if (s.status() != ChargingSessionStatus.ACTIVE) {
            throw new IllegalArgumentException("Session is not ACTIVE: " + sessionId);
        }

        endSession(sessionId, new Date(), kWhCharged, totalCost);
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
}
