Feature: EPIC 3 - Network Monitoring
  As an operator
  I want to monitor the status, availability, and configuration of all charging points
  So that I can maintain optimal system performance

  # ------------------------------------------------------------
  # US-8 - View list of available charging points
  # ------------------------------------------------------------
  Scenario: View all available charging points
    Given the network has charging points
      | id  | locationId | type | status      |
      | CP1 | L1         | AC   | AVAILABLE   |
      | CP2 | L1         | DC   | OCCUPIED    |
      | CP3 | L2         | DC   | OUT_OF_ORDER|
      | CP4 | L2         | AC   | AVAILABLE   |
    When the operator requests all available charging points
    Then the system returns 2 available charging points
    And the available charging points include
      | id  |
      | CP1 |
      | CP4 |

  # ------------------------------------------------------------
  # US-9 - View current price for each location
  # ------------------------------------------------------------
  Scenario: View current price for each location
    Given the network has locations
      | id | name          | address         |
      | L1 | Vienna Center | Stephansplatz 1 |
      | L2 | Graz East     | Hauptstrasse 5  |
    And the operator defines a tariff for location "L1" with:
      | kWhAC | kWhDC | minAC | minDC |
      | 0.20  | 0.15  | 0.09  | 0.01  |
    And the operator defines a tariff for location "L2" with:
      | kWhAC | kWhDC | minAC | minDC |
      | 0.25  | 0.18  | 0.10  | 0.02  |
    When the operator requests current prices for all locations
    Then the system returns current prices for 2 locations
    And location "L1" current price is
      | kWhAC | kWhDC | minAC | minDC |
      | 0.20  | 0.15  | 0.09  | 0.01  |
    And location "L2" current price is
      | kWhAC | kWhDC | minAC | minDC |
      | 0.25  | 0.18  | 0.10  | 0.02  |

  # ------------------------------------------------------------
  # Edge case - no available charging points
  # ------------------------------------------------------------
  Scenario: View available charging points when none are available
    Given the network has charging points
      | id  | locationId | type | status       |
      | CP1 | L1         | AC   | OCCUPIED     |
      | CP2 | L1         | DC   | OUT_OF_ORDER |
    When the operator requests all available charging points
    Then the system returns 0 available charging points
