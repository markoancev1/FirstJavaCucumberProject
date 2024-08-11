package org.example.utils.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.example.utils.RestApiUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RestApiUtilsTest {

    private static final String BASE_URL = "https://fakerestapi.azurewebsites.net/api/v1";

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
    }

    @ParameterizedTest
    @MethodSource("provideEndpointsAndExpectedUrls")
    void testGetUrl(RestApiUtils endpoint, String expectedUrl) {
        assertEquals(expectedUrl, endpoint.getUrl());
    }

    private static Stream<Arguments> provideEndpointsAndExpectedUrls() {
        return Stream.of(
                Arguments.of(RestApiUtils.ACTIVITIES, BASE_URL + "/Activities"),
                Arguments.of(RestApiUtils.ACTIVITIES_BY_ID, BASE_URL + "/Activities/{id}")
        );
    }

    @Test
    void testGetUrlWithParams() {
        String url = RestApiUtils.ACTIVITIES_BY_ID.getUrlWithParams("15");
        assertEquals(BASE_URL + "/Activities/15", url);
    }

    @Test
    void testGetUrlWithParamsThrowsExceptionWhenNotEnoughParams() {
        assertThrows(IllegalArgumentException.class, RestApiUtils.ACTIVITIES_BY_ID::getUrlWithParams);
    }

    @Test
    void testGetUrlWithParamsThrowsExceptionWhenTooManyParams() {
        assertThrows(IllegalArgumentException.class, () -> RestApiUtils.ACTIVITIES_BY_ID.getUrlWithParams("1", "2"));
    }

    @Test
    void testGetUrlWithQueryParams() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("param1", "value1");
        queryParams.put("param2", "value2");

        String url = RestApiUtils.ACTIVITIES.getUrlWithQueryParams(queryParams);
        assertTrue(url.startsWith(BASE_URL + "/Activities?"));
        assertTrue(url.contains("param1=value1"));
        assertTrue(url.contains("param2=value2"));
        assertTrue(url.contains("&"));
    }

    @Test
    void testGetUrlWithQueryParamsReturnsOriginalUrlWhenParamsAreEmpty() {
        String url = RestApiUtils.ACTIVITIES.getUrlWithQueryParams(new HashMap<>());
        assertEquals(BASE_URL + "/Activities", url);
    }

    @Test
    void testGetApiEndpoint() {
        assertEquals(RestApiUtils.ACTIVITIES, RestApiUtils.getApiEndpoint("ACTIVITIES"));
        assertEquals(RestApiUtils.ACTIVITIES_BY_ID, RestApiUtils.getApiEndpoint("ACTIVITIES_BY_ID"));
    }

    @Test
    void testGetApiEndpointThrowsExceptionForInvalidName() {
        assertThrows(IllegalArgumentException.class, () -> RestApiUtils.getApiEndpoint("INVALID_ENDPOINT"));
    }

    @Test
    void testPerformGetRequest() {
        try (MockedStatic<RestAssured> mockedRestAssured = mockStatic(RestAssured.class)) {
            Response mockResponse = mock(Response.class);
            mockedRestAssured.when(() -> RestAssured.get(anyString())).thenReturn(mockResponse);

            Response response = RestApiUtils.performGetRequest(RestApiUtils.ACTIVITIES);

            assertNotNull(response);
            mockedRestAssured.verify(() -> RestAssured.get(BASE_URL + "/Activities"));
        }
    }

    @Test
    void testUrlEncodingInGetUrlWithParams() {
        try (MockedStatic<URLEncoder> mockedURLEncoder = mockStatic(URLEncoder.class)) {
            mockedURLEncoder.when(() -> URLEncoder.encode(eq("test param"), eq(StandardCharsets.UTF_8)))
                    .thenReturn("test+param");

            String url = RestApiUtils.ACTIVITIES_BY_ID.getUrlWithParams("test param");

            assertEquals(BASE_URL + "/Activities/test+param", url);
            mockedURLEncoder.verify(() -> URLEncoder.encode("test param", StandardCharsets.UTF_8));
        }
    }
}