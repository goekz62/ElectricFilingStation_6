package org.example;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

public class StepDefinitions {

    private LocationManager locationManager;
    private ChargingPointManager chargingPointManager;
    private CustomerManager customerManager;

    private List<Location> lastLocations;
    private List<ChargingPoint> lastChargingPoints;

    private Map<String, Tariff> lastPrices;

    private ChargingSessionManager chargingSessionManager;
    private ChargingSession lastSession;

    private InvoiceManager invoiceManager;
    private List<TopUp> lastTopUps;
    private List<Invoice> lastInvoices;

    private Customer createdCustomer;
    private double lastPricePerKwh;
    private double lastPricePerMinute;
    private ChargingPoint lastSelectedChargingPoint;


    private static final DateTimeFormatter ISO_DT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private Date parseIsoDateTime(String text) {
        LocalDateTime ldt = LocalDateTime.parse(text, ISO_DT);
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    }

    // =========================================================
    // Locations
    // =========================================================
    @Given("the network has no locations")
    public void the_network_has_no_locations() {
        locationManager = new LocationManager();
        chargingPointManager = new ChargingPointManager();
    }

    @When("the operator creates a location with id {string}, name {string}, address {string}")
    public void the_operator_creates_a_location(String id, String name, String address) {
        if (locationManager == null) locationManager = new LocationManager();
        locationManager.createLocation(id, name, address);
    }

    @Then("the location list contains a location with id {string} and name {string}")
    public void the_location_list_contains_a_location(String id, String name) {
        Location loc = locationManager.readLocation(id);
        assertNotNull(loc);
        assertEquals(id, loc.id());
        assertEquals(name, loc.name());
        loc.toString();
    }

    @Given("a location exists with id {string}, name {string}, address {string}")
    public void a_location_exists(String id, String name, String address) {
        locationManager = new LocationManager();
        chargingPointManager = new ChargingPointManager();
        locationManager.createLocation(id, name, address);
    }

    @Given("the network has locations")
    public void the_network_has_locations(DataTable table) {
        the_network_has_no_locations();
        for (Map<String, String> row : table.asMaps(String.class, String.class)) {
            locationManager.createLocation(row.get("id"), row.get("name"), row.get("address"));
        }
    }

    @When("the operator requests all locations")
    public void the_operator_requests_all_locations() {
        lastLocations = locationManager.readAllLocations();
        lastLocations.forEach(Location::toString);
    }

    @Then("the system returns {int} locations")
    public void the_system_returns_locations(int expected) {
        assertNotNull(lastLocations);
        assertEquals(expected, lastLocations.size());
    }

    // =========================================================
    // Charging Points (WITH STATUS)
    // =========================================================
    @When("the operator adds a charging point with id {string} and type {string} and status {string} to location {string}")
    public void the_operator_adds_a_charging_point(String cpId, String type, String status, String locationId) {
        if (chargingPointManager == null) chargingPointManager = new ChargingPointManager();

        chargingPointManager.createChargingPoint(
                cpId,
                locationId,
                ChargingType.valueOf(type),
                ChargingPointStatus.valueOf(status)
        );
    }

    @Then("location {string} has {int} charging points")
    public void location_has_charging_points(String locationId, int expected) {
        assertNotNull(chargingPointManager);
        assertEquals(expected, chargingPointManager.countByLocation(locationId));
    }

    @Given("the locations have charging points")
    public void the_locations_have_charging_points(DataTable table) {
        if (chargingPointManager == null) chargingPointManager = new ChargingPointManager();

        for (Map<String, String> row : table.asMaps(String.class, String.class)) {
            chargingPointManager.createChargingPoint(
                    row.get("id"),
                    row.get("location"),
                    ChargingType.valueOf(row.get("type")),
                    ChargingPointStatus.valueOf(row.get("status"))
            );
        }
    }

    @When("the operator requests all charging points")
    public void the_operator_requests_all_charging_points() {
        lastChargingPoints = chargingPointManager.readAllChargingPoints();
        lastChargingPoints.forEach(ChargingPoint::toString);
    }

    @Then("the system returns {int} charging points")
    public void the_system_returns_charging_points(int expected) {
        assertNotNull(lastChargingPoints);
        assertEquals(expected, lastChargingPoints.size());
    }

    // =========================================================
    // Customers (AUTO-ID)
    // =========================================================
    @Given("there are no customers")
    public void there_are_no_customers() {
        customerManager = new CustomerManager();
        createdCustomer = null;
    }

    // ✅ KEEP ONLY THIS ONE (removes duplicate exception)
    @When("a customer is created with name {string} and lastname {string}")
    public void a_customer_is_created(String firstName, String lastName) {
        if (customerManager == null) customerManager = new CustomerManager();
        createdCustomer = customerManager.createCustomer(firstName, lastName);
        assertNotNull(createdCustomer);
    }

    @Then("the customer list contains a customer with name {string} and lastname {string}")
    public void the_customer_list_contains_a_customer(String firstName, String lastName) {
        assertNotNull(createdCustomer);
        assertEquals(firstName, createdCustomer.firstName());
        assertEquals(lastName, createdCustomer.lastName());

        Customer fromManager = customerManager.readCustomer(createdCustomer.id());
        assertNotNull(fromManager);
        assertEquals(createdCustomer, fromManager);
        fromManager.toString();
    }

    @Then("the system generates a customer id starting with {string}")
    public void the_system_generates_a_customer_id_starting_with(String prefix) {
        assertNotNull(createdCustomer);
        assertTrue(createdCustomer.id().startsWith(prefix));
    }

    // =========================================================
    // Pricing (US-6, US-7, US-9)
    // =========================================================
    @When("the operator defines a tariff for location {string} with")
    public void the_operator_defines_a_tariff_for_location_with(String locationId, DataTable table) {
        Map<String, String> row = table.asMaps(String.class, String.class).get(0);

        double kwhAC = Double.parseDouble(row.get("pricePerKwhAC"));
        double kwhDC = Double.parseDouble(row.get("pricePerKwhDC"));
        double minAC = Double.parseDouble(row.get("pricePerMinuteAC"));
        double minDC = Double.parseDouble(row.get("pricePerMinuteDC"));

        locationManager.defineTariff(locationId, kwhAC, kwhDC, minAC, minDC);
    }

    @Then("location {string} has tariff")
    public void location_has_tariff(String locationId, DataTable table) {
        Map<String, String> row = table.asMaps(String.class, String.class).get(0);

        double expKwhAC = Double.parseDouble(row.get("pricePerKwhAC"));
        double expKwhDC = Double.parseDouble(row.get("pricePerKwhDC"));
        double expMinAC = Double.parseDouble(row.get("pricePerMinuteAC"));
        double expMinDC = Double.parseDouble(row.get("pricePerMinuteDC"));

        Location loc = locationManager.readLocation(locationId);
        assertNotNull(loc);
        assertNotNull(loc.tariff());

        Tariff t = loc.tariff();
        assertEquals(expKwhAC, t.pricePerKwhAC(), 0.00001);
        assertEquals(expKwhDC, t.pricePerKwhDC(), 0.00001);
        assertEquals(expMinAC, t.pricePerMinuteAC(), 0.00001);
        assertEquals(expMinDC, t.pricePerMinuteDC(), 0.00001);

        t.toString();
    }

    @When("the operator updates the tariff for location {string} to")
    public void the_operator_updates_the_tariff_for_location_to(String locationId, DataTable table) {
        Map<String, String> row = table.asMaps(String.class, String.class).get(0);

        double kwhAC = Double.parseDouble(row.get("pricePerKwhAC"));
        double kwhDC = Double.parseDouble(row.get("pricePerKwhDC"));
        double minAC = Double.parseDouble(row.get("pricePerMinuteAC"));
        double minDC = Double.parseDouble(row.get("pricePerMinuteDC"));

        locationManager.updateTariff(locationId, kwhAC, kwhDC, minAC, minDC);
    }

    @When("the operator requests current prices for all locations")
    public void the_operator_requests_current_prices_for_all_locations() {
        lastPrices = locationManager.readCurrentPricesByLocation();
    }

    @Then("the system returns prices for {int} locations")
    public void the_system_returns_prices_for_locations(int expected) {
        assertNotNull(lastPrices);
        assertEquals(expected, lastPrices.size());
    }

    // =========================================================
    // US-11 Charging Session Info
    // =========================================================
    @Given("a charging session exists")
    public void a_charging_session_exists(DataTable table) {
        if (chargingSessionManager == null) {
            chargingSessionManager = new ChargingSessionManager();
        }

        Map<String, String> row = table.asMaps(String.class, String.class).get(0);

        String id = row.get("id");
        String customerId = row.get("customerId");
        String chargingPointId = row.get("chargingPointId");

        Date start = parseIsoDateTime(row.get("startTime"));
        Date end = row.get("endTime") == null ? null : parseIsoDateTime(row.get("endTime"));

        double kWh = Double.parseDouble(row.get("kWhCharged"));
        double cost = Double.parseDouble(row.get("totalCost"));

        ChargingSessionStatus status = ChargingSessionStatus.valueOf(row.get("status"));

        chargingSessionManager.createFinishedSession(id, customerId, chargingPointId, start, end, kWh, cost, status);
    }

    @When("the operator requests charging session {string}")
    public void the_operator_requests_charging_session(String sessionId) {
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
    public void the_session_has_kwh_and_total_cost(double kWh, double totalCost) {
        assertNotNull(lastSession);
        assertEquals(kWh, lastSession.kWhCharged(), 0.0001);
        assertEquals(totalCost, lastSession.totalCost(), 0.0001);
    }

    @Then("the session status is {string}")
    public void the_session_status_is(String status) {
        assertNotNull(lastSession);
        assertEquals(ChargingSessionStatus.valueOf(status), lastSession.status());
        lastSession.toString();
    }

    // =========================================================
    // US-12 View invoices + top-ups (InvoiceManager)
    // =========================================================
    @Given("the customer has top-ups")
    public void the_customer_has_topups(DataTable table) {
        if (invoiceManager == null) invoiceManager = new InvoiceManager();
        assertNotNull(createdCustomer);

        for (Map<String, String> row : table.asMaps(String.class, String.class)) {
            invoiceManager.addTopUp(
                    row.get("id"),
                    createdCustomer.id(),
                    Double.parseDouble(row.get("amount")),
                    parseIsoDateTime(row.get("dateTime"))
            );
        }
    }

    @Given("the customer has invoices")
    public void the_customer_has_invoices(DataTable table) {
        if (invoiceManager == null) invoiceManager = new InvoiceManager();
        assertNotNull(createdCustomer);

        for (Map<String, String> row : table.asMaps(String.class, String.class)) {

            ChargingSession session = new ChargingSession(
                    row.get("sessionId"),
                    createdCustomer.id(),
                    row.get("chargingPointId"),
                    parseIsoDateTime(row.get("startTime")),
                    parseIsoDateTime(row.get("endTime")),
                    Double.parseDouble(row.get("kWhCharged")),
                    Double.parseDouble(row.get("totalCost")),
                    ChargingSessionStatus.FINISHED
            );

            invoiceManager.addInvoice(
                    row.get("invoiceId"),
                    createdCustomer.id(),
                    session,
                    parseIsoDateTime(row.get("endTime")),
                    InvoiceStatus.valueOf(row.get("status"))
            );
        }
    }

    @When("the operator requests billing history for that customer")
    public void the_operator_requests_billing_history_for_that_customer() {
        assertNotNull(createdCustomer);
        assertNotNull(invoiceManager);

        lastTopUps = invoiceManager.readTopUps(createdCustomer.id());
        lastInvoices = invoiceManager.readInvoices(createdCustomer.id());
    }

    @Then("the system returns {int} top-ups and {int} invoices")
    public void the_system_returns_topups_and_invoices(int expectedTopUps, int expectedInvoices) {
        assertNotNull(lastTopUps);
        assertNotNull(lastInvoices);
        assertEquals(expectedTopUps, lastTopUps.size());
        assertEquals(expectedInvoices, lastInvoices.size());
    }

    @Then("invoice {string} includes session {string} on charging point {string} with total cost {double}")
    public void invoice_includes_session_on_cp_with_total_cost(String invoiceId, String sessionId, String chargingPointId, double totalCost) {
        assertNotNull(lastInvoices);

        Invoice invoice = lastInvoices.stream()
                .filter(i -> i.id().equals(invoiceId))
                .findFirst()
                .orElse(null);

        assertNotNull(invoice);
        assertEquals(sessionId, invoice.session().id());
        assertEquals(chargingPointId, invoice.session().chargingPointId());
        assertEquals(totalCost, invoice.session().totalCost(), 0.0001);
    }
    private String currentCustomerId;

    @Given("a customer exists with id {string}")
    public void a_customer_exists_with_id(String customerId) {
        invoiceManager = new InvoiceManager();
        currentCustomerId = customerId;
        assertNotNull(currentCustomerId);
    }

    @Given("customer {string} has no previous top-ups")
    public void customer_has_no_previous_topups(String customerId) {
        List<TopUp> topUps = invoiceManager.readTopUps(customerId);
        assertTrue(topUps.isEmpty());
    }

    @When("customer {string} tops up amount {double}")
    public void customer_tops_up_amount(String customerId, double amount) {
        invoiceManager.addTopUp(
                "T1",
                customerId,
                amount,
                new Date()
        );
    }

    @Then("customer {string} balance should be {double}")
    public void customer_balance_should_be(String customerId, double expectedBalance) {
        double actualBalance = invoiceManager.readBalance(customerId);
        assertEquals(expectedBalance, actualBalance, 0.0001);
    }
    @When("the customer requests current price for charging point {string}")
    public void the_customer_requests_current_price_for_charging_point(String chargingPointId) {
        assertNotNull(chargingPointManager, "chargingPointManager must be initialized");
        assertNotNull(locationManager, "locationManager must be initialized");

        // Find charging point (no need for a readChargingPoint method)
        lastSelectedChargingPoint = chargingPointManager.readAllChargingPoints().stream()
                .filter(cp -> cp.id().equals(chargingPointId))
                .findFirst()
                .orElse(null);

        assertNotNull(lastSelectedChargingPoint, "Charging point not found: " + chargingPointId);

        Location loc = locationManager.readLocation(lastSelectedChargingPoint.locationId());
        assertNotNull(loc, "Location not found: " + lastSelectedChargingPoint.locationId());
        assertNotNull(loc.tariff(), "Tariff not defined for location: " + loc.id());

        Tariff t = loc.tariff();

        if (lastSelectedChargingPoint.type() == ChargingType.AC) {
            lastPricePerKwh = t.pricePerKwhAC();
            lastPricePerMinute = t.pricePerMinuteAC();
        } else { // DC
            lastPricePerKwh = t.pricePerKwhDC();
            lastPricePerMinute = t.pricePerMinuteDC();
        }
    }

    @Then("the system shows price per kWh {double} and price per minute {double}")
    public void the_system_shows_price_per_kwh_and_price_per_minute(double expectedKwh, double expectedMinute) {
        assertNotNull(lastSelectedChargingPoint, "No charging point selected");
        assertEquals(expectedKwh, lastPricePerKwh, 0.00001);
        assertEquals(expectedMinute, lastPricePerMinute, 0.00001);
    }

    // =======================
// US-9 Start charging
// =======================


    private Customer currentCustomer;
    private ChargingSession startedSession;
    private boolean denied;

    @Given("a customer exists with id {string}")
    public void a_customer_exists_with_id(String customerId) {
        customerManager = new CustomerManager();
        invoiceManager = new InvoiceManager();

        currentCustomer = customerManager.createCustomer("Temp", "User");
    }

    @Given("the customer has a balance of {double}")
    public void the_customer_has_a_balance_of(double amount) {
        if (amount > 0) {
            invoiceManager.addTopUp(
                    "T1",
                    currentCustomer.id(),
                    amount,
                    new java.util.Date()
            );
        }
    }

    @Given("a charging point {string} exists with status {string}")
    public void a_charging_point_exists_with_status(String cpId, String status) {
        chargingPointManager = new ChargingPointManager();

        chargingPointManager.createChargingPoint(
                cpId,
                "L1",
                ChargingType.AC,
                ChargingPointStatus.valueOf(status)
        );
    }

    @When("the customer starts charging at {string}")
    public void the_customer_starts_charging_at(String cpId) {
        chargingSessionManager = new ChargingSessionManager();
        denied = false;
        startedSession = null;

        ChargingPoint cp = chargingPointManager.readChargingPoint(cpId);

        if (cp == null || cp.status() != ChargingPointStatus.AVAILABLE) {
            denied = true;
            return;
        }

        if (invoiceManager.readBalance(currentCustomer.id()) <= 0) {
            denied = true;
            return;
        }

        String sessionId = "S1";
        chargingSessionManager.createSession(
                sessionId,
                currentCustomer.id(),
                cpId,
                new java.util.Date()
        );

        chargingPointManager.updateStatus(cpId, ChargingPointStatus.OCCUPIED);
        startedSession = chargingSessionManager.readSession(sessionId);
    }

    @Then("a charging session is created")
    public void a_charging_session_is_created() {
        assertFalse(denied);
        assertNotNull(startedSession);
    }

    @Then("the session status is {string}")
    public void the_session_status_is(String status) {
        assertEquals(
                ChargingSessionStatus.valueOf(status),
                startedSession.status()
        );
    }

    @Then("the charging point {string} status is {string}")
    public void the_charging_point_status_is(String cpId, String status) {
        ChargingPoint cp = chargingPointManager.readChargingPoint(cpId);
        assertEquals(
                ChargingPointStatus.valueOf(status),
                cp.status()
        );
    }

    @Then("the charging session is denied")
    public void the_charging_session_is_denied() {
        assertTrue(denied);
        assertNull(startedSession);
    }




}
