Feature: Static setup requirements
  As an operator
  I want to ensure the initial network setup meets the required scope
  So that the system fulfills the baseline project constraints

  Scenario: Seed data meets minimum scope requirements
    Given the seed network is loaded
    Then the system has 10 locations and 5 customers
    And each location has between 2 and 5 charging points
