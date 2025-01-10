package com.example.oracleadmin.controller;

public class BackupScheduleRequest {
    private String dateTime;
    private boolean isIncremental;

    // Getters et setters
    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public boolean isIncremental() {
        return isIncremental;
    }

    public void setIncremental(boolean incremental) {
        isIncremental = incremental;
    }
}
