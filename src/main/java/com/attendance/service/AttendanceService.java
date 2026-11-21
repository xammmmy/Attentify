package com.attendance.service;

import com.attendance.dao.AttendanceDAO;
import com.attendance.exception.InvalidAttendanceException;
import com.attendance.model.*;
import java.sql.SQLException;
import java.util.*;

public class AttendanceService {
    private final AttendanceDAO dao;
    // Module 6: Collections Framework (List, Map, Set)
    private final List<AttendanceRecord> cachedRecords = new ArrayList<>();
    private final Map<String, AttendanceRecord> recordLookupMap = new HashMap<>();
    private final Set<String> registeredIds = new HashSet<>();

    public AttendanceService() {
        this.dao = new AttendanceDAO();
    }

    // Module 7: Synchronized access to shared collection state
    public synchronized List<AttendanceRecord> loadAndSyncRecords() throws SQLException {
        List<AttendanceRecord> freshData = dao.findAll();
        cachedRecords.clear();
        recordLookupMap.clear();
        registeredIds.clear();

        for (AttendanceRecord rec : freshData) {
            cachedRecords.add(rec);
            String id = rec.getStudent().getId();
            recordLookupMap.put(id, rec);
            registeredIds.add(id);
        }
        return new ArrayList<>(cachedRecords);
    }

    public synchronized void addAttendance(String id, String name, String program,
                                           AttendanceStatus status, String remarks)
            throws InvalidAttendanceException, SQLException {
        validateInput(id, name, program);

        if (registeredIds.contains(id.trim())) {
            throw new InvalidAttendanceException("Student ID '" + id + "' already has an attendance record.");
        }

        Student student = new Student(id.trim(), name.trim(), program.trim());
        AttendanceRecord record = new AttendanceRecord(0, student, status, remarks.trim());
        dao.save(record);
        loadAndSyncRecords();
    }

    public synchronized void updateAttendance(String id, String name, String program,
                                              AttendanceStatus status, String remarks)
            throws InvalidAttendanceException, SQLException {
        validateInput(id, name, program);

        if (!registeredIds.contains(id.trim())) {
            throw new InvalidAttendanceException("Cannot update: Student ID '" + id + "' does not exist.");
        }

        Student student = new Student(id.trim(), name.trim(), program.trim());
        AttendanceRecord record = new AttendanceRecord(0, student, status, remarks.trim());
        dao.update(record);
        loadAndSyncRecords();
    }

    public synchronized void deleteAttendance(String id) throws InvalidAttendanceException, SQLException {
        if (id == null || id.isBlank()) {
            throw new InvalidAttendanceException("Please select or enter a valid Student ID to delete.");
        }
        dao.deleteByKey(id.trim());
        loadAndSyncRecords();
    }

    // Module 6: HashMap fast key-based lookup
    public synchronized AttendanceRecord findByStudentId(String id) {
        return recordLookupMap.get(id.trim());
    }

    // Module 6: Comparator sorting
    public synchronized List<AttendanceRecord> getRecordsSortedByName() {
        List<AttendanceRecord> sorted = new ArrayList<>(cachedRecords);
        sorted.sort(Comparator.comparing(r -> r.getStudent().getFullName().toLowerCase()));
        return sorted;
    }

    // Module 5 & 6: Iterator traversal & Record return
    public synchronized AttendanceSummary computeSummary() {
        int total = 0, present = 0, late = 0, absent = 0;
        Iterator<AttendanceRecord> it = cachedRecords.iterator();
        while (it.hasNext()) {
            AttendanceRecord rec = it.next();
            total++;
            if (rec.getStatus() == AttendanceStatus.PRESENT) present++;
            else if (rec.getStatus() == AttendanceStatus.LATE) late++;
            else if (rec.getStatus() == AttendanceStatus.ABSENT) absent++;
        }
        return new AttendanceSummary(total, present, late, absent);
    }

    private void validateInput(String id, String name, String program) throws InvalidAttendanceException {
        if (id == null || id.isBlank()) {
            throw new InvalidAttendanceException("Student ID is required.");
        }
        if (name == null || name.isBlank()) {
            throw new InvalidAttendanceException("Student Full Name is required.");
        }
        if (program == null || program.isBlank()) {
            throw new InvalidAttendanceException("Course/Program is required.");
        }
    }
}