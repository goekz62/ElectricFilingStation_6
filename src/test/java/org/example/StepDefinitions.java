package org.example;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Locale;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

public class StepDefinitions {

    private LocationManager locationManager;
    private ChargingPointManager chargingPointManager;

    private List<Location> lastLocations;
    private List<ChargingPoint> lastChargingPoints;
    private List<ChargingPoint> lastFilteredChargingPoints;
    private Exception lastError;

    private CustomerManager customerManager;
    private Customer createdCustomer;
    private Customer identifiedCustomer;

    private ChargingSessionManager chargingSessionManager;
    private ChargingSession lastSession;

    private InvoiceManager invoiceManager;
    private List<TopUp> lastTopUps;
    private List<Invoice> lastInvoices;
    private double lastBalance;

    private List<ChargingPoint> lastAvailablePoints;
    private Map<String, Tariff> lastPricesByLocation;
    private List<NetworkStatusEntry> lastNetworkStatus;
    private Date priceCheckTime;

    private String currentCustomerId;

    private static final DateTimeFormatter ISO_DT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private Date parseIsoDateTime(String text) {
        LocalDateTime ldt = LocalDateTime.parse(text, ISO_DT);
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    }

    private LocalTime parseTime(String text) {
        return LocalTime.parse(text);
    }

    private Date priceCheckTimeOrNow() {
        return priceCheckTime == null ? new Date() : priceCheckTime;
    }

    // =========================================================
    // Common Setup
    // =========================================================

    @Given("the network is empty")
    public void the_network_is_empty() {
        locationManager = new LocationManager();
        chargingPointManager = new ChargingPointManager();
        lastLocations = null;
        lastError = null;
        priceCheckTime = null;
    }

    @Given("the network has locations")
    public void the_network_has_locations(DataTable table) {
        the_network_is_empty();

        for (Map<String, String> row : table.asMaps(String.class, String.class)) {
            locationManager.createLocation(
                    row.get("id"),
                    row.get("name"),
                    row.get("address")
            );
        }
    }

    @Given("a location exists with id {string}, name {string}, address {string}")
    public void a_location_exists_with_id_name_address(String id, String name, String address) {
        if (locationManager == null) locationManager = new LocationManager();
        if (chargingPointManager == null) chargingPointManager = new ChargingPointManager();

        if (locationManager.readLocation(id) == null) {
            locationManager.createLocation(id, name, address);
        }
    }

    @Given("the current time is {string}")
    public void the_current_time_is(String timeText) {
        priceCheckTime = parseIsoDateTime(timeText);
    }

    // =========================================================
    // EPIC 1 - Manage Locations and Charging Points
    // US-1 Add and manage new locations
    // =========================================================

    @When("the operator creates a location with id {string}, name {string}, address {string}")
    public void the_operator_creates_a_location(String id, String name, String address) {
        lastError = null;
        try {
            locationManager.createLocation(id, name, address);
        } catch (Exception e) {
            lastError = e;
        }
    }

    @When("the operator tries to create another location with id {string}, name {string}, address {string}")
    public void the_operator_tries_to_create_another_location(String id, String name, String address) {
        lastError = null;
        try {
            locationManager.createLocation(id, name, address);
        } catch (Exception e) {
            lastError = e;
        }
    }

    @Then("the location list contains a location with id {string} and name {string}")
    public void the_location_list_contains_a_location_with_id_and_name(String id, String name) {
        Location loc = locationManager.readLocation(id);
        assertNotNull(loc);
        assertEquals(id, loc.id());
        assertEquals(name, loc.name());
    }

    @When("the operator updates location {string} with name {string} and address {string}")
    public void the_operator_updates_location(String id, String name, String address) {
        lastError = null;
        try {
            locationManager.updateLocation(id, name, address);
        } catch (Exception e) {
            lastError = e;
        }
    }

    @When("the operator deletes location {string}")
    public void the_operator_deletes_location(String id) {
        lastError = null;
        try {
            locationManager.deleteLocation(id);
        } catch (Exception e) {
            lastError = e;
        }
    }

    // =========================================================
    // EPIC 1 - US-2 Add AC and DC charging points
    // =========================================================

    @When("the operator adds a charging point with id {string} and type {string} and status {string} to location {string}")
    public void the_operator_adds_a_charging_point(String cpId, String type, String status, String locationId) {
        lastError = null;
        try {
            chargingPointManager.createChargingPoint(
                    cpId,
                    locationId,
                    ChargingType.valueOf(type.toUpperCase(Locale.ROOT)),
                    ChargingPointStatus.valueOf(status.toUpperCase(Locale.ROOT))
            );
        } catch (Exception e) {
            lastError = e;
        }
    }

    @When("the operator tries to add another charging point with id {string} and type {string} and status {string} to location {string}")
    public void the_operator_tries_to_add_another_charging_point(String cpId, String type, String status, String locationId) {
        lastError = null;
        try {
            chargingPointManager.createChargingPoint(
                    cpId,
                    locationId,
                    ChargingType.valueOf(type.toUpperCase(Locale.ROOT)),
                    ChargingPointStatus.valueOf(status.toUpperCase(Locale.ROOT))
            );
        } catch (Exception e) {
            lastError = e;
        }
    }

    @Then("location {string} has {int} charging points")
    public void location_has_charging_points(String locationId, int expected) {
        assertNotNull(chargingPointManager);
        assertEquals(expected, chargingPointManager.countByLocation(locationId));
    }

    @When("the operator updates charging point {string} to location {string} with type {string} and status {string}")
    public void the_operator_updates_charging_point(String cpId, String locationId, String type, String status) {
        lastError = null;
        try {
            chargingPointManager.updateChargingPoint(
                    cpId,
                    locationId,
                    ChargingType.valueOf(type.toUpperCase(Locale.ROOT)),
                    ChargingPointStatus.valueOf(status.toUpperCase(Locale.ROOT))
            );
        } catch (Exception e) {
            lastError = e;
        }
    }

    @When("the operator deletes charging point {string}")
    public void the_operator_deletes_charging_point(String cpId) {
        lastError = null;
        try {
            chargingPointManager.deleteChargingPoint(cpId);
        } catch (Exception e) {
            lastError = e;
        }
    }

    @When("the operator requests all charging points")
    public void the_operator_requests_all_charging_points_list() {
        assertNotNull(chargingPointManager);
        lastChargingPoints = chargingPointManager.readAllChargingPoints();
    }

    @Then("the charging points include")
    public void the_charging_points_include_operator(DataTable table) {
        List<ChargingPoint> points = lastChargingPoints != null ? lastChargingPoints : lastCustomerChargingPoints;
        assertNotNull(points);

        for (Map<String, String> row : table.asMaps(String.class, String.class)) {
            String expId = row.get("id");
            String expType = row.getOrDefault("type", null);
            String expStatus = row.getOrDefault("status", null);

            ChargingPoint cp = points.stream()
                    .filter(p -> p.id().equals(expId))
                    .findFirst()
                    .orElse(null);

            assertNotNull(cp, "Expected charging point not found: " + expId);
            if (expType != null) {
                assertEquals(ChargingType.valueOf(expType), cp.type());
            }
            if (expStatus != null) {
                assertEquals(ChargingPointStatus.valueOf(expStatus), cp.status());
            }
        }
    }

    // =========================================================
    // EPIC 1 - US-3 View list of all locations
    // =========================================================

    @When("the operator requests all locations")
    public void the_operator_requests_all_locations() {
        lastLocations = locationManager.readAllLocations();
    }

    @Then("the system returns {int} locations")
    public void the_system_returns_locations(int expected) {
        assertNotNull(lastLocations);
        assertEquals(expected, lastLocations.size());
    }

    // =========================================================
    // Shared error assertion
    // =========================================================

    @Then("the system shows an error containing {string}")
    public void the_system_shows_an_error_containing(String expectedText) {
        assertNotNull(lastError, "Expected an error but none occurred.");
        assertNotNull(lastError.getMessage(), "Error message was null.");

        assertTrue(
                lastError.getMessage().toLowerCase(Locale.ROOT).contains(expectedText.toLowerCase(Locale.ROOT)),
                "Error message was: " + lastError.getMessage()
        );
    }

    // =========================================================
    // EPIC 2 - Manage Pricing (US-6, US-7)
    // =========================================================

    @When("the operator defines a tariff for location {string} with:")
    public void the_operator_defines_a_tariff_for_location_with(String locationId, DataTable table) {
        Map<String, String> row = table.asMaps(String.class, String.class).get(0);

        double kWhAC = Double.parseDouble(row.get("kWhAC"));
        double kWhDC = Double.parseDouble(row.get("kWhDC"));
        double minAC = Double.parseDouble(row.get("minAC"));
        double minDC = Double.parseDouble(row.get("minDC"));
        String timePeriod = row.getOrDefault("timePeriod", "ALL_DAY");
        String startText = row.getOrDefault("startTime", "00:00");
        String endText = row.getOrDefault("endTime", "23:59");

        lastError = null;
        try {
            locationManager.defineTariff(
                    locationId,
                    kWhAC,
                    kWhDC,
                    minAC,
                    minDC,
                    timePeriod,
                    parseTime(startText),
                    parseTime(endText)
            );
        } catch (Exception e) {
            lastError = e;
        }
    }

    @When("the operator updates the tariff for location {string} to:")
    public void the_operator_updates_the_tariff_for_location_to(String locationId, DataTable table) {
        Map<String, String> row = table.asMaps(String.class, String.class).get(0);

        double kWhAC = Double.parseDouble(row.get("kWhAC"));
        double kWhDC = Double.parseDouble(row.get("kWhDC"));
        double minAC = Double.parseDouble(row.get("minAC"));
        double minDC = Double.parseDouble(row.get("minDC"));
        String timePeriod = row.getOrDefault("timePeriod", null);
        LocalTime startTime = row.containsKey("startTime") ? parseTime(row.get("startTime")) : null;
        LocalTime endTime = row.containsKey("endTime") ? parseTime(row.get("endTime")) : null;

        lastError = null;
        try {
            locationManager.updateTariff(locationId, kWhAC, kWhDC, minAC, minDC, timePeriod, startTime, endTime);
        } catch (Exception e) {
            lastError = e;
        }
    }

    @Then("location {string} has tariff:")
    public void location_has_tariff(String locationId, DataTable table) {
        Map<String, String> row = table.asMaps(String.class, String.class).get(0);

        double expKWhAC = Double.parseDouble(row.get("kWhAC"));
        double expKWhDC = Double.parseDouble(row.get("kWhDC"));
        double expMinAC = Double.parseDouble(row.get("minAC"));
        double expMinDC = Double.parseDouble(row.get("minDC"));
        String expTimePeriod = row.getOrDefault("timePeriod", "ALL_DAY");
        String startText = row.getOrDefault("startTime", "00:00");
        String endText = row.getOrDefault("endTime", "23:59");

        Location loc = locationManager.readLocation(locationId);
        assertNotNull(loc, "Location not found: " + locationId);
        assertFalse(loc.tariffs().isEmpty(), "Tariff not defined for location: " + locationId);

        Tariff t = loc.tariffs().stream()
                .filter(tariff -> tariff.timePeriod().equals(expTimePeriod))
                .findFirst()
                .orElseThrow();

        assertEquals(expKWhAC, t.pricePerKwhAC(), 0.00001);
        assertEquals(expKWhDC, t.pricePerKwhDC(), 0.00001);
        assertEquals(expMinAC, t.pricePerMinuteAC(), 0.00001);
        assertEquals(expMinDC, t.pricePerMinuteDC(), 0.00001);
        assertEquals(expTimePeriod, t.timePeriod());
        assertEquals(parseTime(startText), t.startTime());
        assertEquals(parseTime(endText), t.endTime());
    }

    // =========================================================
    // EPIC 3 - Network Monitoring (US-8, US-9)
    // =========================================================

    @Given("the network has charging points")
    public void the_network_has_charging_points(DataTable table) {
        if (chargingPointManager == null) chargingPointManager = new ChargingPointManager();

        for (Map<String, String> row : table.asMaps(String.class, String.class)) {
            chargingPointManager.createChargingPoint(
                    row.get("id"),
                    row.get("locationId"),
                    ChargingType.valueOf(row.get("type").toUpperCase(Locale.ROOT)),
                    ChargingPointStatus.valueOf(row.get("status").toUpperCase(Locale.ROOT))
            );
        }
    }

    @When("the operator requests all available charging points")
    public void the_operator_requests_all_available_charging_points() {
        assertNotNull(chargingPointManager);

        lastAvailablePoints = chargingPointManager.readAllChargingPoints().stream()
                .filter(cp -> cp.status() == ChargingPointStatus.AVAILABLE)
                .toList();
    }

    @Then("the system returns {int} available charging points")
    public void the_system_returns_available_charging_points(int expected) {
        assertNotNull(lastAvailablePoints);
        assertEquals(expected, lastAvailablePoints.size());
    }

    @Then("the available charging points include")
    public void the_available_charging_points_include(DataTable table) {
        assertNotNull(lastAvailablePoints);

        List<String> expectedIds = table.asMaps(String.class, String.class).stream()
                .map(r -> r.get("id"))
                .toList();

        List<String> actualIds = lastAvailablePoints.stream()
                .map(ChargingPoint::id)
                .toList();

        for (String id : expectedIds) {
            assertTrue(actualIds.contains(id), "Expected available CP id not found: " + id);
        }
    }

    @When("the operator requests current prices for all locations")
    public void the_operator_requests_current_prices_for_all_locations() {
        assertNotNull(locationManager);
        lastPricesByLocation = locationManager.readCurrentPricesByLocation(priceCheckTimeOrNow());
    }

    @Then("the system returns current prices for {int} locations")
    public void the_system_returns_current_prices_for_locations(int expected) {
        assertNotNull(lastPricesByLocation);
        assertEquals(expected, lastPricesByLocation.size());
    }

    @Then("location {string} current price is")
    public void location_current_price_is(String locationId, DataTable table) {
        assertNotNull(lastPricesByLocation);

        Map<String, String> row = table.asMaps(String.class, String.class).get(0);

        double expKWhAC = Double.parseDouble(row.get("kWhAC"));
        double expKWhDC = Double.parseDouble(row.get("kWhDC"));
        double expMinAC = Double.parseDouble(row.get("minAC"));
        double expMinDC = Double.parseDouble(row.get("minDC"));
        String expTimePeriod = row.getOrDefault("timePeriod", "ALL_DAY");
        String startText = row.getOrDefault("startTime", "00:00");
        String endText = row.getOrDefault("endTime", "23:59");

        Tariff t = lastPricesByLocation.get(locationId);
        assertNotNull(t, "No tariff found for location: " + locationId);

        assertEquals(expKWhAC, t.pricePerKwhAC(), 0.00001);
        assertEquals(expKWhDC, t.pricePerKwhDC(), 0.00001);
        assertEquals(expMinAC, t.pricePerMinuteAC(), 0.00001);
        assertEquals(expMinDC, t.pricePerMinuteDC(), 0.00001);
        assertEquals(expTimePeriod, t.timePeriod());
        assertEquals(parseTime(startText), t.startTime());
        assertEquals(parseTime(endText), t.endTime());
    }

    @When("the operator requests the network status")
    public void the_operator_requests_the_network_status() {
        assertNotNull(locationManager);
        assertNotNull(chargingPointManager);
        lastNetworkStatus = locationManager.readNetworkStatus(chargingPointManager, priceCheckTimeOrNow());
    }

    @Then("the network status includes location {string} with tariff")
    public void the_network_status_includes_location_with_tariff(String locationId, DataTable table) {
        assertNotNull(lastNetworkStatus);
        NetworkStatusEntry entry = lastNetworkStatus.stream()
                .filter(status -> status.location().id().equals(locationId))
                .findFirst()
                .orElseThrow();

        Map<String, String> row = table.asMaps(String.class, String.class).get(0);
        double expKWhAC = Double.parseDouble(row.get("kWhAC"));
        double expKWhDC = Double.parseDouble(row.get("kWhDC"));
        double expMinAC = Double.parseDouble(row.get("minAC"));
        double expMinDC = Double.parseDouble(row.get("minDC"));
        String expTimePeriod = row.getOrDefault("timePeriod", "ALL_DAY");
        String startText = row.getOrDefault("startTime", "00:00");
        String endText = row.getOrDefault("endTime", "23:59");

        Tariff t = entry.currentTariff();
        assertNotNull(t);
        assertEquals(expKWhAC, t.pricePerKwhAC(), 0.00001);
        assertEquals(expKWhDC, t.pricePerKwhDC(), 0.00001);
        assertEquals(expMinAC, t.pricePerMinuteAC(), 0.00001);
        assertEquals(expMinDC, t.pricePerMinuteDC(), 0.00001);
        assertEquals(expTimePeriod, t.timePeriod());
        assertEquals(parseTime(startText), t.startTime());
        assertEquals(parseTime(endText), t.endTime());
    }

    @Then("the network status includes charging points for location {string}")
    public void the_network_status_includes_charging_points_for_location(String locationId, DataTable table) {
        assertNotNull(lastNetworkStatus);
        NetworkStatusEntry entry = lastNetworkStatus.stream()
                .filter(status -> status.location().id().equals(locationId))
                .findFirst()
                .orElseThrow();

        List<String> expectedIds = table.asMaps(String.class, String.class).stream()
                .map(r -> r.get("id"))
                .toList();

        List<String> actualIds = entry.chargingPoints().stream()
                .map(ChargingPoint::id)
                .toList();

        for (String id : expectedIds) {
            assertTrue(actualIds.contains(id), "Expected charging point id not found: " + id);
        }
    }

    @Given("the seed network is loaded")
    public void the_seed_network_is_loaded() {
        locationManager = new LocationManager();
        chargingPointManager = new ChargingPointManager();
        customerManager = new CustomerManager();

        for (int i = 1; i <= 10; i++) {
            locationManager.createLocation("L" + i, "Location " + i, "Address " + i);
        }

        int cpId = 1;
        for (int i = 1; i <= 10; i++) {
            String locationId = "L" + i;
            chargingPointManager.createChargingPoint("CP" + (cpId++), locationId, ChargingType.AC, ChargingPointStatus.AVAILABLE);
            chargingPointManager.createChargingPoint("CP" + (cpId++), locationId, ChargingType.DC, ChargingPointStatus.AVAILABLE);
        }

        customerManager.createCustomer("Judith", "Muellner");
        customerManager.createCustomer("Katharina", "Weinberger");
        customerManager.createCustomer("Franz", "Steininger");
        customerManager.createCustomer("Nisa", "Yesillik");
        customerManager.createCustomer("Lukas", "Huber");
    }

    @Then("the system has {int} locations and {int} customers")
    public void the_system_has_locations_and_customers(int expectedLocations, int expectedCustomers) {
        assertNotNull(locationManager);
        assertNotNull(customerManager);
        assertEquals(expectedLocations, locationManager.readAllLocations().size());
        assertEquals(expectedCustomers, customerManager.readAllCustomers().size());
    }

    @Then("each location has between {int} and {int} charging points")
    public void each_location_has_between_and_charging_points(int min, int max) {
        assertNotNull(locationManager);
        assertNotNull(chargingPointManager);
        for (Location loc : locationManager.readAllLocations()) {
            long count = chargingPointManager.countByLocation(loc.id());
            assertTrue(count >= min && count <= max, "Location " + loc.id() + " had " + count + " points");
        }
    }

    @When("the operator filters charging points at location {string} with type {string} and max price {double}")
    public void the_operator_filters_charging_points(String locationId, String type, double maxPrice) {
        assertNotNull(chargingPointManager);
        ChargingType cpType = ChargingType.valueOf(type.toUpperCase(Locale.ROOT));
        lastFilteredChargingPoints = chargingPointManager.filterChargingPoints(
                locationManager,
                locationId,
                cpType,
                null,
                maxPrice,
                priceCheckTimeOrNow()
        );
    }

    @Then("the system returns {int} filtered charging points")
    public void the_system_returns_filtered_charging_points(int expected) {
        assertNotNull(lastFilteredChargingPoints);
        assertEquals(expected, lastFilteredChargingPoints.size());
    }

    @Then("the filtered charging points include")
    public void the_filtered_charging_points_include(DataTable table) {
        assertNotNull(lastFilteredChargingPoints);
        List<String> expectedIds = table.asMaps(String.class, String.class).stream()
                .map(r -> r.get("id"))
                .toList();
        List<String> actualIds = lastFilteredChargingPoints.stream()
                .map(ChargingPoint::id)
                .toList();
        for (String id : expectedIds) {
            assertTrue(actualIds.contains(id), "Expected filtered CP id not found: " + id);
        }
    }

    // =========================================================
    // EPIC 4 - Track Sessions and Billing (US-11, US-12)
    // =========================================================

    @Given("a charging session exists")
    public void a_charging_session_exists(DataTable table) {
        if (chargingSessionManager == null) chargingSessionManager = new ChargingSessionManager();

        Map<String, String> row = table.asMaps(String.class, String.class).get(0);

        String id = row.get("id");
        String customerId = row.get("customerId");
        String chargingPointId = row.get("chargingPointId");

        Date start = parseIsoDateTime(row.get("startTime"));
        Date end = parseIsoDateTime(row.get("endTime"));

        double kWh = Double.parseDouble(row.get("kWhCharged"));
        double cost = Double.parseDouble(row.get("totalCost"));

        ChargingSessionStatus status = ChargingSessionStatus.valueOf(row.get("status"));

        String tariffId = row.getOrDefault("tariffId", "T-UNKNOWN");
        double pricePerKwh = Double.parseDouble(row.getOrDefault("pricePerKwh", "0.0"));
        double pricePerMinute = Double.parseDouble(row.getOrDefault("pricePerMinute", "0.0"));
        String timePeriod = row.getOrDefault("timePeriod", "UNKNOWN");

        chargingSessionManager.createFinishedSession(
                id,
                customerId,
                chargingPointId,
                start,
                end,
                kWh,
                cost,
                status,
                tariffId,
                pricePerKwh,
                pricePerMinute,
                timePeriod
        );
    }

    @When("the operator requests charging session {string}")
    public void the_operator_requests_charging_session(String sessionId) {
        assertNotNull(chargingSessionManager);
        lastSession = chargingSessionManager.readSession(sessionId);
    }

    @Then("the session shows customer {string} and charging point {string}")
    public void the_session_shows_customer_and_charging_point(String customerId, String chargingPointId) {
        assertNotNull(lastSession);
        assertEquals(customerId, lastSession.customerId());
        assertEquals(chargingPointId, lastSession.chargingPointId());
    }

    @Then("the session has start time {string} and end time {string}")
    public void the_session_has_start_and_end_time(String startText, String endText) {
        assertNotNull(lastSession);
        assertEquals(parseIsoDateTime(startText), lastSession.startTime());
        assertEquals(parseIsoDateTime(endText), lastSession.endTime());
    }

    @Then("the session has kWh charged {double} and total cost {double}")
    public void the_session_has_kwh_charged_and_total_cost(double kWh, double totalCost) {
        assertNotNull(lastSession);
        assertEquals(kWh, lastSession.kWhCharged(), 0.0001);
        assertEquals(totalCost, lastSession.totalCost(), 0.0001);
    }

    @Then("the session status is {string}")
    public void the_session_status_is(String status) {
        assertNotNull(lastSession);
        assertEquals(ChargingSessionStatus.valueOf(status), lastSession.status());
    }

    @Given("a customer exists with id {string}")
    public void a_customer_exists_with_id(String customerId) {
        currentCustomerId = customerId;
        if (invoiceManager == null) invoiceManager = new InvoiceManager();
        assertNotNull(currentCustomerId);
    }

    @Given("the customer has top-ups")
    public void the_customer_has_topups(DataTable table) {
        if (invoiceManager == null) invoiceManager = new InvoiceManager();

        // ✅ if customer identified, use them
        if (currentCustomerId == null) {
            assertNotNull(identifiedCustomer, "Customer must be identified before adding top-ups.");
            currentCustomerId = identifiedCustomer.id();
        }

        for (Map<String, String> row : table.asMaps(String.class, String.class)) {
            invoiceManager.addTopUp(
                    row.get("id"),
                    currentCustomerId,
                    Double.parseDouble(row.get("amount")),
                    parseIsoDateTime(row.get("dateTime"))
            );
        }
    }

    @Given("the customer has invoices")
    public void the_customer_has_invoices(DataTable table) {
        if (invoiceManager == null) invoiceManager = new InvoiceManager();

        if (currentCustomerId == null) {
            assertNotNull(identifiedCustomer, "Customer must be identified before adding invoices.");
            currentCustomerId = identifiedCustomer.id();
        }

        Map<String, List<Map<String, String>>> rowsByInvoice = new java.util.LinkedHashMap<>();
        for (Map<String, String> row : table.asMaps(String.class, String.class)) {
            rowsByInvoice.computeIfAbsent(row.get("invoiceId"), key -> new java.util.ArrayList<>()).add(row);
        }

        for (Map.Entry<String, List<Map<String, String>>> entry : rowsByInvoice.entrySet()) {
            String invoiceId = entry.getKey();
            List<ChargingSession> sessions = new java.util.ArrayList<>();
            Date createdAt = null;
            InvoiceStatus status = null;

            for (Map<String, String> row : entry.getValue()) {
                ChargingSession session = new ChargingSession(
                        row.get("sessionId"),
                        currentCustomerId,
                        row.get("chargingPointId"),
                        parseIsoDateTime(row.get("startTime")),
                        parseIsoDateTime(row.get("endTime")),
                        Double.parseDouble(row.get("kWhCharged")),
                        Double.parseDouble(row.get("totalCost")),
                        ChargingSessionStatus.FINISHED,
                        row.getOrDefault("tariffId", "T-UNKNOWN"),
                        Double.parseDouble(row.getOrDefault("pricePerKwh", "0.0")),
                        Double.parseDouble(row.getOrDefault("pricePerMinute", "0.0")),
                        row.getOrDefault("timePeriod", "UNKNOWN")
                );
                sessions.add(session);

                if (createdAt == null) {
                    createdAt = parseIsoDateTime(row.get("endTime"));
                }
                if (status == null) {
                    status = InvoiceStatus.valueOf(row.get("status"));
                }
            }

            invoiceManager.addInvoice(
                    invoiceId,
                    currentCustomerId,
                    sessions,
                    createdAt,
                    status
            );
        }
    }

    @When("the operator requests billing history for that customer")
    public void the_operator_requests_billing_history_for_that_customer() {
        assertNotNull(invoiceManager);
        assertNotNull(currentCustomerId);

        lastTopUps = invoiceManager.readTopUps(currentCustomerId);
        lastInvoices = invoiceManager.readInvoices(currentCustomerId);
    }

    @Then("the system returns {int} top-ups and {int} invoices")
    public void the_system_returns_topups_and_invoices(int expectedTopUps, int expectedInvoices) {
        assertNotNull(lastTopUps);
        assertNotNull(lastInvoices);
        assertEquals(expectedTopUps, lastTopUps.size());
        assertEquals(expectedInvoices, lastInvoices.size());
    }

    @When("the operator corrects the customer balance by {double} with reason {string}")
    public void the_operator_corrects_customer_balance(double amount, String reason) {
        if (invoiceManager == null) invoiceManager = new InvoiceManager();
        assertNotNull(currentCustomerId);
        invoiceManager.addBalanceAdjustment(
                "ADJ1",
                currentCustomerId,
                amount,
                new Date(),
                reason
        );
    }

    @Then("the system returns {int} balance adjustments")
    public void the_system_returns_balance_adjustments(int expected) {
        assertNotNull(invoiceManager);
        assertNotNull(currentCustomerId);
        assertEquals(expected, invoiceManager.readBalanceAdjustments(currentCustomerId).size());
    }

    @Then("invoice {string} includes session {string} on charging point {string} with total cost {double}")
    public void invoice_includes_session_on_cp_with_total_cost(String invoiceId, String sessionId, String chargingPointId, double totalCost) {
        assertNotNull(lastInvoices);

        Invoice invoice = lastInvoices.stream()
                .filter(i -> i.id().equals(invoiceId))
                .findFirst()
                .orElse(null);

        assertNotNull(invoice, "Invoice not found: " + invoiceId);
        ChargingSession session = invoice.sessions().stream()
                .filter(s -> s.id().equals(sessionId))
                .findFirst()
                .orElse(null);
        assertNotNull(session, "Session not found in invoice: " + sessionId);
        assertEquals(chargingPointId, session.chargingPointId());
        assertEquals(totalCost, session.totalCost(), 0.0001);
    }

    @Then("invoice {string} has {int} sessions")
    public void invoice_has_sessions(String invoiceId, int expectedSessions) {
        assertNotNull(lastInvoices);

        Invoice invoice = lastInvoices.stream()
                .filter(i -> i.id().equals(invoiceId))
                .findFirst()
                .orElse(null);

        assertNotNull(invoice, "Invoice not found: " + invoiceId);
        assertEquals(expectedSessions, invoice.sessions().size());
    }

    // =========================================================
    // Customer EPIC 1 - Customer Registration (US-1)
    // =========================================================

    @Given("there are no customers")
    public void there_are_no_customers() {
        customerManager = new CustomerManager();
        createdCustomer = null;
    }

    @When("a customer registers with first name {string} and last name {string}")
    public void a_customer_registers_with_first_name_and_last_name(String firstName, String lastName) {
        assertNotNull(customerManager, "customerManager must be initialized");
        lastError = null;
        createdCustomer = null;
        try {
            createdCustomer = customerManager.createCustomer(firstName, lastName);
        } catch (Exception e) {
            lastError = e;
        }
    }

    @Then("the system creates the customer")
    public void the_system_creates_the_customer() {
        assertNotNull(createdCustomer, "Expected a created customer but got null");
        assertNotNull(createdCustomer.id(), "Customer id must not be null");
    }

    @When("the customer updates their name to first name {string} and last name {string}")
    public void the_customer_updates_their_name(String firstName, String lastName) {
        assertNotNull(createdCustomer, "Customer must be created first");
        customerManager.updateCustomer(createdCustomer.id(), firstName, lastName);
        createdCustomer = customerManager.readCustomer(createdCustomer.id());
    }

    @When("the customer deletes their account")
    public void the_customer_deletes_their_account() {
        assertNotNull(createdCustomer, "Customer must be created first");
        customerManager.deleteCustomer(createdCustomer.id());
        createdCustomer = null;
    }

    @Then("the system returns {int} customers")
    public void the_system_returns_customers(int expected) {
        assertNotNull(customerManager);
        assertEquals(expected, customerManager.readAllCustomers().size());
    }

    @Then("the customer id starts with {string}")
    public void the_customer_id_starts_with(String prefix) {
        assertNotNull(createdCustomer);
        assertTrue(createdCustomer.id().startsWith(prefix),
                "Expected customer id to start with " + prefix + " but was " + createdCustomer.id());
    }

    @Then("the customer data is first name {string} and last name {string}")
    public void the_customer_data_is_first_name_and_last_name(String expectedFirstName, String expectedLastName) {
        assertNotNull(createdCustomer);
        assertEquals(expectedFirstName, createdCustomer.firstName());
        assertEquals(expectedLastName, createdCustomer.lastName());
    }

    // =========================================================
    // Customer EPIC 2 - Manage Prepaid Balance (US-3, US-4, US-5)
    // =========================================================

    @Given("there are customers")
    public void there_are_customers(DataTable table) {
        if (customerManager == null) customerManager = new CustomerManager();
        if (invoiceManager == null) invoiceManager = new InvoiceManager();

        for (Map<String, String> row : table.asMaps(String.class, String.class)) {
            customerManager.createCustomer(row.get("firstName"), row.get("lastName"));
        }

        identifiedCustomer = null;
        lastInvoices = null;
        lastBalance = 0.0;
    }

    @When("the customer identifies as first name {string} and last name {string}")
    public void the_customer_identifies_as_first_name_and_last_name(String firstName, String lastName) {
        assertNotNull(customerManager, "customerManager must be initialized (use: Given there are customers)");

        identifiedCustomer = customerManager.readAllCustomers().stream()
                .filter(c -> c.firstName().equalsIgnoreCase(firstName) && c.lastName().equalsIgnoreCase(lastName))
                .findFirst()
                .orElse(null);

        assertNotNull(identifiedCustomer, "Customer not found: " + firstName + " " + lastName);

        // ✅ IMPORTANT: set currentCustomerId for invoice steps
        currentCustomerId = identifiedCustomer.id();

        // ✅ IMPORTANT: init invoiceManager (needed for topups/invoices)
        if (invoiceManager == null) invoiceManager = new InvoiceManager();
    }

    @Then("the system recognizes the customer")
    public void the_system_recognizes_the_customer() {
        assertNotNull(identifiedCustomer);
        assertNotNull(identifiedCustomer.id());
        assertFalse(identifiedCustomer.id().isBlank());
    }

    @Given("the customer has no top-ups yet")
    public void the_customer_has_no_topups_yet() {
        assertNotNull(invoiceManager, "invoiceManager must be initialized");
        assertNotNull(identifiedCustomer, "Customer must be identified first");

        assertTrue(
                invoiceManager.readTopUps(identifiedCustomer.id()).isEmpty(),
                "Expected no top-ups for " + identifiedCustomer.id()
        );
    }

    @When("the customer tops up amount {double}")
    public void the_customer_tops_up_amount(double amount) {
        assertNotNull(invoiceManager, "invoiceManager must be initialized");
        assertNotNull(identifiedCustomer, "Customer must be identified first");

        int next = invoiceManager.readTopUps(identifiedCustomer.id()).size() + 1;
        String topUpId = "T" + next;

        invoiceManager.addTopUp(topUpId, identifiedCustomer.id(), amount, new Date());
    }

    @When("the customer attempts to top up amount {double}")
    public void the_customer_attempts_to_top_up_amount(double amount) {
        assertNotNull(invoiceManager, "invoiceManager must be initialized");
        assertNotNull(identifiedCustomer, "Customer must be identified first");

        lastError = null;
        try {
            String topUpId = "T_ERR_" + System.currentTimeMillis();
            invoiceManager.addTopUp(topUpId, identifiedCustomer.id(), amount, new Date());
        } catch (Exception e) {
            lastError = e;
        }
    }

    @Then("the customer balance is {double}")
    public void the_customer_balance_is(double expected) {
        assertNotNull(invoiceManager, "invoiceManager must be initialized");
        String customerId = currentCustomerId;
        if (customerId == null && identifiedCustomer != null) {
            customerId = identifiedCustomer.id();
        }
        assertNotNull(customerId, "Customer must be identified first");

        double actual = invoiceManager.readBalance(customerId);
        assertEquals(expected, actual, 0.0001);
    }

    @When("the customer requests their invoices and balance")
    public void the_customer_requests_their_invoices_and_balance() {
        assertNotNull(invoiceManager, "invoiceManager must be initialized");
        assertNotNull(identifiedCustomer, "Customer must be identified first");

        lastInvoices = invoiceManager.readInvoices(identifiedCustomer.id());
        lastBalance = invoiceManager.readBalance(identifiedCustomer.id());
    }

    @Then("the system returns {int} invoices")
    public void the_system_returns_invoices(int expected) {
        assertNotNull(lastInvoices, "lastInvoices must not be null");
        assertEquals(expected, lastInvoices.size());
    }

    // =========================================================
// CUSTOMER EPIC 3 - View Network (US-6 + US-7)
// =========================================================

    private List<ChargingPoint> lastCustomerChargingPoints;
    private ChargingPoint lastCustomerSelectedChargingPoint;
    private double lastCustomerPricePerKwh;
    private double lastCustomerParkingPerMinute;

    // --- helper step: define tariff directly for a location (customer scenarios need tariffs) ---
    @Given("location {string} has tariff")
    public void location_has_tariff_customer(String locationId, DataTable table) {
        if (locationManager == null) locationManager = new LocationManager();

        Map<String, String> row = table.asMaps(String.class, String.class).get(0);

        double kWhAC = Double.parseDouble(row.get("kWhAC"));
        double kWhDC = Double.parseDouble(row.get("kWhDC"));
        double minAC = Double.parseDouble(row.get("minAC"));
        double minDC = Double.parseDouble(row.get("minDC"));
        String timePeriod = row.getOrDefault("timePeriod", "ALL_DAY");
        String startText = row.getOrDefault("startTime", "00:00");
        String endText = row.getOrDefault("endTime", "23:59");

        // use defineTariff (tariff applies for that location)
        locationManager.defineTariff(locationId, kWhAC, kWhDC, minAC, minDC, timePeriod, parseTime(startText), parseTime(endText));

        // quick assert so we fail early if something is wrong
        Location loc = locationManager.readLocation(locationId);
        assertNotNull(loc, "Location not found: " + locationId);
        assertFalse(loc.tariffs().isEmpty(), "Tariff not set for location: " + locationId);
        assertTrue(loc.tariffs().stream().anyMatch(t -> timePeriod.equals(t.timePeriod())));
    }

    // ---------------------------
// US-6: Customer views all charging points
// ---------------------------
    @When("the customer requests all charging points")
    public void the_customer_requests_all_charging_points() {
        assertNotNull(chargingPointManager, "chargingPointManager must be initialized");
        lastCustomerChargingPoints = chargingPointManager.readAllChargingPoints();
        assertNotNull(lastCustomerChargingPoints);
    }

    @Then("the system returns {int} charging points")
    public void the_system_returns_charging_points_customer(int expected) {
        assertNotNull(lastCustomerChargingPoints);
        assertEquals(expected, lastCustomerChargingPoints.size());
    }


    // ---------------------------
// US-7: Customer views current price for selected charging point
// ---------------------------
    @When("the customer requests current price for charging point {string}")
    public void the_customer_requests_current_price_for_charging_point(String chargingPointId) {
        assertNotNull(chargingPointManager, "chargingPointManager must be initialized");
        assertNotNull(locationManager, "locationManager must be initialized");

        lastCustomerSelectedChargingPoint = chargingPointManager.readChargingPoint(chargingPointId);
        assertNotNull(lastCustomerSelectedChargingPoint, "Charging point not found: " + chargingPointId);

        Location loc = locationManager.readLocation(lastCustomerSelectedChargingPoint.locationId());
        assertNotNull(loc, "Location not found: " + lastCustomerSelectedChargingPoint.locationId());
        Tariff t = locationManager.readTariffAt(loc.id(), priceCheckTimeOrNow());
        assertNotNull(t, "Tariff not defined for location: " + loc.id());

        // NEW meaning: kWhAC/kWhDC = price per kWh, minAC/minDC = parking price per minute
        if (lastCustomerSelectedChargingPoint.type() == ChargingType.AC) {
            lastCustomerPricePerKwh = t.pricePerKwhAC();
            lastCustomerParkingPerMinute = t.pricePerMinuteAC();
        } else {
            lastCustomerPricePerKwh = t.pricePerKwhDC();
            lastCustomerParkingPerMinute = t.pricePerMinuteDC();
        }
    }

    @When("the customer filters charging points at location {string} with type {string} and max price {double}")
    public void the_customer_filters_charging_points(String locationId, String type, double maxPrice) {
        assertNotNull(chargingPointManager, "chargingPointManager must be initialized");
        ChargingType cpType = ChargingType.valueOf(type.toUpperCase(Locale.ROOT));
        lastFilteredChargingPoints = chargingPointManager.filterChargingPoints(
                locationManager,
                locationId,
                cpType,
                null,
                maxPrice,
                priceCheckTimeOrNow()
        );
    }

    @Then("the system shows price per kWh {double} and parking per minute {double}")
    public void the_system_shows_price_per_kwh_and_parking_per_minute(double expectedPricePerKwh, double expectedParkingPerMinute) {
        assertNotNull(lastCustomerSelectedChargingPoint, "No charging point selected");
        assertEquals(expectedPricePerKwh, lastCustomerPricePerKwh, 0.00001);
        assertEquals(expectedParkingPerMinute, lastCustomerParkingPerMinute, 0.00001);
    }

    // =========================================================
// CUSTOMER EPIC 4 - Start Charging Session (US-9 + US-10)
// =========================================================


    private ChargingSession lastStartedSession;
    private ChargingSession lastLoadedSession;

    private String lastSessionId;

    // for checking duration/energy/cost
    private long lastDurationMinutes;
    private double lastEnergyKwh;
    private double lastTotalCost;

    // -------------------------
// setup balance (simple test helper)
// -------------------------
    @Given("the customer has balance {double}")
    public void the_customer_has_balance(double amount) {
        if (invoiceManager == null) invoiceManager = new InvoiceManager();
        assertNotNull(identifiedCustomer, "identifiedCustomer must be set first");

        // simplest way: top up exactly that amount
        invoiceManager.addTopUp("T_BAL_" + System.currentTimeMillis(), identifiedCustomer.id(), amount, new java.util.Date());
    }

    @Given("the customer has no balance")
    public void the_customer_has_no_balance() {
        assertNotNull(identifiedCustomer, "identifiedCustomer must be set first");
        invoiceManager = new InvoiceManager();
    }

    // -------------------------
// US-9 start session
// -------------------------
    @When("the customer starts charging session on charging point {string}")
    public void the_customer_starts_charging_session_on_charging_point(String chargingPointId) {
        assertNotNull(identifiedCustomer, "Customer must be identified first");
        if (chargingSessionManager == null) chargingSessionManager = new ChargingSessionManager();
        if (invoiceManager == null) invoiceManager = new InvoiceManager();

        ChargingPoint cp = chargingPointManager.readChargingPoint(chargingPointId);
        assertNotNull(cp, "Charging point not found: " + chargingPointId);

        assertEquals(ChargingPointStatus.AVAILABLE, cp.status(), "Charging point must be AVAILABLE");

        Location loc = locationManager.readLocation(cp.locationId());
        assertNotNull(loc, "Location not found: " + cp.locationId());

        Date startTime = priceCheckTimeOrNow();
        Tariff tariffAtStart = locationManager.readTariffAt(loc.id(), startTime);
        assertNotNull(tariffAtStart, "Tariff must be defined for location " + loc.id());

        double pricePerKwh = (cp.type() == ChargingType.AC) ? tariffAtStart.pricePerKwhAC() : tariffAtStart.pricePerKwhDC();
        double pricePerMinute = (cp.type() == ChargingType.AC) ? tariffAtStart.pricePerMinuteAC() : tariffAtStart.pricePerMinuteDC();

        // minimal prepaid check: must have > 0
        assertTrue(invoiceManager.readBalance(identifiedCustomer.id()) > 0, "Customer has no balance");

        lastStartedSession = chargingSessionManager.createSessionAutoIdAtTime(
                identifiedCustomer.id(),
                chargingPointId,
                startTime,
                tariffAtStart.tariffId(),
                pricePerKwh,
                pricePerMinute,
                tariffAtStart.timePeriod()
        );
        assertNotNull(lastStartedSession);

        lastSessionId = lastStartedSession.id();
    }

    @When("the customer starts a charging session at {string} on charging point {string}")
    public void the_customer_starts_a_charging_session_at_time(String timeText, String chargingPointId) {
        assertNotNull(identifiedCustomer, "Customer must be identified first");
        if (chargingSessionManager == null) chargingSessionManager = new ChargingSessionManager();
        if (invoiceManager == null) invoiceManager = new InvoiceManager();

        Date startTime = parseIsoDateTime(timeText);

        ChargingPoint cp = chargingPointManager.readChargingPoint(chargingPointId);
        assertNotNull(cp, "Charging point not found: " + chargingPointId);
        assertEquals(ChargingPointStatus.AVAILABLE, cp.status(), "Charging point must be AVAILABLE");

        Location loc = locationManager.readLocation(cp.locationId());
        assertNotNull(loc, "Location not found: " + cp.locationId());

        Tariff tariffAtStart = locationManager.readTariffAt(loc.id(), startTime);
        assertNotNull(tariffAtStart, "Tariff must be defined for location " + loc.id());

        double pricePerKwh = (cp.type() == ChargingType.AC) ? tariffAtStart.pricePerKwhAC() : tariffAtStart.pricePerKwhDC();
        double pricePerMinute = (cp.type() == ChargingType.AC) ? tariffAtStart.pricePerMinuteAC() : tariffAtStart.pricePerMinuteDC();

        assertTrue(invoiceManager.readBalance(identifiedCustomer.id()) > 0, "Customer has no balance");

        lastStartedSession = chargingSessionManager.createSessionAutoIdAtTime(
                identifiedCustomer.id(),
                chargingPointId,
                startTime,
                tariffAtStart.tariffId(),
                pricePerKwh,
                pricePerMinute,
                tariffAtStart.timePeriod()
        );
        assertNotNull(lastStartedSession);
        lastSessionId = lastStartedSession.id();
    }

    @When("the customer stops the charging session at {string}")
    public void the_customer_stops_the_charging_session_at_time(String timeText) {
        assertNotNull(lastSessionId);
        Date endTime = parseIsoDateTime(timeText);

        ChargingSession session = chargingSessionManager.readSession(lastSessionId);
        assertNotNull(session);
        ChargingPoint cp = chargingPointManager.readChargingPoint(session.chargingPointId());
        assertNotNull(cp);

        ChargingSessionManager.Calculation calc =
                chargingSessionManager.calculateForSession(session, endTime, cp.type(), session.pricePerKwh(), session.pricePerMinute());

        chargingSessionManager.endSession(lastSessionId, endTime, calc.kWhCharged(), calc.totalCost());
        lastLoadedSession = chargingSessionManager.readSession(lastSessionId);
    }

    @When("the customer attempts to start charging session on charging point {string}")
    public void the_customer_attempts_to_start_charging_session_on_charging_point(String chargingPointId) {
        lastError = null;
        try {
            the_customer_starts_charging_session_on_charging_point(chargingPointId);
        } catch (AssertionError | RuntimeException e) {
            lastError = e;
        }
    }

    @Then("a new charging session is created and is ACTIVE")
    public void a_new_charging_session_is_created_and_is_active() {
        assertNotNull(lastSessionId);
        ChargingSession s = chargingSessionManager.readSession(lastSessionId);
        assertNotNull(s);
        assertEquals(ChargingSessionStatus.ACTIVE, s.status());
    }

    @Then("the session belongs to the identified customer")
    public void the_session_belongs_to_the_identified_customer() {
        ChargingSession s = chargingSessionManager.readSession(lastSessionId);
        assertNotNull(s);
        assertEquals(identifiedCustomer.id(), s.customerId());
    }

    @Then("the session uses charging point {string}")
    public void the_session_uses_charging_point(String expectedCpId) {
        ChargingSession s = chargingSessionManager.readSession(lastSessionId);
        assertNotNull(s);
        assertEquals(expectedCpId, s.chargingPointId());
    }

    // -------------------------
// US-10 view session info
// -------------------------
    @Given("the customer has an active charging session on charging point {string}")
    public void the_customer_has_an_active_charging_session_on_charging_point(String chargingPointId) {
        // reuse the start step
        the_customer_starts_charging_session_on_charging_point(chargingPointId);
    }

    @When("{int} minutes pass for that session")
    public void minutes_pass_for_that_session(int minutes) {
        assertNotNull(lastSessionId);

        ChargingSession s = chargingSessionManager.readSession(lastSessionId);
        assertNotNull(s);
        assertEquals(ChargingSessionStatus.ACTIVE, s.status());

        ChargingPoint cp = chargingPointManager.readChargingPoint(s.chargingPointId());
        assertNotNull(cp);

        double pricePerKwh = s.pricePerKwh();
        double parkingPerMinute = s.pricePerMinute();

        double powerKw = (cp.type() == ChargingType.AC) ? 11.0 : 50.0;
        double hours = minutes / 60.0;
        double energy = powerKw * hours;
        double cost = energy * pricePerKwh + minutes * parkingPerMinute;

        // "simulate" end time after X minutes (still deterministic)
        java.util.Date fakeEnd = new java.util.Date(s.startTime().getTime() + minutes * 60_000L);

        // we end the session so ChargingSession has real values stored
        chargingSessionManager.endSession(lastSessionId, fakeEnd, energy, cost);

        // store for assertions
        lastDurationMinutes = minutes;
        lastEnergyKwh = energy;
        lastTotalCost = cost;
    }

    @When("the customer requests charging session information")
    public void the_customer_requests_charging_session_information() {
        assertNotNull(lastSessionId);
        lastLoadedSession = chargingSessionManager.readSession(lastSessionId);
        assertNotNull(lastLoadedSession);
    }

    @Then("the session shows duration {int} minutes")
    public void the_session_shows_duration_minutes(int expectedMinutes) {
        assertNotNull(lastLoadedSession);
        assertNotNull(lastLoadedSession.startTime());
        assertNotNull(lastLoadedSession.endTime());

        long actualMinutes = (lastLoadedSession.endTime().getTime() - lastLoadedSession.startTime().getTime()) / 60_000L;
        assertEquals(expectedMinutes, actualMinutes);
    }

    @Then("the session shows charged energy {double} kWh")
    public void the_session_shows_charged_energy_kwh(double expectedKwh) {
        assertNotNull(lastLoadedSession);
        assertEquals(expectedKwh, lastLoadedSession.kWhCharged(), 0.0001);
    }

    @Then("the session shows total cost {double}")
    public void the_session_shows_total_cost(double expectedCost) {
        assertNotNull(lastLoadedSession);
        assertEquals(expectedCost, lastLoadedSession.totalCost(), 0.0001);
    }
}
