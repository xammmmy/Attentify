package com.attendance.ui;

import com.attendance.exception.InvalidAttendanceException;
import com.attendance.model.*;
import com.attendance.service.AttendanceService;
import com.attendance.service.SyncWorker;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.List;

public class AttendanceApp extends JFrame {
    private final AttendanceService service;
    private SyncWorker syncWorker;

    // Swing Input Controls
    private JTextField txtStudentId;
    private JTextField txtFullName;
    private JTextField txtProgram;
    private JComboBox<AttendanceStatus> cmbStatus;
    private JTextField txtRemarks;
    private JTextField txtSearchId;

    // Swing Display Controls
    private JTable attendanceTable;
    private DefaultTableModel tableModel;
    private JLabel lblSummary;
    private JLabel lblSyncStatus;

    public AttendanceApp() {
        this.service = new AttendanceService();
        initComponents();
        refreshTableData(false);
        startBackgroundSync();
    }

    private void initComponents() {
        setTitle("Student Attendance Management System");
        setSize(900, 580);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // --- TOP PANEL: Form Inputs ---
        JPanel formPanel = new JPanel(new GridLayout(3, 4, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Student Attendance Details"));

        txtStudentId = new JTextField();
        txtFullName = new JTextField();
        txtProgram = new JTextField();
        cmbStatus = new JComboBox<>(AttendanceStatus.values());
        txtRemarks = new JTextField();
        txtSearchId = new JTextField();

        formPanel.add(new JLabel("Student ID:"));
        formPanel.add(txtStudentId);
        formPanel.add(new JLabel("Full Name:"));
        formPanel.add(txtFullName);

        formPanel.add(new JLabel("Program / Course:"));
        formPanel.add(txtProgram);
        formPanel.add(new JLabel("Attendance Status:"));
        formPanel.add(cmbStatus);

        formPanel.add(new JLabel("Remarks:"));
        formPanel.add(txtRemarks);
        formPanel.add(new JLabel("Quick Search ID:"));
        formPanel.add(txtSearchId);

        // --- BUTTON BAR ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton btnAdd = new JButton("Add Record");
        JButton btnUpdate = new JButton("Update Record");
        JButton btnDelete = new JButton("Delete Record");
        JButton btnSearch = new JButton("Search by ID");
        JButton btnSort = new JButton("Sort by Name");
        JButton btnClear = new JButton("Clear Fields");

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnSearch);
        buttonPanel.add(btnSort);
        buttonPanel.add(btnClear);

        JPanel northContainer = new JPanel(new BorderLayout());
        northContainer.setBorder(new EmptyBorder(10, 10, 0, 10));
        northContainer.add(formPanel, BorderLayout.CENTER);
        northContainer.add(buttonPanel, BorderLayout.SOUTH);
        add(northContainer, BorderLayout.NORTH);

        // --- CENTER PANEL: JTable ---
        String[] columns = {"Student ID", "Full Name", "Program", "Status", "Remarks", "Report Preview"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        attendanceTable = new JTable(tableModel);
        attendanceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(attendanceTable);
        scrollPane.setBorder(new EmptyBorder(0, 10, 0, 10));
        add(scrollPane, BorderLayout.CENTER);

        // --- SOUTH PANEL: Summary & Thread Status ---
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBorder(new EmptyBorder(5, 12, 10, 12));
        lblSummary = new JLabel("Total: 0 | Present: 0 | Late: 0 | Absent: 0 | Rate: 0.0%");
        lblSummary.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblSyncStatus = new JLabel("Initializing background sync...");
        lblSyncStatus.setForeground(new Color(0, 102, 51));

        southPanel.add(lblSummary, BorderLayout.WEST);
        southPanel.add(lblSyncStatus, BorderLayout.EAST);
        add(southPanel, BorderLayout.SOUTH);

        // --- EVENT LISTENERS (Module 7 & 8) ---
        btnAdd.addActionListener(e -> handleAdd());
        btnUpdate.addActionListener(e -> handleUpdate());
        btnDelete.addActionListener(e -> handleDelete());
        btnSearch.addActionListener(e -> handleSearch());
        btnSort.addActionListener(e -> refreshTableData(true));
        btnClear.addActionListener(e -> clearForm());

        // Populate fields when clicking a row in JTable
        attendanceTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && attendanceTable.getSelectedRow() != -1) {
                int row = attendanceTable.getSelectedRow();
                txtStudentId.setText(tableModel.getValueAt(row, 0).toString());
                txtFullName.setText(tableModel.getValueAt(row, 1).toString());
                txtProgram.setText(tableModel.getValueAt(row, 2).toString());
                cmbStatus.setSelectedItem(AttendanceStatus.valueOf(tableModel.getValueAt(row, 3).toString()));
                Object rem = tableModel.getValueAt(row, 4);
                txtRemarks.setText(rem != null ? rem.toString() : "");
            }
        });

        // Cleanly stop background thread on window close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (syncWorker != null) syncWorker.stopWorker();
            }
        });
    }

    private void handleAdd() {
        try {
            service.addAttendance(
                    txtStudentId.getText(),
                    txtFullName.getText(),
                    txtProgram.getText(),
                    (AttendanceStatus) cmbStatus.getSelectedItem(),
                    txtRemarks.getText()
            );
            refreshTableData(false);
            clearForm();
            JOptionPane.showMessageDialog(this, "Attendance recorded successfully!");
        } catch (InvalidAttendanceException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void handleUpdate() {
        try {
            service.updateAttendance(
                    txtStudentId.getText(),
                    txtFullName.getText(),
                    txtProgram.getText(),
                    (AttendanceStatus) cmbStatus.getSelectedItem(),
                    txtRemarks.getText()
            );
            refreshTableData(false);
            JOptionPane.showMessageDialog(this, "Attendance updated successfully!");
        } catch (InvalidAttendanceException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void handleDelete() {
        String id = txtStudentId.getText();
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete attendance record for Student ID: " + id + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            service.deleteAttendance(id);
            refreshTableData(false);
            clearForm();
            JOptionPane.showMessageDialog(this, "Record deleted.");
        } catch (InvalidAttendanceException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void handleSearch() {
        String searchId = txtSearchId.getText();
        if (searchId.isBlank()) {
            JOptionPane.showMessageDialog(this, "Enter a Student ID to search.");
            return;
        }
        AttendanceRecord found = service.findByStudentId(searchId);
        if (found != null) {
            txtStudentId.setText(found.getStudent().getId());
            txtFullName.setText(found.getStudent().getFullName());
            txtProgram.setText(found.getStudent().getProgram());
            cmbStatus.setSelectedItem(found.getStatus());
            txtRemarks.setText(found.getRemarks());
            JOptionPane.showMessageDialog(this, "Found: " + found.generateReportLine());
        } else {
            JOptionPane.showMessageDialog(this, "No record found for ID: " + searchId, "Not Found", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void refreshTableData(boolean sortByName) {
        try {
            List<AttendanceRecord> records = sortByName
                    ? service.getRecordsSortedByName()
                    : service.loadAndSyncRecords();

            tableModel.setRowCount(0);
            for (AttendanceRecord rec : records) {
                tableModel.addRow(new Object[]{
                        rec.getStudent().getId(),
                        rec.getStudent().getFullName(),
                        rec.getStudent().getProgram(),
                        rec.getStatus().name(),
                        rec.getRemarks(),
                        rec.generateReportLine()
                });
            }

            AttendanceSummary summary = service.computeSummary();
            lblSummary.setText(String.format(
                    "Total: %d | Present: %d | Late: %d | Absent: %d | Attendance Rate: %.1f%%",
                    summary.totalRecords(), summary.presentCount(),
                    summary.lateCount(), summary.absentCount(), summary.attendanceRate()
            ));
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void startBackgroundSync() {
        syncWorker = new SyncWorker(service, lblSyncStatus);
        Thread syncThread = new Thread(syncWorker, "Attendance-Sync-Thread");
        syncThread.setDaemon(true);
        syncThread.start();
    }

    private void clearForm() {
        txtStudentId.setText("");
        txtFullName.setText("");
        txtProgram.setText("");
        cmbStatus.setSelectedIndex(0);
        txtRemarks.setText("");
        txtSearchId.setText("");
        attendanceTable.clearSelection();
    }

    private void showDatabaseError(SQLException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this,
                "Database error occurred. Verify MySQL is running and credentials are valid.\nDetails: " + ex.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AttendanceApp app = new AttendanceApp();
            app.setVisible(true);
        });
    }
}