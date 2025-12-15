package org.example;

import io.cucumber.java.en.*;
import java.util.List;
import java.util.Map;
import io.cucumber.datatable.DataTable;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

public class StepDefinitions {

    private LocationManager locationManager;
    private List<Location> lastLocations;
    private ChargingPointManager chargingPointManager;
    private List<ChargingPoint> lastChargingPoints;

    private CustomerManager customerManager;


    // Manage Location
    @Given("the network has no locations")
    public void the_network_has_no_locations() {
        locationManager = new LocationManager();

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
        loc.toString(); // ensure toString exists and is usable
    }

    @Given("a location exists with id {string}, name {string}, address {string}")
    public void a_location_exists(String id, String name, String address) {
        the_network_has_no_locations();
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

    @When("the operator adds a charging point with id {string} and type {string} to location {string}")
    public void the_operator_adds_a_charging_point(String cpId, String type, String locationId) {
        if (chargingPointManager == null) {
            chargingPointManager = new ChargingPointManager();
        }
        chargingPointManager.createChargingPoint(cpId, locationId, ChargingType.valueOf(type));
    }

    @Then("location {string} has {int} charging points")
    public void location_has_charging_points(String locationId, int expected) {
        assertEquals(expected, chargingPointManager.countByLocation(locationId));
    }

    @Given("the locations have charging points")
    public void the_locations_have_charging_points(DataTable table) {
        if (chargingPointManager == null) chargingPointManager = new ChargingPointManager();
        for (Map<String, String> row : table.asMaps(String.class, String.class)) {
            chargingPointManager.createChargingPoint(
                    row.get("id"),
                    row.get("location"),
                    ChargingType.valueOf(row.get("type"))
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
    // Customers (create/read only)
    @Given("there are no customers")
    public void there_are_no_customers() {
        locationManager = new LocationManager();
        chargingPointManager = new ChargingPointManager();
        customerManager = new CustomerManager();
    }

    @When("a customer is created with id {string}, name {string} and lastname {string}")
    public void  a_customer_is_created(String id, String firstName, String lastName) {
        customerManager.createCustomer(id, firstName, lastName);
    }

    @Then("the customer list contains a customer with id {string} and name {string} and lastname {string}")
    public void the_customer_list_contains_a_customer(String id, String firstName, String lastName) {
        Customer c = customerManager.readCustomer(id);
        assertNotNull(c);
        assertEquals(id, c.id());
        assertEquals(firstName, c.firstName());
        assertEquals(lastName, c.lastName());
        c.toString();
    }

}
