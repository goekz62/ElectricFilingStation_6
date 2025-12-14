Feature: Manage Locations and Charging Points
  As an operator
  I want to manage locations and their charging points
  So that the charging network can be maintained

  Scenario: add new locations
    Given the network has no locations
    When the operator creates a location with id "L1", name "Vienna Center", address "Stephansplatz 1"
    Then the location list contains a location with id "L1" and name "Vienna Center"

  Scenario: add AC and DC points
    Given a location exists with id "L1", name "Vienna Center", address "Stephansplatz 1"
    When the operator adds a charging point with id "CP1" and type "AC" to location "L1"
    And the operator adds a charging point with id "CP2" and type "DC" to location "L1"
    Then location "L1" has 2 charging points
