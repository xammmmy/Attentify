package com.attendance.model;

public class Student extends Person {
    private String program;
    private static int totalStudentsCreated = 0;

    public Student(String id, String fullName, String program) {
        super(id, fullName);
        this.program = program;
        totalStudentsCreated++;
    }

    public String getProgram() { return program; }
    public void setProgram(String program) { this.program = program; }

    public static int getTotalStudentsCreated() {
        return totalStudentsCreated;
    }

    @Override
    public String getRoleDetails() {
        return "Student [" + getId() + "] - Program: " + program;
    }
}