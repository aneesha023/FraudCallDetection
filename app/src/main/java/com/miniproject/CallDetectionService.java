package com.miniproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

public class CallDetectionService extends BroadcastReceiver {

    private static final String TAG = "CallDetectionService";

    // Dummy known numbers list — in a real app you'd use Contacts
    private static final Set<String> knownNumbers = new HashSet<String>() {{
        add("+919999999999"); // Add your known numbers here
    }};

    @Override
    public void onReceive(Context context, Intent intent) {
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

        Log.d(TAG, "Call state: " + state + ", Number: " + incomingNumber);

        if (state != null && state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
            // Call answered
            if (incomingNumber != null && !knownNumbers.contains(incomingNumber)) {
                Log.d(TAG, "Unknown number answered. Starting recording...");
                Intent recordIntent = new Intent(context, CallRecordingService.class);
                recordIntent.putExtra("number", incomingNumber); // Send number to service
                context.startService(recordIntent);
            }
        }
    }
}
