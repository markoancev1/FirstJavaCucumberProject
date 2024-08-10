package org.example.data;

import org.example.pojo.Activity;

public class TestDataFactory {

    public static Activity createSampleActivity() {
        Activity activity = new Activity();
        activity.setId(31);
        activity.setTitle("Activity 30");
        activity.setDueDate("2024-07-30T11:58:38.538Z");
        activity.setCompleted(false);
        return activity;
    }
}