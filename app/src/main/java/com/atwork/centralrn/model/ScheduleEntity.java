package com.atwork.centralrn.model;

import java.util.List;

public class ScheduleEntity {
    private List<Schedule> schedules;

    public ScheduleEntity() {}

    public void setSchedules(List<Schedule> schedules) { this.schedules = schedules; }
    public List<Schedule> getSchedules() { return this.schedules; }
}