Feature: Api Feature

  Scenario: Verify status code and response body
    Given I perform a GET request to "ACTIVITIES" with query params
    When the status code is 200
    Then the response body contains "id", "title", "dueDate", "completed"

  Scenario: Test Post request
    Given I perform a POST request to "ACTIVITIES" with query params
    When the status code is 200
    Then check response body information is correct

  Scenario Outline: Test GET request with multiple IDs
    Given I perform a GET request to "ACTIVITIES_BY_ID" with <ID>
    When the status code is 200
    Then the response body contains "id", "title", "dueDate", "completed"

    Examples:
      | ID  |
      | 5   |
      | 11  |
      | 19  |

  Scenario: Perform gradual load test on API
    Given I perform a gradual load test on "ACTIVITIES" starting with 10 users up to 20 users incrementing by 10 every 10 seconds with 10 requests with status 200
