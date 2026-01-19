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
    And location "L1" has tariff
      | kWhAC | kWhDC | minAC | minDC |
      | 20    | 15    | 0.09  | 0.01  |
    And location "L2" has tariff
      | kWhAC | kWhDC | minAC | minDC |
      | 18    | 25    | 0.08  | 0.03  |

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
    Then the system shows charging speed kWhPerHour 20 and pricePerMinute 0.09

  Scenario: Customer views current price for a selected charging point (DC)
    When the customer requests current price for charging point "CP2"
    Then the system shows charging speed kWhPerHour 15 and pricePerMinute 0.01