package org.example.steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.example.utils.ApiEndpoint;
import org.junit.Assert;
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
}