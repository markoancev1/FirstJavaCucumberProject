package org.example.pojo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents an activity with an id, title, due date, and completion status.
 */
public class Activity {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activity.class);

    private int id;
    private String title;
    private String dueDate;
    private boolean completed;

    public Activity(int id, String title, String dueDate, boolean completed) {
        this.id = id;
        this.title = title;
        this.dueDate = dueDate;
        this.completed = completed;
        LOGGER.debug("Created new Activity object with id {}", id);
    }

    public Activity() {
        LOGGER.debug("Created new empty Activity object");
    }

    public int getId() {
        LOGGER.debug("Get id for Activity object with id {}", this.id);
        return id;
    }

    public void setId(int id) {
        this.id = id;
        LOGGER.debug("Set id to {} for Activity object with id {}", id, this.id);
    }

    public String getTitle() {
        LOGGER.debug("Get title for Activity object with id {}", this.id);
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        LOGGER.debug("Set title to {} for Activity object with id {}", title, this.id);
    }

    public String getDueDate() {
        LOGGER.debug("Get due date for Activity object with id {}", this.id);
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
        LOGGER.debug("Set due date to {} for Activity object with id {}", dueDate, this.id);
    }

    public boolean isCompleted() {
        LOGGER.debug("Get completed for Activity object with id {}", this.id);
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
        LOGGER.debug("Set completed to {} for Activity object with id {}", completed, this.id);
    }

    /**
     * Serializes this object to a JSON string.
     *
     * @return the JSON string representation of this object
     * @throws JsonProcessingException if the serialization fails
     */
    public String serialize() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(this);
        LOGGER.debug("Serialized Activity object with id {} to JSON string {}", this.id, jsonString);
        return jsonString;
    }

    /**
     * Deserializes a JSON string to an Activity object.
     *
     * @param jsonString the JSON string to deserialize
     * @return the Activity object
     * @throws JsonProcessingException if the deserialization fails
     */
    public static Activity deserialize(String jsonString) throws JsonProcessingException {
        LOGGER.debug("Deserializing JSON string {} to Activity object", jsonString);
        ObjectMapper objectMapper = new ObjectMapper();
        Activity activity = objectMapper.readValue(jsonString, Activity.class);
        LOGGER.debug("Deserialized JSON string {} to Activity object with id {}", jsonString, activity.getId());
        return activity;
    }
}