Feature: EPIC 1 - Manage Locations and Charging Points
  As an operator, I want to manage all charging locations and their AC/DC charging points
  so that I can ensure the charging network is correctly structured and ready for customer use.

  Background:
    Given the network is empty

  # ---------------------------------------------------------
  # US-1 Add and manage new locations
  # ---------------------------------------------------------
  Scenario: Add a new location
    When the operator creates a location with id "L1", name "Vienna Center", address "Stephansplatz 1"
    Then the location list contains a location with id "L1" and name "Vienna Center"

  Scenario: Prevent duplicate location id
    When the operator creates a location with id "L1", name "Vienna Center", address "Stephansplatz 1"
    And the operator tries to create another location with id "L1", name "Duplicate", address "Somewhere 2"
    Then the system shows an error containing "already exists"

  Scenario: Update a location
    Given a location exists with id "L1", name "Vienna Center", address "Stephansplatz 1"
    When the operator updates location "L1" with name "Vienna Central" and address "Stephansplatz 2"
    Then the location list contains a location with id "L1" and name "Vienna Central"

  Scenario: Delete a location
    Given a location exists with id "L1", name "Vienna Center", address "Stephansplatz 1"
    When the operator deletes location "L1"
    And the operator requests all locations
    Then the system returns 0 locations

  # ---------------------------------------------------------
  # US-2 Add AC and DC charging points
  # ---------------------------------------------------------
  Scenario: Add an AC charging point to a location
    Given a location exists with id "L1", name "Vienna Center", address "Stephansplatz 1"
    When the operator adds a charging point with id "CP1" and type "AC" and status "AVAILABLE" to location "L1"
    Then location "L1" has 1 charging points

  Scenario: Add a DC charging point to a location
    Given a location exists with id "L1", name "Vienna Center", address "Stephansplatz 1"
    When the operator adds a charging point with id "CP2" and type "DC" and status "OCCUPIED" to location "L1"
    Then location "L1" has 1 charging points

  Scenario: Prevent duplicate charging point id
    Given a location exists with id "L1", name "Vienna Center", address "Stephansplatz 1"
    When the operator adds a charging point with id "CP1" and type "AC" and status "AVAILABLE" to location "L1"
    And the operator tries to add another charging point with id "CP1" and type "DC" and status "AVAILABLE" to location "L1"
    Then the system shows an error containing "already exists"

  Scenario: Update a charging point
    Given a location exists with id "L1", name "Vienna Center", address "Stephansplatz 1"
    And the operator adds a charging point with id "CP1" and type "AC" and status "AVAILABLE" to location "L1"
    When the operator updates charging point "CP1" to location "L1" with type "DC" and status "OCCUPIED"
    And the operator requests all charging points
    Then the charging points include
      | id  | type | status   |
      | CP1 | DC   | OCCUPIED |

  Scenario: Delete a charging point
    Given a location exists with id "L1", name "Vienna Center", address "Stephansplatz 1"
    And the operator adds a charging point with id "CP1" and type "AC" and status "AVAILABLE" to location "L1"
    When the operator deletes charging point "CP1"
    Then location "L1" has 0 charging points
  # ---------------------------------------------------------
  # US-3 View list of all locations
  # ---------------------------------------------------------
  Scenario: View all locations
    Given the network has locations
      | id  | name               | address                 |
      | L1  | Vienna Center      | Stephansplatz 1         |
      | L2  | Graz East          | Hauptstrasse 5          |
      | L3  | Graz North         | Hauptstrasse 7          |
      | L4  | Linz Center        | Landstrasse 12          |
      | L5  | Salzburg West      | Getreidegasse 8         |
      | L6  | Innsbruck Mitte    | Maria-Theresien-Strasse 3 |
      | L7  | Klagenfurt Süd     | Villacher Strasse 20    |
      | L8  | St. Pölten Zentrum | Rathausplatz 1          |
      | L9  | Wels Nord          | Bahnhofstrasse 9        |
      | L10 | Bregenz Hafen      | Seestrasse 4            |
    When the operator requests all locations
    Then the system returns 10 locations
