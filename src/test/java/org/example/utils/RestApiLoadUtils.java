package org.example.utils;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class RestApiLoadUtils {
    private static final Logger logger = LoggerFactory.getLogger(RestApiLoadUtils.class);
    private static final int AWAIT_TERMINATION_TIMEOUT = 60; // seconds

    @SuppressWarnings("ClassCanBeRecord")
    public static class LoadTestResult {
        public final boolean success;
        public final int totalRequests;
        public final int failedRequests;
        public final long totalResponseTime;
        public final long maxResponseTime;
        public final long minResponseTime;

        public LoadTestResult(boolean success, int totalRequests, int failedRequests, long totalResponseTime, long maxResponseTime, long minResponseTime) {
            this.success = success;
            this.totalRequests = totalRequests;
            this.failedRequests = failedRequests;
            this.totalResponseTime = totalResponseTime;
            this.maxResponseTime = maxResponseTime;
            this.minResponseTime = minResponseTime;
        }
    }

    public static LoadTestResult runLoadTestWithGradualIncrease(String url, int initialThreadCount,
                                                                int maxThreadCount, int requestsPerThread,
                                                                int incrementStep, int incrementIntervalSecs,
                                                                Supplier<String> requestBodySupplier,
                                                                int expectedStatusCode) {
        int totalRequests = 0;
        int totalFailedRequests = 0;
        long totalResponseTime = 0;
        long maxResponseTime = Long.MIN_VALUE;
        long minResponseTime = Long.MAX_VALUE;

        for (int threadCount = initialThreadCount; threadCount <= maxThreadCount; threadCount += incrementStep) {
            logger.info("Starting new increment with {} threads each making {} requests.", threadCount, requestsPerThread);
            LoadTestResult incrementResult = runLoadTest(url, requestBodySupplier, threadCount, requestsPerThread, expectedStatusCode);

            totalRequests += incrementResult.totalRequests;
            totalFailedRequests += incrementResult.failedRequests;
            totalResponseTime += incrementResult.totalResponseTime;
            maxResponseTime = Math.max(maxResponseTime, incrementResult.maxResponseTime);
            minResponseTime = Math.min(minResponseTime, incrementResult.minResponseTime);

            logger.info("Completed increment with {} threads. Failed requests: {}", threadCount, incrementResult.failedRequests);

            try {
                Thread.sleep(incrementIntervalSecs * 1000L);
            } catch (InterruptedException e) {
                logger.error("Load test interrupted during wait between increments.", e);
                Thread.currentThread().interrupt();
                break;
            }
        }

        boolean overallSuccess = totalFailedRequests == 0;
        return new LoadTestResult(overallSuccess, totalRequests, totalFailedRequests, totalResponseTime, maxResponseTime, minResponseTime);
    }

    public static LoadTestResult runLoadTest(String url, Supplier<String> requestBodySupplier, int threadCount, int callCountPerThread, int expectedStatusCode) {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        AtomicInteger failureCount = new AtomicInteger(0);
        AtomicInteger totalRequests = new AtomicInteger(0);
        AtomicLong totalResponseTime = new AtomicLong(0);
        AtomicLong maxResponseTime = new AtomicLong(0);
        AtomicLong minResponseTime = new AtomicLong(Long.MAX_VALUE);

        CountDownLatch latch = new CountDownLatch(threadCount * callCountPerThread);

        try {
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    for (int j = 0; j < callCountPerThread; j++) {
                        long startTime = System.nanoTime();
                        try {
                            String requestBody = requestBodySupplier.get();
                            Response response = RestAssured.given()
                                    .header("Content-Type", "application/json")
                                    .body(requestBody)
                                    .post(url);
                            long responseTime = System.nanoTime() - startTime;
                            totalResponseTime.addAndGet(responseTime);
                            maxResponseTime.updateAndGet(current -> Math.max(current, responseTime));
                            minResponseTime.updateAndGet(current -> Math.min(current, responseTime));

                            if (response.getStatusCode() != expectedStatusCode) {
                                logger.error("Request failed with status code: {}, Expected: {}, Response Time: {} ms",
                                        response.getStatusCode(), expectedStatusCode, responseTime / 1_000_000);
                                failureCount.incrementAndGet();
                            } else {
                                logger.debug("Request succeeded with status code: {}, Response Time: {} ms",
                                        response.getStatusCode(), responseTime / 1_000_000);
                            }
                        } catch (Exception e) {
                            logger.error("Request failed due to an error: {}", e.getMessage(), e);
                            failureCount.incrementAndGet();
                        } finally {
                            totalRequests.incrementAndGet();
                            latch.countDown();
                        }
                    }
                });
            }

            boolean completed = latch.await(AWAIT_TERMINATION_TIMEOUT, TimeUnit.SECONDS);
            if (completed) {
                logger.info("All load test tasks completed within the timeout period.");
            } else {
                logger.warn("Load test tasks did not all complete within the timeout period. Some tasks may have been interrupted.");
            }
        } catch (InterruptedException e) {
            logger.error("Load test interrupted while waiting for completion.", e);
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdownNow();
        }

        return new LoadTestResult(
                failureCount.get() == 0,
                totalRequests.get(),
                failureCount.get(),
                totalResponseTime.get() / 1_000_000, // Convert to milliseconds
                maxResponseTime.get() / 1_000_000, // Convert to milliseconds
                minResponseTime.get() / 1_000_000 // Convert to milliseconds
        );
    }
}