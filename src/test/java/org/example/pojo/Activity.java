package org.example.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Represents an activity with an id, title, due date, and completion status.
 */
public class Activity {
    private static final Logger LOGGER = LoggerFactory.getLogger(Activity.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private int id;
    private String title;
    private String dueDate;
    private boolean completed;

    public Activity(int id, String title, String dueDate, boolean completed) {
        this.id = id;
        this.title = title;
        this.dueDate = dueDate;
        this.completed = completed;
    }

    // Default constructor
    public Activity() {
    }

    // Getter and setter methods
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

    @JsonIgnore
    public String serialize() {
        try {
            return OBJECT_MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to serialize Activity: {}", this, e);
            throw new RuntimeException("Serialization failed", e);
        }
    }

    public static Activity deserialize(String jsonString) {
        try {
            return OBJECT_MAPPER.readValue(jsonString, Activity.class);
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to deserialize Activity from JSON: {}", jsonString, e);
            throw new RuntimeException("Deserialization failed", e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Activity activity = (Activity) o;
        return id == activity.id &&
                completed == activity.completed &&
                Objects.equals(title, activity.title) &&
                Objects.equals(dueDate, activity.dueDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, dueDate, completed);
    }

    @Override
    public String toString() {
        return "Activity{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", dueDate='" + dueDate + '\'' +
                ", completed=" + completed +
                '}';
    }
}