Feature: Api Feature

  Scenario: Verify status code and response body
    Given I perform a GET request to "ACTIVITIES" with query params
    Then the status code is 200
    And the response body contains "id", "title", "dueDate", "completed"

  Scenario: Test Post request
    Given I perform a POST request to "ACTIVITIES" with query params
    Then the status code is 200
    And check response body information is correct

