Feature: Top up account before charging session
  As a customer
  I want to top up my prepaid account before a charging session
  So that I always have enough balance to start charging

  Scenario: customer tops up prepaid account successfully
    Given a customer exists with id "C1"
    And customer "C1" has no previous top-ups
    When customer "C1" tops up amount 20.00
    Then customer "C1" balance should be 20.00
