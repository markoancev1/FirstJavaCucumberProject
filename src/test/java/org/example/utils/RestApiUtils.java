package org.example.utils;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum RestApiUtils {
    ACTIVITIES("/Activities"),
    ACTIVITIES_BY_ID("/Activities/{id}");

    private static final String BASE_URL = "https://fakerestapi.azurewebsites.net/api/v1";
    private static final Logger LOGGER = LoggerFactory.getLogger(RestApiUtils.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final ConcurrentHashMap<RestApiUtils, String> URL_CACHE = new ConcurrentHashMap<>();

    private final String path;

    RestApiUtils(String path) {
        this.path = path;
    }

    public String getUrl() {
        return URL_CACHE.computeIfAbsent(this, key -> BASE_URL + key.path);
    }

    public String getUrlWithParams(String... params) {
        StringBuilder resultUrl = new StringBuilder(getUrl());
        int paramIndex = 0;

        while (resultUrl.indexOf("{") != -1) {
            if (paramIndex >= params.length) {
                throw new IllegalArgumentException("Not enough parameters provided for URL: " + resultUrl);
            }
            int closingBraceIndex = resultUrl.indexOf("}", resultUrl.indexOf("{"));
            resultUrl.replace(resultUrl.indexOf("{"), closingBraceIndex + 1,
                    URLEncoder.encode(params[paramIndex++], StandardCharsets.UTF_8));
        }

        if (paramIndex < params.length) {
            throw new IllegalArgumentException("Too many parameters provided for URL: " + resultUrl);
        }

        return resultUrl.toString();
    }

    public String getUrlWithQueryParams(Map<String, String> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return getUrl();
        }

        StringBuilder urlWithParams = new StringBuilder(getUrl()).append('?');
        queryParams.forEach((key, value) ->
                urlWithParams.append(URLEncoder.encode(key, StandardCharsets.UTF_8))
                        .append('=')
                        .append(URLEncoder.encode(value, StandardCharsets.UTF_8))
                        .append('&')
        );
        urlWithParams.setLength(urlWithParams.length() - 1); // Remove trailing "&"

        return urlWithParams.toString();
    }

    public String toJson() {
        try {
            return OBJECT_MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            LOGGER.error("Error converting API endpoint to JSON", e);
            throw new RuntimeException("Failed to convert API endpoint to JSON", e);
        }
    }

    public static RestApiUtils getApiEndpoint(String enumName) {
        try {
            return RestApiUtils.valueOf(enumName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid endpoint name: " + enumName, e);
        }
    }

    public static Response performGetRequest(RestApiUtils endpoint) {
        LOGGER.info("Performing GET request to: {}", endpoint.getUrl());
        return RestAssured.get(endpoint.getUrl());
    }
}