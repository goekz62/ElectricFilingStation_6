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


  Scenario: view current price for each location
    Given the network has locations
      | id | name          | address         |
      | L1 | Vienna Center | Stephansplatz 1 |
      | L2 | Graz East     | Hauptstrasse 5  |
    And the operator defines a tariff for location "L1" with
      | pricePerKwhAC | pricePerKwhDC | pricePerMinuteAC | pricePerMinuteDC |
      | 0.45          | 0.60          | 0.05             | 0.08             |
    And the operator defines a tariff for location "L2" with
      | pricePerKwhAC | pricePerKwhDC | pricePerMinuteAC | pricePerMinuteDC |
      | 0.40          | 0.55          | 0.04             | 0.07             |
    When the operator requests current prices for all locations
    Then the system returns prices for 2 locations
