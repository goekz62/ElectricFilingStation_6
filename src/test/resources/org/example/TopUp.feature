Feature: Top up account before charging session
  As a customer
  I want to top up my prepaid account before a charging session
  So that I always have enough balance to start charging

  Scenario: customer tops up prepaid account successfully
    Given a customer exists with id "C1"
    And customer "C1" has no previous top-ups
    When customer "C1" tops up amount 20.00
    Then customer "C1" balance should be 20.00


  Scenario: customer views price for an AC charging point
    Given a location exists with id "L1", name "Vienna Center", address "Stephansplatz 1"
    And the operator defines a tariff for location "L1" with
      | pricePerKwhAC | pricePerKwhDC | pricePerMinuteAC | pricePerMinuteDC |
      | 0.45          | 0.60          | 0.05             | 0.08             |
    And the locations have charging points
      | id  | location | type | status    |
      | CP1 | L1       | AC   | AVAILABLE |
    When the customer requests current price for charging point "CP1"
    Then the system shows price per kWh 0.45 and price per minute 0.05

  Scenario: customer views price for a DC charging point
    Given a location exists with id "L1", name "Vienna Center", address "Stephansplatz 1"
    And the operator defines a tariff for location "L1" with
      | pricePerKwhAC | pricePerKwhDC | pricePerMinuteAC | pricePerMinuteDC |
      | 0.45          | 0.60          | 0.05             | 0.08             |
    And the locations have charging points
      | id  | location | type | status    |
      | CP2 | L1       | DC   | AVAILABLE |
    When the customer requests current price for charging point "CP2"
    Then the system shows price per kWh 0.60 and price per minute 0.08
