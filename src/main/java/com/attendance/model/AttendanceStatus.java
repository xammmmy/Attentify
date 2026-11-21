package com.attendance.model;

public enum AttendanceStatus {
    PRESENT("Present (On Time)"),
    LATE("Late Arrival"),
    ABSENT("Absent"),
    EXCUSED("Excused Absence");

    private final String description;

    AttendanceStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}