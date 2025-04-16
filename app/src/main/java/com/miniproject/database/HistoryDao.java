package com.miniproject.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface HistoryDao {
    @Insert
    void insert(HistoryEntry entry);

    @Query("SELECT * FROM history_table ORDER BY id DESC")
    List<HistoryEntry> getAllHistory();
}
