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
