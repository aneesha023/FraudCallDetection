package com.miniproject.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "history_table")
public class HistoryEntry {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String phoneNumber;
    public String riskLevel;
    public String timestamp;

    public HistoryEntry(String phoneNumber, String riskLevel, String timestamp) {
        this.phoneNumber = phoneNumber;
        this.riskLevel = riskLevel;
        this.timestamp = timestamp;
    }
}
