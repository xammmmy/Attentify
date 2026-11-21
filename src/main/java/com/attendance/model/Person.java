package com.attendance.model;

public abstract class Person {
    private String id;
    private String fullName;

    public Person(String id, String fullName) {
        this.id = id;
        this.fullName = fullName;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    // Abstract method implemented polymorphically by subclasses
    public abstract String getRoleDetails();
}