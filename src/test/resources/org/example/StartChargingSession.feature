Feature: Start charging session

  As a customer
  I want to select a charging point and start charging
  So that I can charge my vehicle

  Scenario: start charging successfully
    Given a customer exists with id "C1"
    And the customer has a balance of 20.00
    And a charging point "CP1" exists with status "AVAILABLE"
    When the customer starts charging at "CP1"
    Then a charging session is created
    And the session status is "ACTIVE"
    And the charging point "CP1" status is "OCCUPIED"

  Scenario: cannot start charging if charging point is not available
    Given a customer exists with id "C1"
    And the customer has a balance of 20.00
    And a charging point "CP2" exists with status "OUT_OF_ORDER"
    When the customer starts charging at "CP2"
    Then the charging session is denied

  Scenario: cannot start charging if balance is insufficient
    Given a customer exists with id "C1"
    And the customer has a balance of 0.00
    And a charging point "CP3" exists with status "AVAILABLE"
    When the customer starts charging at "CP3"
    Then the charging session is denied


  Scenario: start charging session at a charging point (US-9)
    Given a customer exists
    And a location exists with tariff
    And a charging point "CP1" exists at location "L1" with type "AC" and status "AVAILABLE"
    When the customer starts charging at charging point "CP1"
    Then a new charging session is created and is ACTIVE

  Scenario: view real-time charging session information (US-10)
    Given an active charging session exists for customer at charging point "CP1"
    When the customer requests live view for that session
    Then the session status is "ACTIVE"
    And the session live values have kWhCharged > 0 and totalCost > 0

