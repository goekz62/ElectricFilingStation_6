Feature: Monitor Network (Operator)
  As an operator
  I want to monitor the charging network
  So that I can see all locations and their charging points

  Scenario: view list of all locations
    Given the network has locations
      | id | name          | address         |
      | L1 | Vienna Center | Stephansplatz 1 |
      | L2 | Graz East     | Hauptstrasse 5  |
    When the operator requests all locations
    Then the system returns 2 locations

  Scenario: view list of all charging points
    Given the locations have charging points
      | id  | location         | type |
      | CP1 | L1               | AC   |
      | CP2 | L1               | DC   |
      | CP3 | L2               | DC   |
    When the operator requests all charging points
    Then the system returns 3 charging points


