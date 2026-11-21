package com.attendance.dao;

import com.attendance.model.AttendanceRecord;
import com.attendance.model.AttendanceStatus;
import com.attendance.model.Student;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AttendanceDAO implements Repository<AttendanceRecord> {

    @Override
    public void save(AttendanceRecord record) throws SQLException {
        String sql = "INSERT INTO attendance_records (student_id, full_name, course_program, status, remarks) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, record.getStudent().getId());
            ps.setString(2, record.getStudent().getFullName());
            ps.setString(3, record.getStudent().getProgram());
            ps.setString(4, record.getStatus().name());
            ps.setString(5, record.getRemarks());
            ps.executeUpdate();
        }
    }

    @Override
    public List<AttendanceRecord> findAll() throws SQLException {
        List<AttendanceRecord> list = new ArrayList<>();
        String sql = "SELECT record_id, student_id, full_name, course_program, status, remarks FROM attendance_records";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int recId = rs.getInt("record_id");
                Student student = new Student(
                        rs.getString("student_id"),
                        rs.getString("full_name"),
                        rs.getString("course_program")
                );
                AttendanceStatus status = AttendanceStatus.valueOf(rs.getString("status"));
                String remarks = rs.getString("remarks");

                list.add(new AttendanceRecord(recId, student, status, remarks));
            }
        }
        return list;
    }

    @Override
    public void update(AttendanceRecord record) throws SQLException {
        String sql = "UPDATE attendance_records SET full_name = ?, course_program = ?, status = ?, remarks = ? WHERE student_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, record.getStudent().getFullName());
            ps.setString(2, record.getStudent().getProgram());
            ps.setString(3, record.getStatus().name());
            ps.setString(4, record.getRemarks());
            ps.setString(5, record.getStudent().getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void deleteByKey(String studentId) throws SQLException {
        String sql = "DELETE FROM attendance_records WHERE student_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.executeUpdate();
        }
    }
}