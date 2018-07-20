package com.atwork.centralrn.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Schedule implements Serializable {

    @SerializedName("scheduleTime")
    private String scheduleTime;

    @SerializedName("identifier")
    private String identifier;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getScheduleTime() {
        return scheduleTime;
    }

    public void setScheduleTime(String scheduleTime) {
        this.scheduleTime = scheduleTime;
    }
}
