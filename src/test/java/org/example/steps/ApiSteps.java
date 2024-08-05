package org.example.steps;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.example.pojo.Activity;
import org.example.utils.ApiEndpoint;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Class for defining API step definitions for Cucumber.
 */
public class ApiSteps {
    private static final Logger logger = LoggerFactory.getLogger(ApiSteps.class);
    private Response response;

    /**
     * Asserts that the status code of the response matches the expected status code.
     *
     * @param statusCode the expected status code
     */
    @Then("the status code is {int}")
    public void theStatusCodeIs(int statusCode) {
        logger.info("Verifying that the status code is {}", statusCode);
        Assertions.assertNotNull(response, "Response is null");
        Assertions.assertEquals(statusCode, response.getStatusCode(), "Status code does not match");
    }

    /**
     * Asserts that the response body contains the specified strings.
     *
     * @param arg0 the first string to check
     * @param arg1 the second string to check
     * @param arg2 the third string to check
     * @param arg3 the fourth string to check
     */
    @And("the response body contains {string}, {string}, {string}, {string}")
    public void theResponseBodyContains(String arg0, String arg1, String arg2, String arg3) {
        logger.info("Checking if response body contains: {}, {}, {}, {}", arg0, arg1, arg2, arg3);
        Assertions.assertNotNull(response, "Response is null");
        String responseBody = response.getBody().asString();
        logger.debug("Response body: {}", responseBody);
        assertResponseBodyContains(responseBody, arg0, arg1, arg2, arg3);
    }

    private void assertResponseBodyContains(String responseBody, String... args) {
        for (String arg : args) {
            Assertions.assertTrue(responseBody.contains(arg), "Response body does not contain: " + arg);
        }
    }

    /**
     * Asserts that the number of objects in the response matches the expected count.
     *
     * @param count the expected count of objects
     */
    @And("the number of objects in the response is {int}")
    public void theNumberOfObjectsInTheResponseIs(int count) {
        logger.info("Verifying that the number of objects in the response is {}", count);
        Assertions.assertNotNull(response, "Response is null");
        List<Object> objects = getResponseObjects();
        Assertions.assertEquals(count, objects.size(), "Number of objects does not match");
    }

    private List<Object> getResponseObjects() {
        JsonPath jsonPath = new JsonPath(response.getBody().asString());
        return jsonPath.getList("$");
    }

    /**
     * Performs a GET request to the specified API endpoint.
     *
     * @param enumName the name of the API endpoint enum
     */
    @Given("I perform a GET request to {string} with query params")
    public void iPerformAGETRequestToWithQueryParams(String enumName) {
        logger.info("Performing GET request to endpoint: {}", enumName);
        ApiEndpoint endpoint = ApiEndpoint.valueOf(enumName.toUpperCase());
        Assertions.assertNotNull(endpoint, "Endpoint is null");
        response = RestAssured.get(endpoint.getUrl());
        Assertions.assertNotNull(response, "Response is null");
        logger.info("Received response with status code: {}", response.getStatusCode());
    }

    /**
     * Performs a POST request to the specified API endpoint.
     *
     * @param enumName the name of the API endpoint enum
     */
    @Given("I perform a POST request to {string} with query params")
    public void iPerformAPOSTRequestToWithQueryParams(String enumName) throws JsonProcessingException {
        logger.info("Performing POST request to endpoint: {}", enumName);
        ApiEndpoint endpoint = ApiEndpoint.valueOf(enumName.toUpperCase());
        Assertions.assertNotNull(endpoint, "Endpoint is null");

        Activity activity = new Activity();
        activity.setId(31);
        activity.setTitle("Activity 30");
        activity.setDueDate("2024-07-30T11:58:38.538Z");
        activity.setCompleted(false);

        String jsonBody = activity.serialize();
        logger.debug("POST request body: {}", jsonBody);

        response = RestAssured.given()
                .contentType("application/json")
                .body(jsonBody)
                .post(endpoint.getUrl());
        Assertions.assertNotNull(response, "Response is null");
        logger.info("Received response with status code: {}", response.getStatusCode());
    }

    /**
     * Checks if the response body information is correct.
     *
     * @throws JsonProcessingException if there is an error processing JSON
     */
    @And("check response body information is correct")
    public void checkResponseBodyInformationIsCorrect() throws JsonProcessingException {
        logger.info("Checking if the response body information is correct");
        String responseBody = response.getBody().asString();
        Activity responseActivity = Activity.deserialize(responseBody);

        Assertions.assertEquals(31, responseActivity.getId(), "Activity ID does not match");
        Assertions.assertEquals("Activity 30", responseActivity.getTitle(), "Activity title does not match");
        Assertions.assertEquals("2024-07-30T11:58:38.538Z", responseActivity.getDueDate(), "Activity due date does not match");
        Assertions.assertFalse(responseActivity.isCompleted(), "Activity should not be completed");
        logger.info("Response body information is correct");
    }
}