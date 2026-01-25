Feature: Customer EPIC 2 - Manage Customer Prepaid Balance

  As a customer
  I want to manage my prepaid balance (top up, view invoices, view balance) and identify myself
  so that I can control my charging costs.

  Background:
    Given there are customers
      | firstName | lastName |
      | Nisa      | Yesillik |
      | Max       | Huber    |

  # -------------------------------------------------
  # US-5 – Customer Identification
  # -------------------------------------------------
  Scenario: Customer identifies themselves by first and last name
    When the customer identifies as first name "Nisa" and last name "Yesillik"
    Then the system recognizes the customer

  # -------------------------------------------------
  # US-3 – Top up account before charging session
  # -------------------------------------------------
  Scenario: Customer tops up their prepaid balance
    Given the customer identifies as first name "Nisa" and last name "Yesillik"
    And the customer has no top-ups yet
    When the customer tops up amount 20.00
    Then the customer balance is 20.00

  Scenario: Customer cannot top up with a negative amount
    Given the customer identifies as first name "Nisa" and last name "Yesillik"
    When the customer attempts to top up amount -5.00
    Then the system shows an error containing "must be > 0"

  # -------------------------------------------------
  # US-4 – View all invoices and current balance
  # -------------------------------------------------
  Scenario: Customer views invoices and current balance
    Given the customer identifies as first name "Nisa" and last name "Yesillik"
    And the customer has top-ups
      | id | amount | dateTime         |
      | T1 | 30.00  | 2026-01-17T09:00 |
      | T2 | 10.00  | 2026-01-17T09:30 |
    And the customer has invoices
      | invoiceId | sessionId | chargingPointId | startTime        | endTime          | kWhCharged | totalCost | status |
      | I1        | S1        | CP1             | 2026-01-17T10:00 | 2026-01-17T10:30 | 12.50      | 7.80      | PAID   |
      | I1        | S2        | CP2             | 2026-01-17T11:00 | 2026-01-17T11:10 | 5.00       | 4.20      | PAID   |
    When the customer requests their invoices and balance
    Then the system returns 1 invoices
    And invoice "I1" has 2 sessions
    And the customer balance is 28.00
