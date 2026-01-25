Feature: Customer EPIC 1 - Customer Registration Management

  As a customer
  I want to register and get a customer ID
  so that I can access charging services.

  Scenario: Create a new customer account
    Given there are no customers
    When a customer registers with first name "Nisa" and last name "Yesillik"
    Then the system creates the customer
    And the customer id starts with "C"
    And the customer data is first name "Nisa" and last name "Yesillik"

  Scenario: Prevent registration with missing name
    Given there are no customers
    When a customer registers with first name "" and last name "Yesillik"
    Then the system shows an error containing "first name"

  Scenario: Update customer account information
    Given there are no customers
    When a customer registers with first name "Nisa" and last name "Yesillik"
    And the customer updates their name to first name "Nisa" and last name "Yildiz"
    Then the customer data is first name "Nisa" and last name "Yildiz"

  Scenario: Delete customer account
    Given there are no customers
    When a customer registers with first name "Nisa" and last name "Yesillik"
    And the customer deletes their account
    Then the system returns 0 customers
