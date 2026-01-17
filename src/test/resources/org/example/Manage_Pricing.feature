Feature: Manage Pricing (Operator)
  As an operator
  I want to define prices for AC and DC charging
  So that customers are charged correctly per location

  Scenario: define prices for AC and DC points
    Given a location exists with id "L1", name "Vienna Center", address "Stephansplatz 1"
    When the operator defines a tariff for location "L1" with
      | pricePerKwhAC | pricePerKwhDC | pricePerMinuteAC | pricePerMinuteDC |
      | 0.45          | 0.60          | 0.05             | 0.08             |
    Then location "L1" has tariff
      | pricePerKwhAC | pricePerKwhDC | pricePerMinuteAC | pricePerMinuteDC |
      | 0.45          | 0.60          | 0.05             | 0.08             |
