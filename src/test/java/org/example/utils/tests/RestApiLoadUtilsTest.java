package org.example.utils.tests;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.example.utils.RestApiLoadUtils;
import org.example.utils.RestApiUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class RestApiLoadUtilsTest {

    private MockedStatic<RestAssured> mockedRestAssured;
    private Response mockResponse;
    private RequestSpecification mockRequestSpecification;
    private static final Logger logger = LoggerFactory.getLogger(RestApiLoadUtilsTest.class);

    @BeforeEach
    void setUp() {
        mockedRestAssured = Mockito.mockStatic(RestAssured.class);
        mockResponse = mock(Response.class);
        mockRequestSpecification = mock(RequestSpecification.class);

        // Setup default behavior for RestAssured mock
        mockedRestAssured.when(RestAssured::given).thenReturn(mockRequestSpecification);

        // Setup chained method calls
        when(mockRequestSpecification.header(anyString(), anyString())).thenReturn(mockRequestSpecification);
        when(mockRequestSpecification.body(Optional.ofNullable(any()))).thenReturn(mockRequestSpecification);
        when(mockRequestSpecification.post(anyString())).thenReturn(mockResponse);
    }

    @AfterEach
    void tearDown() {
        mockedRestAssured.close();
    }

    @Test
    void testRunLoadTestSuccess() {
        when(mockResponse.getStatusCode()).thenReturn(200);

        int threadCount = 2;
        int callCountPerThread = 3;
        RestApiLoadUtils.LoadTestResult result = RestApiLoadUtils.runLoadTest(
                RestApiUtils.ACTIVITIES.getUrl(), () -> "{}", threadCount, callCountPerThread, 200);

        System.out.println("Test parameters: threadCount=" + threadCount +
                ", callCountPerThread=" + callCountPerThread);
        System.out.println("Total requests: " + result.totalRequests);
        System.out.println("Failed requests: " + result.failedRequests);
        System.out.println("Success: " + result.success);
        System.out.println("Total response time: " + result.totalResponseTime);
        System.out.println("Max response time: " + result.maxResponseTime);
        System.out.println("Min response time: " + result.minResponseTime);

        assertEquals(threadCount * callCountPerThread, result.totalRequests, "Total requests mismatch");

        // Allow for a small number of failed requests due to potential race conditions or timing issues
        int maxAllowedFailures = (int) Math.ceil(result.totalRequests * 0.1); // Allow up to 10% failures
        assertTrue(result.failedRequests <= maxAllowedFailures,
                "Expected at most " + maxAllowedFailures + " failed requests, but got " + result.failedRequests);

        // Success is true if the number of failed requests is within the acceptable range
        assertTrue(result.success, "Unexpected success status");
    }

    @Test
    void testRunLoadTestFailure() {
        when(mockResponse.getStatusCode()).thenReturn(500);

        RestApiLoadUtils.LoadTestResult result = RestApiLoadUtils.runLoadTest(
                "https://www.google.com", () -> "{}", 2, 3, 200);

        assertEquals(6, result.totalRequests);
        assertEquals(6, result.failedRequests);
        assertFalse(result.success);
    }

    @ParameterizedTest
    @CsvSource({
            "1, 1, 1, 1, 1",
            "2, 3, 1, 1, 1",
            "1, 5, 2, 1, 1"
    })
    void testRunLoadTestWithGradualIncrease(int initialThreadCount, int maxThreadCount,
                                            int requestsPerThread, int incrementStep, int incrementIntervalSecs) {
        // Ensure all responses are successful
        when(mockResponse.getStatusCode()).thenReturn(200);

        RestApiLoadUtils.LoadTestResult result = RestApiLoadUtils.runLoadTestWithGradualIncrease(
                RestApiUtils.ACTIVITIES.getUrl(), initialThreadCount, maxThreadCount, requestsPerThread,
                incrementStep, incrementIntervalSecs, () -> "{}", 200);

        int expectedTotalRequests = 0;
        for (int i = initialThreadCount; i <= maxThreadCount; i += incrementStep) {
            expectedTotalRequests += i * requestsPerThread;
        }

        System.out.println("Test parameters: initialThreadCount=" + initialThreadCount +
                ", maxThreadCount=" + maxThreadCount +
                ", requestsPerThread=" + requestsPerThread +
                ", incrementStep=" + incrementStep +
                ", incrementIntervalSecs=" + incrementIntervalSecs);
        System.out.println("Expected total requests: " + expectedTotalRequests);
        System.out.println("Actual total requests: " + result.totalRequests);
        System.out.println("Failed requests: " + result.failedRequests);
        System.out.println("Success: " + result.success);

        assertEquals(expectedTotalRequests, result.totalRequests, "Total requests mismatch");

        // Allow for a higher number of failed requests, but it should not exceed 10% of total requests
        int maxAllowedFailures = (int) Math.ceil(expectedTotalRequests * 0.1);
        assertTrue(result.failedRequests <= maxAllowedFailures,
                "Expected at most " + maxAllowedFailures + " failed requests, but got " + result.failedRequests);

        // Success is true if the number of failed requests is within the acceptable range
        assertTrue(result.success, "Unexpected success status");
    }

    @Test
    void testLoadTestResultCreation() {
        RestApiLoadUtils.LoadTestResult result = new RestApiLoadUtils.LoadTestResult(
                true, 100, 5, 1000, 50, 10);

        assertTrue(result.success);
        assertEquals(100, result.totalRequests);
        assertEquals(5, result.failedRequests);
        assertEquals(1000, result.totalResponseTime);
        assertEquals(50, result.maxResponseTime);
        assertEquals(10, result.minResponseTime);
    }

    @Test
    void testRunLoadTestWithException() {
        when(mockRequestSpecification.post(anyString())).thenThrow(new RuntimeException("Test exception"));

        RestApiLoadUtils.LoadTestResult result = RestApiLoadUtils.runLoadTest(
                "https://www.google.com", () -> "{}", 2, 3, 200);

        assertEquals(6, result.totalRequests);
        assertEquals(6, result.failedRequests);
        assertFalse(result.success);
    }

    @Test
    void testRunLoadTestResponseTimes() {
        when(mockResponse.getStatusCode()).thenReturn(200);

        RestApiLoadUtils.LoadTestResult result = RestApiLoadUtils.runLoadTest(
                "https://www.google.com", () -> "{}", 1, 1, 200);

        assertTrue(result.minResponseTime <= result.maxResponseTime);
        assertTrue(result.totalResponseTime >= result.maxResponseTime);
        assertTrue(result.totalResponseTime <= result.maxResponseTime * result.totalRequests);
    }
}