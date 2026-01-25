Feature: EPIC 2 - Manage Pricing
  As an operator
  I want to define and update AC/DC charging prices per location
  So that charging sessions are billed correctly using current tariffs

  Background:
    Given the network has locations
      | id | name           | address           |
      | L1 | Vienna Center  | Stephansplatz 1   |
      | L2 | Graz East      | Hauptstrasse 5    |

  # =========================
  # US-6 Define prices
  # =========================

  Scenario: Define AC and DC prices for a location
    When the operator defines a tariff for location "L1" with:
      | kWhAC | kWhDC | minAC | minDC | timePeriod | startTime | endTime |
      | 0.20  | 0.30  | 0.09  | 0.12  | DAY        | 06:00     | 18:00   |
    Then location "L1" has tariff:
      | kWhAC | kWhDC | minAC | minDC | timePeriod | startTime | endTime |
      | 0.20  | 0.30  | 0.09  | 0.12  | DAY        | 06:00     | 18:00   |

  Scenario: Define at least 5 different tariffs with different time periods for one location
    When the operator defines a tariff for location "L1" with:
      | kWhAC | kWhDC | minAC | minDC | timePeriod | startTime | endTime |
      | 0.20  | 0.30  | 0.09  | 0.12  | P1         | 00:00     | 04:00   |
    And the operator defines a tariff for location "L1" with:
      | kWhAC | kWhDC | minAC | minDC | timePeriod | startTime | endTime |
      | 0.21  | 0.31  | 0.10  | 0.13  | P2         | 04:00     | 08:00   |
    And the operator defines a tariff for location "L1" with:
      | kWhAC | kWhDC | minAC | minDC | timePeriod | startTime | endTime |
      | 0.22  | 0.32  | 0.11  | 0.14  | P3         | 08:00     | 12:00   |
    And the operator defines a tariff for location "L1" with:
      | kWhAC | kWhDC | minAC | minDC | timePeriod | startTime | endTime |
      | 0.23  | 0.33  | 0.12  | 0.15  | P4         | 12:00     | 16:00   |
    And the operator defines a tariff for location "L1" with:
      | kWhAC | kWhDC | minAC | minDC | timePeriod | startTime | endTime |
      | 0.24  | 0.34  | 0.13  | 0.16  | P5         | 16:00     | 20:00   |
    Then location "L1" has tariff:
      | kWhAC | kWhDC | minAC | minDC | timePeriod | startTime | endTime |
      | 0.20  | 0.30  | 0.09  | 0.12  | P1         | 00:00     | 04:00   |
    And location "L1" has tariff:
      | kWhAC | kWhDC | minAC | minDC | timePeriod | startTime | endTime |
      | 0.21  | 0.31  | 0.10  | 0.13  | P2         | 04:00     | 08:00   |
    And location "L1" has tariff:
      | kWhAC | kWhDC | minAC | minDC | timePeriod | startTime | endTime |
      | 0.22  | 0.32  | 0.11  | 0.14  | P3         | 08:00     | 12:00   |
    And location "L1" has tariff:
      | kWhAC | kWhDC | minAC | minDC | timePeriod | startTime | endTime |
      | 0.23  | 0.33  | 0.12  | 0.15  | P4         | 12:00     | 16:00   |
    And location "L1" has tariff:
      | kWhAC | kWhDC | minAC | minDC | timePeriod | startTime | endTime |
      | 0.24  | 0.34  | 0.13  | 0.16  | P5         | 16:00     | 20:00   |


  # =========================
  # US-7 Update prices
  # =========================
  Scenario: Update pricing information for a location
    And the operator defines a tariff for location "L1" with:
      | kWhAC | kWhDC | minAC | minDC | timePeriod | startTime | endTime |
      | 0.20  | 0.30  | 0.09  | 0.12  | DAY        | 06:00     | 18:00   |
    When the operator updates the tariff for location "L1" to:
      | kWhAC | kWhDC | minAC | minDC | timePeriod | startTime | endTime |
      | 0.22  | 0.32  | 0.10  | 0.14  | DAY        | 06:00     | 18:00   |
    Then location "L1" has tariff:
      | kWhAC | kWhDC | minAC | minDC | timePeriod | startTime | endTime |
      | 0.22  | 0.32  | 0.10  | 0.14  | DAY        | 06:00     | 18:00   |

  Scenario: Define tariffs for multiple locations
    When the operator defines a tariff for location "L1" with:
      | kWhAC | kWhDC | minAC | minDC | timePeriod | startTime | endTime |
      | 0.20  | 0.30  | 0.09  | 0.12  | DAY        | 06:00     | 18:00   |
    And the operator defines a tariff for location "L2" with:
      | kWhAC | kWhDC | minAC | minDC | timePeriod | startTime | endTime |
      | 0.25  | 0.35  | 0.10  | 0.15  | NIGHT      | 18:00     | 06:00   |
    Then location "L1" has tariff:
      | kWhAC | kWhDC | minAC | minDC | timePeriod | startTime | endTime |
      | 0.20  | 0.30  | 0.09  | 0.12  | DAY        | 06:00     | 18:00   |
    And location "L2" has tariff:
      | kWhAC | kWhDC | minAC | minDC | timePeriod | startTime | endTime |
      | 0.25  | 0.35  | 0.10  | 0.15  | NIGHT      | 18:00     | 06:00   |

  Scenario: Prevent defining tariffs for an unknown location
    When the operator defines a tariff for location "L99" with:
      | kWhAC | kWhDC | minAC | minDC | timePeriod | startTime | endTime |
      | 0.20  | 0.30  | 0.09  | 0.12  | DAY        | 06:00     | 18:00   |
    Then the system shows an error containing "Location not found"
