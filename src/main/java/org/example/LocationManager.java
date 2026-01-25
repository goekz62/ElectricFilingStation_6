package org.example;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

public class LocationManager {
    private static final LocalTime DEFAULT_START = LocalTime.of(0, 0);
    private static final LocalTime DEFAULT_END = LocalTime.of(23, 59);

    private final Map<String, Location> locations = new LinkedHashMap<>();

    public void createLocation(String id, String name, String address) {
        if (locations.containsKey(id)) {
            throw new IllegalArgumentException("Location already exists: " + id);
        }
        locations.put(id, new Location(id, name, address)); // tariffs empty initially
    }

    public void updateLocation(String id, String name, String address) {
        Location existing = locations.get(id);
        if (existing == null) {
            throw new IllegalArgumentException("Location not found: " + id);
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Location name must not be empty");
        }
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("Location address must not be empty");
        }
        Location updated = new Location(id, name, address, existing.tariffs());
        locations.put(id, updated);
    }

    public void deleteLocation(String id) {
        if (!locations.containsKey(id)) {
            throw new IllegalArgumentException("Location not found: " + id);
        }
        locations.remove(id);
    }

    public Location readLocation(String id) {
        return locations.get(id);
    }

    public List<Location> readAllLocations() {
        return new ArrayList<>(locations.values());
    }

    // MVP2: define pricing (Tariff) for a location
    public void defineTariff(String locationId,
                             double pricePerKwhAC,
                             double pricePerKwhDC,
                             double pricePerMinuteAC,
                             double pricePerMinuteDC) {
        defineTariff(locationId, pricePerKwhAC, pricePerKwhDC, pricePerMinuteAC, pricePerMinuteDC, "ALL_DAY", DEFAULT_START, DEFAULT_END);
    }

    public void defineTariff(String locationId,
                             double pricePerKwhAC,
                             double pricePerKwhDC,
                             double pricePerMinuteAC,
                             double pricePerMinuteDC,
                             String timePeriod) {
        defineTariff(locationId, pricePerKwhAC, pricePerKwhDC, pricePerMinuteAC, pricePerMinuteDC, timePeriod, DEFAULT_START, DEFAULT_END);
    }

    public void defineTariff(String locationId,
                             double pricePerKwhAC,
                             double pricePerKwhDC,
                             double pricePerMinuteAC,
                             double pricePerMinuteDC,
                             String timePeriod,
                             LocalTime startTime,
                             LocalTime endTime) {

        Location loc = locations.get(locationId);
        if (loc == null) {
            throw new IllegalArgumentException("Location not found: " + locationId);
        }
        if (timePeriod == null || timePeriod.isBlank()) {
            throw new IllegalArgumentException("timePeriod must not be empty");
        }
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("startTime and endTime must not be null");
        }

        List<Tariff> tariffs = new ArrayList<>(loc.tariffs());
        if (hasOverlap(tariffs, startTime, endTime)) {
            throw new IllegalArgumentException("Tariff time range overlaps existing tariff for location: " + locationId);
        }
        String tariffId = "T-" + locationId + "-" + (tariffs.size() + 1);
        Tariff tariff = new Tariff(
                tariffId,
                pricePerKwhAC,
                pricePerKwhDC,
                pricePerMinuteAC,
                pricePerMinuteDC,
                timePeriod,
                startTime,
                endTime
        );

        tariffs.add(tariff);

        // record is immutable -> replace with updated copy
        Location updated = new Location(loc.id(), loc.name(), loc.address(), List.copyOf(tariffs));
        locations.put(locationId, updated);
    }
    public void updateTariff(String locationId,
                             double pricePerKwhAC,
                             double pricePerKwhDC,
                             double pricePerMinuteAC,
                             double pricePerMinuteDC) {
        updateTariff(locationId, pricePerKwhAC, pricePerKwhDC, pricePerMinuteAC, pricePerMinuteDC, null, null, null);
    }

    public void updateTariff(String locationId,
                             double pricePerKwhAC,
                             double pricePerKwhDC,
                             double pricePerMinuteAC,
                             double pricePerMinuteDC,
                             String timePeriod) {
        updateTariff(locationId, pricePerKwhAC, pricePerKwhDC, pricePerMinuteAC, pricePerMinuteDC, timePeriod, null, null);
    }

    public void updateTariff(String locationId,
                             double pricePerKwhAC,
                             double pricePerKwhDC,
                             double pricePerMinuteAC,
                             double pricePerMinuteDC,
                             String timePeriod,
                             LocalTime startTime,
                             LocalTime endTime) {

        Location loc = locations.get(locationId);
        if (loc == null) {
            throw new IllegalArgumentException("Location not found: " + locationId);
        }
        if (loc.tariffs().isEmpty()) {
            throw new IllegalArgumentException("No tariff defined for location: " + locationId);
        }
        if (timePeriod != null && timePeriod.isBlank()) {
            throw new IllegalArgumentException("timePeriod must not be empty");
        }

        List<Tariff> tariffs = new ArrayList<>(loc.tariffs());
        int index = findTariffIndex(tariffs, timePeriod);
        Tariff old = tariffs.get(index);
        LocalTime updatedStart = startTime == null ? old.startTime() : startTime;
        LocalTime updatedEnd = endTime == null ? old.endTime() : endTime;

        List<Tariff> others = new ArrayList<>(tariffs);
        others.remove(index);
        if (hasOverlap(others, updatedStart, updatedEnd)) {
            throw new IllegalArgumentException("Updated tariff time range overlaps existing tariff for location: " + locationId);
        }

        Tariff updatedTariff = new Tariff(
                old.tariffId(),
                pricePerKwhAC,
                pricePerKwhDC,
                pricePerMinuteAC,
                pricePerMinuteDC,
                timePeriod == null ? old.timePeriod() : timePeriod,
                updatedStart,
                updatedEnd
        );

        tariffs.set(index, updatedTariff);
        Location updatedLocation = new Location(loc.id(), loc.name(), loc.address(), List.copyOf(tariffs));
        locations.put(locationId, updatedLocation);
    }

    public Map<String, Tariff> readCurrentPricesByLocation(Date atTime) {
        Map<String, Tariff> result = new LinkedHashMap<>();
        Date time = atTime == null ? new Date() : atTime;
        for (Location loc : locations.values()) {
            result.put(loc.id(), readTariffAt(loc.id(), time)); // may be null if not defined yet
        }
        return result;
    }

    public Tariff readTariffAt(String locationId, Date atTime) {
        Location loc = locations.get(locationId);
        if (loc == null) {
            return null;
        }
        if (loc.tariffs().isEmpty()) {
            return null;
        }
        Date effectiveTime = atTime == null ? new Date() : atTime;
        LocalTime time = effectiveTime.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
        for (Tariff tariff : loc.tariffs()) {
            if (isWithin(time, tariff.startTime(), tariff.endTime())) {
                return tariff;
            }
        }
        return null;
    }

    public List<Tariff> readTariffs(String locationId) {
        Location loc = locations.get(locationId);
        if (loc == null) {
            throw new IllegalArgumentException("Location not found: " + locationId);
        }
        return loc.tariffs();
    }

    public List<NetworkStatusEntry> readNetworkStatus(ChargingPointManager chargingPointManager, Date atTime) {
        if (chargingPointManager == null) {
            throw new IllegalArgumentException("chargingPointManager must not be null");
        }
        Date time = atTime == null ? new Date() : atTime;
        List<NetworkStatusEntry> result = new ArrayList<>();
        for (Location loc : locations.values()) {
            Tariff current = readTariffAt(loc.id(), time);
            List<ChargingPoint> points = chargingPointManager.readByLocation(loc.id());
            result.add(new NetworkStatusEntry(loc, current, points));
        }
        return result;
    }

    private int findTariffIndex(List<Tariff> tariffs, String timePeriod) {
        if (timePeriod == null) {
            return tariffs.size() - 1;
        }
        List<Integer> matches = new ArrayList<>();
        for (int i = 0; i < tariffs.size(); i++) {
            if (timePeriod.equalsIgnoreCase(tariffs.get(i).timePeriod())) {
                matches.add(i);
            }
        }
        if (matches.isEmpty()) {
            throw new IllegalArgumentException("Tariff not found for timePeriod: " + timePeriod);
        }
        if (matches.size() > 1) {
            throw new IllegalArgumentException("Multiple tariffs found for timePeriod: " + timePeriod);
        }
        return matches.get(0);
    }

    private boolean hasOverlap(List<Tariff> tariffs, LocalTime startTime, LocalTime endTime) {
        for (Tariff existing : tariffs) {
            if (rangesOverlap(startTime, endTime, existing.startTime(), existing.endTime())) {
                return true;
            }
        }
        return false;
    }

    private boolean rangesOverlap(LocalTime startA, LocalTime endA, LocalTime startB, LocalTime endB) {
        return isWithin(startA, startB, endB) || isWithin(startB, startA, endA);
    }

    private boolean isWithin(LocalTime time, LocalTime start, LocalTime end) {
        if (start.equals(end)) {
            return true;
        }
        if (start.isBefore(end)) {
            return !time.isBefore(start) && time.isBefore(end);
        }
        return !time.isBefore(start) || time.isBefore(end);
    }
}
