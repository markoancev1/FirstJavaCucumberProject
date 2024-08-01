package org.example.pojo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Date;

/**
 * Represents an activity with an id, title, due date, and completion status.
 */
public class Activity {
    private int id;
    private String title;
    private String dueDate;
    private boolean completed;

    /**
     * Default constructor.
     */
    public Activity() {
    }

    /**
     * Parameterized constructor to initialize all fields.
     *
     * @param id the unique identifier for the activity
     * @param title the title of the activity
     * @param dueDate the due date of the activity
     * @param completed the completion status of the activity
     */
    public Activity(int id, String title, String dueDate, boolean completed) {
        this.id = id;
        this.title = title;
        this.dueDate = dueDate;
        this.completed = completed;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    /**
     * Serialize this object to a JSON string.
     *
     * @return the JSON string representation of this object
     * @throws JsonProcessingException if the serialization fails
     */
    public String serialize() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(this);
    }

    /**
     * Deserialize a JSON string to an Activity object.
     *
     * @param jsonString the JSON string to deserialize
     * @return the Activity object
     * @throws JsonProcessingException if the deserialization fails
     */
    public static Activity deserialize(String jsonString) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(jsonString, Activity.class);
    }

}