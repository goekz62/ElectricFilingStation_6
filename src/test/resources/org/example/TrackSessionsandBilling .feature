Feature: View Charging Session Information
  As an operator
  I want to view charging session information
  So that I can monitor charging sessions

  Scenario: view charging session information by id
    Given a charging session exists
      | id | customerId | chargingPointId | startTime            | endTime              | kWhCharged | totalCost | status   |
      | S1 | C1         | CP1             | 2026-01-17T10:00     | 2026-01-17T10:30     | 12.5       | 7.80      | FINISHED |
    When the operator requests charging session "S1"
    Then the session shows customer "C1" and charging point "CP1"
    And the session has start time "2026-01-17T10:00" and end time "2026-01-17T10:30"
    And the session has kWh charged 12.5 and total cost 7.80
    And the session status is "FINISHED"


  Scenario: view invoices (with session details) and top-ups for a customer
    Given there are no customers
    When a customer is created with name "Marta" and lastname "Mueller"
    And the customer has top-ups
      | id | amount | dateTime         |
      | T1 | 20.00  | 2026-01-17T09:00 |
      | T2 | 15.00  | 2026-01-17T09:30 |
    And the customer has invoices
      | invoiceId | sessionId | chargingPointId | startTime        | endTime          | kWhCharged | totalCost | status |
      | I1        | S1        | CP1             | 2026-01-17T10:00 | 2026-01-17T10:30 | 12.5       | 7.80      | PAID   |
      | I2        | S2        | CP2             | 2026-01-17T12:00 | 2026-01-17T12:20 | 8.0        | 4.80      | OPEN   |
    When the operator requests billing history for that customer
    Then the system returns 2 top-ups and 2 invoices
    And invoice "I1" includes session "S1" on charging point "CP1" with total cost 7.80