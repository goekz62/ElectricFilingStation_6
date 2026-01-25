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
      | kWhAC | kWhDC | minAC | minDC |
      | 0.20  | 0.15  | 0.09  | 0.01  |
    Then location "L1" has tariff:
      | kWhAC | kWhDC | minAC | minDC |
      | 0.20  | 0.15  | 0.09  | 0.01  |

  # =========================
  # US-7 Update prices
  # =========================
  Scenario: Update pricing information for a location
    And the operator defines a tariff for location "L1" with:
      | kWhAC | kWhDC | minAC | minDC |
      | 0.20  | 0.15  | 0.09  | 0.01  |
    When the operator updates the tariff for location "L1" to:
      | kWhAC | kWhDC | minAC | minDC |
      | 0.22  | 0.18  | 0.10  | 0.02  |
    Then location "L1" has tariff:
      | kWhAC | kWhDC | minAC | minDC |
      | 0.22  | 0.18  | 0.10  | 0.02  |

  Scenario: Define tariffs for multiple locations
    When the operator defines a tariff for location "L1" with:
      | kWhAC | kWhDC | minAC | minDC |
      | 0.20  | 0.15  | 0.09  | 0.01  |
    And the operator defines a tariff for location "L2" with:
      | kWhAC | kWhDC | minAC | minDC |
      | 0.25  | 0.18  | 0.10  | 0.02  |
    Then location "L1" has tariff:
      | kWhAC | kWhDC | minAC | minDC |
      | 0.20  | 0.15  | 0.09  | 0.01  |
    And location "L2" has tariff:
      | kWhAC | kWhDC | minAC | minDC |
      | 0.25  | 0.18  | 0.10  | 0.02  |

  Scenario: Prevent defining tariffs for an unknown location
    When the operator defines a tariff for location "L99" with:
      | kWhAC | kWhDC | minAC | minDC |
      | 0.20  | 0.15  | 0.09  | 0.01  |
    Then the system shows an error containing "Location not found"
