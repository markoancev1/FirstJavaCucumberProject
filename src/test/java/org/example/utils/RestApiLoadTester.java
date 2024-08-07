package org.example.utils;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class is responsible for performing load tests on a REST API.
 */
public class RestApiLoadTester {
    private static final Logger logger = LoggerFactory.getLogger(RestApiLoadTester.class);
    private static final int AWAIT_TERMINATION_TIMEOUT = 60; // seconds

    /**
     * Runs a load test with a gradual increase in the number of threads.
     *
     * @param url                  The URL of the API to test.
     * @param initialThreadCount   The initial number of threads to start with.
     * @param maxThreadCount       The maximum number of threads to use.
     * @param requestsPerThread    The number of requests each thread will make.
     * @param incrementStep        The number of threads to increase by in each increment.
     * @param incrementIntervalSecs The time to wait between increments in seconds.
     * @return true if all increments are successful, false otherwise.
     */
    public static boolean runLoadTestWithGradualIncrease(String url, int initialThreadCount,
                                                         int maxThreadCount, int requestsPerThread,
                                                         int incrementStep, int incrementIntervalSecs) {
        boolean overallTestSuccessful = true;
        for (int threadCount = initialThreadCount; threadCount <= maxThreadCount; threadCount += incrementStep) {
            logger.info("Starting new increment with {} threads each making {} requests.", threadCount, requestsPerThread);
            boolean incrementSuccess = runLoadTest(url, getSampleRequestBody(), threadCount, requestsPerThread);
            if (!incrementSuccess) {
                overallTestSuccessful = false;
            }
            logger.info("Completed increment with {} threads.", threadCount);
            try {
                Thread.sleep(incrementIntervalSecs * 1000L);
            } catch (InterruptedException e) {
                logger.error("Load test interrupted during wait between increments.", e);
                Thread.currentThread().interrupt();
            }
        }
        return overallTestSuccessful;
    }

    /**
     * Runs a load test with the specified number of threads and requests.
     *
     * @param url                The URL of the API to test.
     * @param requestBody        The request body to send with each request.
     * @param threadCount        The number of threads to use.
     * @param callCountPerThread The number of requests each thread will make.
     * @return true if all requests were successful, false otherwise.
     */
    public static boolean runLoadTest(String url, String requestBody, int threadCount, int callCountPerThread) {
        ExecutorService executor = Executors.newCachedThreadPool(); // Use cached thread pool
        AtomicInteger failureCount = new AtomicInteger(0);

        try {
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    for (int j = 0; j < callCountPerThread; j++) {
                        long startTime = System.currentTimeMillis();
                        try {
                            Response response = RestAssured.given()
                                    .header("Content-Type", "application/json")
                                    .body(requestBody)
                                    .post(url);
                            long responseTime = System.currentTimeMillis() - startTime;
                            if (response.getStatusCode() != 200) {
                                logger.error("Request {} failed with status code: {}, Response Time: {} ms",
                                        j, response.getStatusCode(), responseTime);
                                failureCount.incrementAndGet();
                            } else {
                                logger.debug("Request {} succeeded with status code: {}, Response Time: {} ms",
                                        j, response.getStatusCode(), responseTime);
                            }
                        } catch (Exception e) {
                            logger.error("Request {} failed due to an error: {}", j, e.getMessage(), e);
                            failureCount.incrementAndGet();
                        }
                    }
                });
            }
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(AWAIT_TERMINATION_TIMEOUT, TimeUnit.SECONDS)) {
                    logger.warn("Executor did not terminate in the expected time; shutting down now");
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                logger.error("Main thread interrupted while waiting for executor to finish.", e);
                executor.shutdownNow();
            }
        }

        return failureCount.get() == 0; // Return true only if there were no failures
    }

    /**
     * Provides a sample request body for testing purposes.
     *
     * @return A sample JSON request body.
     */
    public static String getSampleRequestBody() {
        return "{\"name\": \"John\", \"age\": 30}";
    }
}