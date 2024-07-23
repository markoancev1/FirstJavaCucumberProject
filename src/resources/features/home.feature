Feature: Home page feature


  Scenario Outline: Check elements visibility on home page in different browsers and enter emails
    Given I have a configured Cucumber-JVM project
    When I read data from file "<filePath>" column "<columnName>"
    And I run the tests
    Then they should pass without any failures

    Examples:
      | filePath                                      | columnName |
      | src/test/java/org/example/data/Data.xlsx      | email      |
      | src/test/java/org/example/data/Data.csv       | email      |
