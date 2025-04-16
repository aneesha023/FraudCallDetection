package com.miniproject;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.miniproject.adapter.HistoryAdapter;
import com.miniproject.model.HistoryItem;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private List<HistoryItem> historyItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history); // Ensure this layout exists

        recyclerView = findViewById(R.id.historyRecyclerView); // Must exist in activity_history.xml
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Temporary hardcoded data, replace with real API/DB fetch later
        historyItems = new ArrayList<>();
        historyItems.add(new HistoryItem("1234567890", "High", "12:01 PM"));
        historyItems.add(new HistoryItem("9876543210", "Low", "12:15 PM"));
        historyItems.add(new HistoryItem("7894561230", "Medium", "12:30 PM"));

        adapter = new HistoryAdapter(historyItems);
        recyclerView.setAdapter(adapter);
    }
}
