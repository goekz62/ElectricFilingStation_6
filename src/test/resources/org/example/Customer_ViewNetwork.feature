Feature: Customer EPIC 3 - View Network
  As a customer, I want to view the entire charging network including charger types, prices, and availability
  so that I can easily find the most suitable charging point for my needs.

  Background:
    Given the network has locations
      | id | name          | address         |
      | L1 | Vienna Center | Stephansplatz 1 |
      | L2 | Graz East     | Hauptstrasse 5  |
    And the network has charging points
      | id  | locationId | type | status       |
      | CP1 | L1         | AC   | AVAILABLE    |
      | CP2 | L1         | DC   | OCCUPIED     |
      | CP3 | L2         | DC   | OUT_OF_ORDER |
      | CP4 | L2         | AC   | AVAILABLE    |
    And the current time is "2026-01-17T10:00"
    And location "L1" has tariff
      | kWhAC | kWhDC | minAC | minDC | timePeriod | startTime | endTime |
      | 0.20  | 0.30  | 0.09  | 0.12  | DAY        | 06:00     | 18:00   |
    And location "L2" has tariff
      | kWhAC | kWhDC | minAC | minDC | timePeriod | startTime | endTime |
      | 0.18  | 0.25  | 0.08  | 0.12  | DAY        | 06:00     | 18:00   |

  # ---------------------------
  # US-6 View all AC/DC charging points
  # ---------------------------
  Scenario: Customer views all charging points
    When the customer requests all charging points
    Then the system returns 4 charging points
    And the charging points include
      | id  | type | status       |
      | CP1 | AC   | AVAILABLE    |
      | CP2 | DC   | OCCUPIED     |
      | CP3 | DC   | OUT_OF_ORDER |
      | CP4 | AC   | AVAILABLE    |

  # ---------------------------
  # US-7 View current price for a selected charging point
  # ---------------------------
  Scenario: Customer views current price for a selected charging point (AC)
    When the customer requests current price for charging point "CP1"
    Then the system shows price per kWh 0.20 and parking per minute 0.09

  Scenario: Customer views current price for a selected charging point (DC)
    When the customer requests current price for charging point "CP2"
    Then the system shows price per kWh 0.30 and parking per minute 0.12

  # ---------------------------
  # Edge case: no charging points
  # ---------------------------
  Scenario: Customer views charging points when none exist
    Given the network is empty
    When the customer requests all charging points
    Then the system returns 0 charging points

  # ---------------------------
  # US-8 Filter charging points
  # ---------------------------
  Scenario: Customer filters charging points by location and type
    When the customer filters charging points at location "L1" with type "AC" and max price 0.25
    Then the system returns 1 filtered charging points
    And the filtered charging points include
      | id  |
      | CP1 |
