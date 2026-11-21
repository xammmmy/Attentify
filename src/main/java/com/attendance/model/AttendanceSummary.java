package com.attendance.model;

public record AttendanceSummary(int totalRecords, int presentCount, int lateCount, int absentCount) {
    public double attendanceRate() {
        if (totalRecords == 0) return 0.0;
        return ((double) (presentCount + lateCount) / totalRecords) * 100.0;
    }
}