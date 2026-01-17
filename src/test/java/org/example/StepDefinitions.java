package org.example;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.*;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class StepDefinitions {

    private LocationManager locationManager;
    private ChargingPointManager chargingPointManager;
    private CustomerManager customerManager;

    private List<Location> lastLocations;
    private List<ChargingPoint> lastChargingPoints;
    private Map<String, Tariff> lastPrices;

    // for auto-generated customer IDs
    private Customer createdCustomer;

    // -------------------------
    // Locations
    // -------------------------
    @Given("the network has no locations")
    public void the_network_has_no_locations() {
        locationManager = new LocationManager();
        chargingPointManager = new ChargingPointManager(); // safe init for scenarios that add points
    }

    @When("the operator creates a location with id {string}, name {string}, address {string}")
    public void the_operator_creates_a_location(String id, String name, String address) {
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

    // -------------------------
    // Charging Points (WITH STATUS)
    // -------------------------
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

    // -------------------------
    // Customers (AUTO-ID)
    // -------------------------
    @Given("there are no customers")
    public void there_are_no_customers() {
        customerManager = new CustomerManager();
        createdCustomer = null;
    }

    @When("a customer is created with name {string} and lastname {string}")
    public void a_customer_is_created(String firstName, String lastName) {
        createdCustomer = customerManager.createCustomer(firstName, lastName); // system generates ID
    }

    @Then("the customer list contains a customer with name {string} and lastname {string}")
    public void the_customer_list_contains_a_customer(String firstName, String lastName) {
        assertNotNull(createdCustomer);
        assertEquals(firstName, createdCustomer.firstName());
        assertEquals(lastName, createdCustomer.lastName());

        // verify it's stored in the manager
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

    // -------------------------
// Pricing (US-6)
// -------------------------
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




}
