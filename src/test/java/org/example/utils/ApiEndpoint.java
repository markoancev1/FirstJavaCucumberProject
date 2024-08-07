package org.example.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enum representing API endpoints for the Fake REST API.
 * Each enum constant corresponds to a specific API endpoint, which can be retrieved as a URL string.
 * This enum also provides utility methods for formatting URLs with path and query parameters,
 * and for serializing the endpoint information to JSON.
 */
public enum ApiEndpoint {
    ACTIVITIES("https://fakerestapi.azurewebsites.net/api/v1/Activities"),
    ACTIVITIES_BY_ID("https://fakerestapi.azurewebsites.net/api/v1/Activities/{id}");

    private final String url;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Logger LOGGER = Logger.getLogger(ApiEndpoint.class.getName());

    /**
     * Constructs an ApiEndpoint with the specified URL.
     *
     * @param url the URL of the API endpoint.
     */
    ApiEndpoint(String url) {
        this.url = url;
    }

    /**
     * Retrieves the URL associated with this API endpoint.
     *
     * @return the URL as a string.
     */
    public String getUrl() {
        LOGGER.info("Retrieving URL: " + url);
        return url;
    }

    /**
     * Replaces placeholders in the URL with the specified parameters.
     * This method replaces placeholders in the URL in the form of `{placeholder}` with the
     * provided parameters in the order they are specified. It throws an exception if the number
     * of provided parameters does not match the number of placeholders.
     *
     * @param params the parameters to replace the placeholders.
     * @return the URL with placeholders replaced by the specified parameters.
     * @throws IllegalArgumentException if the number of parameters does not match the number of placeholders.
     */
    public String getUrlWithParams(String... params) {
        String resultUrl = url;
        LOGGER.info("Replacing placeholders in URL: " + url + " with parameters: " + String.join(", ", params));
        for (String param : params) {
            int placeholderIndex = resultUrl.indexOf("{");
            if (placeholderIndex == -1) {
                LOGGER.severe("Too many parameters provided for URL: " + url);
                throw new IllegalArgumentException("Too many parameters provided for URL: " + url);
            }
            resultUrl = resultUrl.replaceFirst("\\{[^}]+}", param);
        }
        if (resultUrl.contains("{")) {
            LOGGER.severe("Not enough parameters provided for URL: " + url);
            throw new IllegalArgumentException("Not enough parameters provided for URL: " + url);
        }
        LOGGER.info("Final URL after parameter replacement: " + resultUrl);
        return resultUrl;
    }

    /**
     * Appends query parameters to the URL.
     * This method constructs a URL by appending the specified query parameters as key-value pairs.
     * If no query parameters are provided, it returns the original URL.
     *
     * @param queryParams a map of query parameters, where keys are parameter names and values are parameter values.
     * @return the URL with query parameters appended as a query string.
     */
    public String getUrlWithQueryParams(Map<String, String> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            LOGGER.info("No query parameters provided. Returning original URL: " + url);
            return url;
        }

        StringBuilder urlWithParams = new StringBuilder(url).append("?");
        queryParams.forEach((key, value) -> urlWithParams.append(key).append("=").append(value).append("&"));
        urlWithParams.setLength(urlWithParams.length() - 1); // Remove trailing "&"

        String finalUrl = urlWithParams.toString();
        LOGGER.info("URL with query parameters: " + finalUrl);
        return finalUrl;
    }

    /**
     * Converts this API endpoint to a JSON string representation.
     * Uses Jackson's ObjectMapper to serialize the API endpoint details into a JSON string.
     *
     * @return a JSON representation of the API endpoint.
     * @throws JsonProcessingException if an error occurs during JSON processing.
     */
    public String toJson() throws JsonProcessingException {
        try {
            String json = OBJECT_MAPPER.writeValueAsString(this);
            LOGGER.info("Converted API endpoint to JSON: " + json);
            return json;
        } catch (JsonProcessingException e) {
            LOGGER.log(Level.SEVERE, "Error converting API endpoint to JSON", e);
            throw e;
        }
    }
}