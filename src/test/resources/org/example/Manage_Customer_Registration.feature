Feature: Customer Registration Management
  As a customer
  I want to manage my account
  So that I can use the charging network

  Scenario: create an account
    Given there are no customers
    When a customer is created with id "C1", name "Marta" and lastname "Mueller"
    Then the customer list contains a customer with id "C1" and name "Marta" and lastname "Mueller"
