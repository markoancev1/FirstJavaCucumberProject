Feature: Api Feature

  Scenario: Verify status code and response body
    Given I perform a GET request to "ACTIVITIES" with query params
    Then the status code is 200
    And the number of objects in the response is 30
    And the response body contains "id", "title", "dueDate", "completed"