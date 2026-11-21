package com.attendance.model;

public class AttendanceRecord implements Reportable {
    private int recordId;
    private Student student; // "Has-a" relationship
    private AttendanceStatus status;
    private String remarks;

    public AttendanceRecord(int recordId, Student student, AttendanceStatus status, String remarks) {
        this.recordId = recordId;
        this.student = student;
        this.status = status;
        this.remarks = remarks;
    }

    public int getRecordId() { return recordId; }
    public Student getStudent() { return student; }
    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus status) { this.status = status; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getBadgeCode() {
        // Module 5: Enhanced switch expression
        return switch (status) {
            case PRESENT -> "[P]";
            case LATE    -> "[L]";
            case ABSENT  -> "[A]";
            case EXCUSED -> "[E]";
        };
    }

    @Override
    public String generateReportLine() {
        return String.format("%s %s (%s) - %s | Note: %s",
                getBadgeCode(),
                student.getFullName(),
                student.getId(),
                status.getDescription(),
                remarks == null || remarks.isBlank() ? "None" : remarks);
    }
}