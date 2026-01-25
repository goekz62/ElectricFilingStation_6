Feature: EPIC 4 - Track Sessions and Billing
  As an operator
  I want to track charging sessions, invoices, and customer top-ups
  So that I can support customers, validate billing, and maintain financial accuracy

  # ------------------------------------------------------------
  # US-11 - View charging session information
  # ------------------------------------------------------------
  Scenario:  view detailed charging session information
    Given a charging session exists
      | id | customerId | chargingPointId | startTime          | endTime            | kWhCharged | totalCost | status   |
      | S1 | C1         | CP1             | 2026-01-17T10:00   | 2026-01-17T10:30   | 12.5       | 7.80      | FINISHED |
    When the operator requests charging session "S1"
    Then the session shows customer "C1" and charging point "CP1"
    And the session has start time "2026-01-17T10:00" and end time "2026-01-17T10:30"
    And the session has kWh charged 12.5 and total cost 7.80
    And the session status is "FINISHED"

  # ------------------------------------------------------------
  # US-12 - View invoices and top-up history of customers
  # ------------------------------------------------------------
  Scenario:  view top-up history and invoices for a customer
    Given a customer exists with id "C1"
    And the customer has top-ups
      | id | amount | dateTime          |
      | T1 | 20.00  | 2026-01-17T09:00  |
      | T2 | 15.00  | 2026-01-17T09:30  |
    And the customer has invoices
      | invoiceId | sessionId | chargingPointId | startTime        | endTime          | kWhCharged | totalCost | status |
      | I1        | S1        | CP1             | 2026-01-17T10:00 | 2026-01-17T10:30 | 12.5       | 7.80      | PAID   |
    When the operator requests billing history for that customer
    Then the system returns 2 top-ups and 1 invoices
    And invoice "I1" includes session "S1" on charging point "CP1" with total cost 7.80

  # ------------------------------------------------------------
  # Edge case - customer without billing history
  # ------------------------------------------------------------
  Scenario: view empty billing history for a customer
    Given a customer exists with id "C2"
    When the operator requests billing history for that customer
    Then the system returns 0 top-ups and 0 invoices

  # ------------------------------------------------------------
  # US-13 - Manually correct customer balance
  # ------------------------------------------------------------
  Scenario: manually correct customer balance
    Given a customer exists with id "C3"
    When the operator corrects the customer balance by 5.00 with reason "manual correction"
    Then the customer balance is 5.00
    And the system returns 1 balance adjustments
