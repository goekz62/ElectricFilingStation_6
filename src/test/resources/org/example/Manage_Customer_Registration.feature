Feature: Customer Registration Management
  As a customer
  I want to manage my account
  So that I can use the charging network

  Scenario: create an account
    Given there are no customers
    When a customer is created with name "Marta" and lastname "Mueller"
    Then the customer list contains a customer with name "Marta" and lastname "Mueller"
    And the system generates a customer id starting with "C"