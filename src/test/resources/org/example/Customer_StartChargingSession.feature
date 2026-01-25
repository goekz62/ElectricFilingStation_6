Feature: Customer EPIC 4 - Start Charging Session
  As a customer, I want to start and monitor my charging session
  so that I can understand energy usage, charging cost, and session progress.

  Background:
    Given the network has locations
      | id | name          | address         |
      | L1 | Vienna Center | Stephansplatz 1 |
    And the network has charging points
      | id  | locationId | type | status    |
      | CP1 | L1         | AC   | AVAILABLE |
    And the current time is "2026-01-17T10:00"
    And location "L1" has tariff
      | kWhAC | kWhDC | minAC | minDC | timePeriod | startTime | endTime |
      | 0.20  | 0.30  | 0.09  | 0.12  | DAY        | 06:00     | 18:00   |
    And there are customers
      | firstName | lastName |
      | Nisa      | Yesillik |
    And the customer identifies as first name "Nisa" and last name "Yesillik"
    And the customer has balance 50.00

  # ---------------------------
  # US-9 Start charging session
  # ---------------------------
  Scenario: Customer starts a charging session on an available charging point
    When the customer starts charging session on charging point "CP1"
    Then a new charging session is created and is ACTIVE
    And the session belongs to the identified customer
    And the session uses charging point "CP1"

  # ---------------------------
  # US-10 View charging session information
  # ---------------------------
  Scenario: Customer views charging session info after 30 minutes
    Given the customer has an active charging session on charging point "CP1"
    When 30 minutes pass for that session
    And the customer requests charging session information
    Then the session shows duration 30 minutes
    And the session shows charged energy 5.50 kWh
    And the session shows total cost 3.80

  # ---------------------------
  # Edge case: insufficient balance
  # ---------------------------
  Scenario: Customer cannot start a charging session without balance
    Given the customer has no balance
    When the customer attempts to start charging session on charging point "CP1"
    Then the system shows an error containing "balance"
