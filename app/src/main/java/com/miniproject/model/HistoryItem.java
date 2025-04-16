package com.miniproject.model;

public class HistoryItem {
    private String phoneNumber;
    private String riskLevel;
    private String time;

    public HistoryItem(String phoneNumber, String riskLevel, String time) {
        this.phoneNumber = phoneNumber;
        this.riskLevel = riskLevel;
        this.time = time;
    }

    public String getPhoneNumber() { return phoneNumber; }
    public String getRiskLevel() { return riskLevel; }
    public String getTime() { return time; }
}
