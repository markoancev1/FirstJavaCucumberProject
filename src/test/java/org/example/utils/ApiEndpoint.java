package org.example.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * Enum representing API endpoints.
 */
public enum ApiEndpoint {
    ACTIVITIES("https://fakerestapi.azurewebsites.net/api/v1/Activities"),
    ACTIVITIES_BY_ID("https://fakerestapi.azurewebsites.net/api/v1/Activities/{id}");

    private final String url;

    ApiEndpoint(String url) {
        this.url = url;
    }

    /**
     * Gets the URL for the endpoint.
     *
     * @return the URL as a string.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Replaces the placeholders in the URL with the provided parameters.
     *
     * @param params the parameters to replace placeholders.
     * @return the URL with the parameters replaced.
     * @throws IllegalArgumentException if the number of parameters does not match the placeholders.
     */
    public String getUrlWithParams(String... params) {
        String resultUrl = url;
        for (String param : params) {
            if (!resultUrl.contains("{")) {
                throw new IllegalArgumentException("Too many parameters provided for URL: " + url);
            }
            resultUrl = resultUrl.replaceFirst("\\{[^}]+}", param);
        }
        if (resultUrl.contains("{")) {
            throw new IllegalArgumentException("Not enough parameters provided for URL: " + url);
        }
        return resultUrl;
    }

    /**
     * Adds query parameters to the URL.
     *
     * @param queryParams the query parameters to add.
     * @return the URL with query parameters.
     */
    public String getUrlWithQueryParams(Map<String, String> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return url;
        }

        StringBuilder urlWithParams = new StringBuilder(url);
        urlWithParams.append("?");
        queryParams.forEach((key, value) -> urlWithParams.append(key).append("=").append(value).append("&"));
        urlWithParams.deleteCharAt(urlWithParams.length() - 1);

        return urlWithParams.toString();
    }

    /**
     * Converts the Activity object to a JSON string.
     *
     * @return JSON representation of the Activity object.
     * @throws JsonProcessingException if the object cannot be converted to JSON.
     */
    public String toJson() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(this);
    }
}