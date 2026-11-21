package com.attendance.service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

public class SyncWorker implements Runnable {
    private final AttendanceService service;
    private final JLabel statusLabel;
    private volatile boolean running = true;       

    public SyncWorker(AttendanceService service, JLabel statusLabel) {
        this.service = service;
        this.statusLabel = statusLabel;
    }

    public void stopWorker() {
        running = false;
    }

    @Override
    public void run() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm:ss");
        while (running) {
            try {
                Thread.sleep(15000); // Sync every 15 seconds in background
                service.loadAndSyncRecords();
                String timeNow = LocalTime.now().format(fmt);
                SwingUtilities.invokeLater(() ->
                    statusLabel.setText("Background Sync Active | Last synced: " + timeNow)
                );
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                SwingUtilities.invokeLater(() ->
                    statusLabel.setText("Sync Warning: Check MySQL Connection")
                );
            }
        }
    }
}