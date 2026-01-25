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
      | kWhAC | kWhDC | minAC | minDC | timePeriod | startTime | endTime |
      | 0.20  | 0.30  | 0.09  | 0.12  | DAY        | 06:00     | 18:00   |
    And the operator defines a tariff for location "L2" with:
      | kWhAC | kWhDC | minAC | minDC | timePeriod | startTime | endTime |
      | 0.25  | 0.35  | 0.10  | 0.15  | DAY        | 06:00     | 18:00   |
    And the current time is "2026-01-17T10:00"
    When the operator requests current prices for all locations
    Then the system returns current prices for 2 locations
    And location "L1" current price is
      | kWhAC | kWhDC | minAC | minDC | timePeriod | startTime | endTime |
      | 0.20  | 0.30  | 0.09  | 0.12  | DAY        | 06:00     | 18:00   |
    And location "L2" current price is
      | kWhAC | kWhDC | minAC | minDC | timePeriod | startTime | endTime |
      | 0.25  | 0.35  | 0.10  | 0.15  | DAY        | 06:00     | 18:00   |

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

  # ------------------------------------------------------------
  # US-10 - Filter charging points
  # ------------------------------------------------------------
  Scenario: Filter charging points by location, type, and price
    Given the network has locations
      | id | name          | address         |
      | L1 | Vienna Center | Stephansplatz 1 |
      | L2 | Graz East     | Hauptstrasse 5  |
    And the network has charging points
      | id  | locationId | type | status    |
      | CP1 | L1         | AC   | AVAILABLE |
      | CP2 | L1         | DC   | AVAILABLE |
      | CP3 | L2         | AC   | AVAILABLE |
    And the operator defines a tariff for location "L1" with:
      | kWhAC | kWhDC | minAC | minDC | timePeriod | startTime | endTime |
      | 0.20  | 0.45  | 0.09  | 0.12  | DAY        | 06:00     | 18:00   |
    And the operator defines a tariff for location "L2" with:
      | kWhAC | kWhDC | minAC | minDC | timePeriod | startTime | endTime |
      | 0.30  | 0.50  | 0.10  | 0.15  | DAY        | 06:00     | 18:00   |
    And the current time is "2026-01-17T10:00"
    When the operator filters charging points at location "L1" with type "AC" and max price 0.25
    Then the system returns 1 filtered charging points
    And the filtered charging points include
      | id  |
      | CP1 |

  # ------------------------------------------------------------
  # Network status list per location
  # ------------------------------------------------------------
  Scenario: View network status per location with current prices and charger states
    Given the network has locations
      | id | name          | address         |
      | L1 | Vienna Center | Stephansplatz 1 |
    And the network has charging points
      | id  | locationId | type | status      |
      | CP1 | L1         | AC   | AVAILABLE   |
      | CP2 | L1         | DC   | OUT_OF_ORDER|
    And the operator defines a tariff for location "L1" with:
      | kWhAC | kWhDC | minAC | minDC | timePeriod | startTime | endTime |
      | 0.20  | 0.30  | 0.09  | 0.12  | DAY        | 06:00     | 18:00   |
    And the current time is "2026-01-17T10:00"
    When the operator requests the network status
    Then the network status includes location "L1" with tariff
      | kWhAC | kWhDC | minAC | minDC | timePeriod | startTime | endTime |
      | 0.20  | 0.30  | 0.09  | 0.12  | DAY        | 06:00     | 18:00   |
    And the network status includes charging points for location "L1"
      | id  |
      | CP1 |
      | CP2 |
