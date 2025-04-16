package com.miniproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

public class CallReceiver extends BroadcastReceiver {
    private static final String TAG = "CallReceiver";

    private static final Set<String> knownNumbers = new HashSet<String>() {{
        add("+919999999999"); // Add your known numbers here
    }};
    @Override
    public void onReceive(Context context, Intent intent) {
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
        Log.d(TAG, "Call state: " + state + ", Incoming number: " + incomingNumber);

        if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
            // Handle incoming call (Ringing state)
            Log.d(TAG, "Phone is ringing. Incoming number: " + incomingNumber);
            // Save the number for later use
            if (incomingNumber != null && !knownNumbers.contains(incomingNumber)) {
                // If the number is not known, trigger the call recording service
                Intent serviceIntent = new Intent(context, CallRecordingService.class);
                serviceIntent.putExtra("incomingNumber", incomingNumber);  // Pass the incoming number
                context.startService(serviceIntent);
            }
        }

        if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state)) {
            // Call answered
            Log.d(TAG, "Call answered!");
            Intent serviceIntent = new Intent(context, CallRecordingService.class);
            context.startService(serviceIntent); // Start the call recording service
        }

        if (TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
            // Call ended or idle
            Log.d(TAG, "Call ended or idle.");
        }
    }
}
