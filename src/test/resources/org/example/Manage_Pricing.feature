Feature: Manage Pricing (Operator)
  As an operator
  I want to manage tariffs
  So that pricing is correct

  Scenario: define prices for AC and DC points
    Given a location exists with id "L1", name "Vienna Center", address "Stephansplatz 1"
    When the operator defines a tariff for location "L1" with
      | pricePerKwhAC | pricePerKwhDC | pricePerMinuteAC | pricePerMinuteDC |
      | 0.45          | 0.60          | 0.05             | 0.08             |
    Then location "L1" has tariff
      | pricePerKwhAC | pricePerKwhDC | pricePerMinuteAC | pricePerMinuteDC |
      | 0.45          | 0.60          | 0.05             | 0.08             |


  Scenario: update tariff for an existing location
    Given a location exists with id "L1", name "Vienna Center", address "Stephansplatz 1"
    And the operator defines a tariff for location "L1" with
      | pricePerKwhAC | pricePerKwhDC | pricePerMinuteAC | pricePerMinuteDC |
      | 0.45          | 0.60          | 0.05             | 0.08             |
    When the operator updates the tariff for location "L1" to
      | pricePerKwhAC | pricePerKwhDC | pricePerMinuteAC | pricePerMinuteDC |
      | 0.50          | 0.70          | 0.06             | 0.10             |
    Then location "L1" has tariff
      | pricePerKwhAC | pricePerKwhDC | pricePerMinuteAC | pricePerMinuteDC |
      | 0.50          | 0.70          | 0.06             | 0.10             |