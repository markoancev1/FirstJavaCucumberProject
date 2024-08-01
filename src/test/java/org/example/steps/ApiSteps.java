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
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
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
        Assert.assertNotNull("Response is null", response);
        Assert.assertEquals("Status code does not match", statusCode, response.getStatusCode());
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
        Assert.assertNotNull("Response is null", response);
        String responseBody = response.getBody().asString();
        logger.info(responseBody);
        assertResponseBodyContains(responseBody, arg0, arg1, arg2, arg3);
    }

    private void assertResponseBodyContains(String responseBody, String... args) {
        for (String arg : args) {
            Assert.assertTrue("Response body does not contain: " + arg, responseBody.contains(arg));
        }
    }

    /**
     * Asserts that the number of objects in the response matches the expected count.
     *
     * @param count the expected count of objects
     */
    @And("the number of objects in the response is {int}")
    public void theNumberOfObjectsInTheResponseIs(int count) {
        Assert.assertNotNull("Response is null", response);
        List<Object> objects = getResponseObjects();
        Assert.assertEquals("Number of objects does not match", count, objects.size());
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
        ApiEndpoint endpoint = ApiEndpoint.valueOf(enumName.toUpperCase());
        Assert.assertNotNull("Endpoint is null", endpoint);
        response = RestAssured.get(endpoint.getUrl());
        Assert.assertNotNull("Response is null", response);
    }

    @Given("I perform a POST request to {string} with query params")
    public void iPerformAPOSTRequestToWithQueryParams(String enumName) throws JsonProcessingException {
        ApiEndpoint endpoint = ApiEndpoint.valueOf(enumName.toUpperCase());
        Assert.assertNotNull("Endpoint is null", endpoint);

        Activity activity = new Activity();
        activity.setId(31);
        activity.setTitle("Activity 30");
        activity.setDueDate("2024-07-30T11:58:38.538Z");
        activity.setCompleted(false);

        String jsonBody = activity.serialize();

        response = RestAssured.given()
                .contentType("application/json")
                .body(jsonBody)
                .post(endpoint.getUrl());
    }

    @And("check response body information is correct")
    public void checkResponseBodyInformationIsCorrect() throws JsonProcessingException {
        String responseBody = response.getBody().asString();
        Activity responseActivity = Activity.deserialize(responseBody);

        Assert.assertEquals(31, responseActivity.getId());
        Assert.assertEquals("Activity 30", responseActivity.getTitle());
        Assert.assertEquals("2024-07-30T11:58:38.538Z", responseActivity.getDueDate());
        Assert.assertFalse(responseActivity.isCompleted());
    }
}