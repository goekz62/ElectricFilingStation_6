Feature: Manage Locations and Charging Points
  As an operator
  I want to manage locations and their charging points
  So that the charging network can be maintained

  Scenario: add new locations
    Given the network has no locations
    When the operator creates a location with id "L1", name "Vienna Center", address "Stephansplatz 1"
    Then the location list contains a location with id "L1" and name "Vienna Center"

