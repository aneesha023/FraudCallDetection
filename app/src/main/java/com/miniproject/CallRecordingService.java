package com.miniproject;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.miniproject.network.ApiClient;
import com.miniproject.network.FraudApiService;
import com.miniproject.network.SpeechResponse;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CallRecordingService extends Service {

    private static final String TAG = "CallRecordingService";
    private MediaRecorder recorder;
    private File outputFile;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, ">>> onStartCommand triggered");

        String incomingNumber = intent.getStringExtra("number");  // Get incoming number
        startRecording();
        return START_NOT_STICKY;
    }

    private void startRecording() {
        try {
            Log.d(TAG, ">>> startRecording() called");

            File dir = new File(getExternalFilesDir(null), "recordings");
            if (!dir.exists()) dir.mkdirs();

            outputFile = new File(dir, "call_record.3gp");
            Log.d(TAG, "Output file path: " + outputFile.getAbsolutePath());

            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC); // Use MIC for emulator/dev
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setOutputFile(outputFile.getAbsolutePath());

            recorder.prepare();
            recorder.start();
            Log.d(TAG, "Recording started at: " + System.currentTimeMillis());

            // Stop after 15 seconds and then upload
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                stopRecording();
                uploadRecording();
            }, 15000); // 15 seconds recording

        } catch (IOException e) {
            Log.e(TAG, "Recording failed", e);
        }
    }

    private void stopRecording() {
        try {
            if (recorder != null) {
                recorder.stop();
                recorder.release();
                recorder = null;
                Log.d(TAG, "Recording stopped at: " + System.currentTimeMillis());
                Log.d(TAG, "File saved at: " + outputFile.getAbsolutePath());
            }
        } catch (Exception e) {
            Log.e(TAG, "stopRecording error", e);
        }
    }

    private void uploadRecording() {
        if (outputFile == null || !outputFile.exists()) {
            Log.e(TAG, "No file to upload.");
            stopSelf();
            return;
        }

        Log.d(TAG, ">>> uploadRecording() started");
        Log.d(TAG, "Uploading file: " + outputFile.getAbsolutePath());

        FraudApiService service = ApiClient.getInstance().create(FraudApiService.class);
        RequestBody requestFile = RequestBody.create(outputFile, MediaType.parse("audio/3gp"));
        MultipartBody.Part body = MultipartBody.Part.createFormData("audio", outputFile.getName(), requestFile);

        service.analyzeSpeech(body).enqueue(new Callback<SpeechResponse>() {
            @Override
            public void onResponse(Call<SpeechResponse> call, Response<SpeechResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String transcript = response.body().getTranscription();
                    String classification = response.body().getClassification();

                    Log.d(TAG, "Transcription: " + transcript + " | Classification: " + classification);

                    if ("Fraud".equalsIgnoreCase(classification)) {
                        Log.d(TAG, "🚨 Fraud Detected! Launching alert activity.");
                        Intent alertIntent = new Intent(getApplicationContext(), FraudAlertActivity.class);
                        alertIntent.putExtra("transcript", transcript);
                        alertIntent.putExtra("classification", classification);
                        alertIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(alertIntent);
                    } else {
                        Log.d(TAG, "✅ Safe call");
                        new Handler(Looper.getMainLooper()).post(() -> {
                            Toast.makeText(getApplicationContext(), "Call classified as Safe", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    Log.e(TAG, "Backend error: " + response.code());
                }

                stopSelf();
            }

            @Override
            public void onFailure(Call<SpeechResponse> call, Throwable t) {
                Log.e(TAG, "Upload failed", t);
                stopSelf();
            }
        });
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
