package org.example.utils;

import org.junit.jupiter.api.Assertions;
import io.restassured.response.Response;

public class AssertionUtils {

    public static void assertResponseNotNull(Response response) {
        Assertions.assertNotNull(response, "Response is null");
    }

    public static void assertResponseBodyContains(String responseBody, String... expectedValues) {
        for (String value : expectedValues) {
            Assertions.assertTrue(responseBody.contains(value), "Response body does not contain: " + value);
        }
    }

    public static void assertStatusCode(Response response, int expectedStatusCode) {
        Assertions.assertEquals(expectedStatusCode, response.getStatusCode(),
                "Status code does not match. Expected: " + expectedStatusCode + ", Actual: " + response.getStatusCode());
    }
}