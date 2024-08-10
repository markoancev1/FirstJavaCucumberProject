package org.example.steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.example.pojo.Activity;
import org.example.utils.AssertionUtils;
import org.example.utils.RestApiLoadUtils;
import org.example.utils.RestApiUtils;
import org.example.data.TestDataFactory;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Supplier;

public class ApiSteps {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiSteps.class);
    private Response response;

    @When("the status code is {int}")
    public void verifyStatusCode(int expectedStatusCode) {
        LOGGER.info("Verifying that the status code is {}", expectedStatusCode);
        AssertionUtils.assertStatusCode(getResponse(), expectedStatusCode);
    }

    @Then("the response body contains {string}, {string}, {string}, {string}")
    public void verifyResponseBodyContains(String id, String title, String dueDate, String completed) {
        LOGGER.info("Checking if response body contains: {}, {}, {}, {}", id, title, dueDate, completed);
        String responseBody = getResponse().getBody().asString();
        LOGGER.debug("Response body: {}", responseBody);
        AssertionUtils.assertResponseBodyContains(responseBody, id, title, dueDate, completed);
    }

    @And("the number of objects in the response is {int}")
    public void verifyNumberOfObjects(int expectedCount) {
        LOGGER.info("Verifying that the number of objects in the response is {}", expectedCount);
        List<Object> objects = getResponse().jsonPath().getList("$");
        Assertions.assertEquals(expectedCount, objects.size(), "Number of objects does not match");
    }

    @Given("I perform a GET request to {string} with query params")
    public void performGetRequest(String endpointName) {
        LOGGER.info("Performing GET request to endpoint: {}", endpointName);
        RestApiUtils endpoint = RestApiUtils.getApiEndpoint(endpointName);
        response = RestApiUtils.performGetRequest(endpoint);
        verifyStatusCode(200);
    }

    @Given("I perform a POST request to {string} with query params")
    public void performPostRequest(String endpointName) {
        LOGGER.info("Performing POST request to endpoint: {}", endpointName);
        RestApiUtils endpoint = RestApiUtils.getApiEndpoint(endpointName);
        Activity activity = TestDataFactory.createSampleActivity();
        String jsonBody = activity.serialize();
        LOGGER.debug("POST request body: {}", jsonBody);

        response = RestAssured.given()
                .contentType("application/json")
                .body(jsonBody)
                .post(endpoint.getUrl());
        LOGGER.info("Received response with status code: {}", response.getStatusCode());
    }

    @Then("check response body information is correct")
    public void verifyResponseBodyInformation() {
        LOGGER.info("Checking if the response body information is correct");
        String responseBody = getResponse().getBody().asString();
        Activity responseActivity = Activity.deserialize(responseBody);

        Activity expectedActivity = TestDataFactory.createSampleActivity();
        Assertions.assertEquals(expectedActivity.getId(), responseActivity.getId(), "Activity ID does not match");
        Assertions.assertEquals(expectedActivity.getTitle(), responseActivity.getTitle(), "Activity title does not match");
        Assertions.assertEquals(expectedActivity.getDueDate(), responseActivity.getDueDate(), "Activity due date does not match");
        Assertions.assertEquals(expectedActivity.isCompleted(), responseActivity.isCompleted(), "Activity completion status does not match");
        LOGGER.info("Response body information is correct");
    }

    @Given("I perform a GET request to {string} with {int}")
    public void performGetRequestWithId(String endpointName, int id) {
        RestApiUtils endpoint = RestApiUtils.getApiEndpoint(endpointName);
        String urlWithId = endpoint.getUrlWithParams(String.valueOf(id));
        response = RestAssured.get(urlWithId);
    }

    private Response getResponse() {
        AssertionUtils.assertResponseNotNull(response);
        return response;
    }

    @Given("I perform a gradual load test on {string} starting with {int} users up to {int} users incrementing by {int} every {int} seconds with {int} requests with status {int}")
    public void performGradualLoadTest(String url, int initialUsers, int maxUsers, int incrementStep, int incrementIntervalSecs, int requestsPerUser, int statusCode) {
        RestApiUtils endpoint = RestApiUtils.getApiEndpoint(url);
        Supplier<String> requestBodySupplier = () -> TestDataFactory.createSampleActivity().serialize();

        RestApiLoadUtils.LoadTestResult result = RestApiLoadUtils.runLoadTestWithGradualIncrease(
                endpoint.getUrl(),
                initialUsers,
                maxUsers,
                requestsPerUser,
                incrementStep,
                incrementIntervalSecs,
                requestBodySupplier,
                statusCode
        );

        LOGGER.info("Load test completed. Total requests: {}, Failed requests: {}, Success rate: {}%",
                result.totalRequests,
                result.failedRequests,
                (result.totalRequests - result.failedRequests) * 100.0 / result.totalRequests);
        LOGGER.info("Response times - Average: {} ms, Min: {} ms, Max: {} ms",
                result.totalResponseTime / result.totalRequests,
                result.minResponseTime,
                result.maxResponseTime);

        Assertions.assertTrue(result.success, "Load test failed");
    }
}