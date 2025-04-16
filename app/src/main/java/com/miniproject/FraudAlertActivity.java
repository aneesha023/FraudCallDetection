package com.miniproject;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class FraudAlertActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fraud_alert);

        String transcript = getIntent().getStringExtra("transcript");
        String classification = getIntent().getStringExtra("classification");

        String number = getIntent().getStringExtra("phoneNumber");
        if (number == null) number = "Unknown";

        TextView alertTextView = findViewById(R.id.alertTextView);
        TextView transcriptTextView = findViewById(R.id.transcriptTextView);

        alertTextView.setText("⚠️ FRAUD DETECTED");
        transcriptTextView.setText("Transcript: " + transcript + "\nClassification: " + classification);
    }
}
