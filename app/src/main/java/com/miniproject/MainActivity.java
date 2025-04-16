package com.miniproject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.JsonObject;
import com.miniproject.network.ApiClient;
import com.miniproject.network.FraudApiService;
import com.miniproject.network.PredictionResponse;
import com.miniproject.network.SpeechResponse;
import com.miniproject.HistoryActivity;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS_CODE = 123;
    private Button btnUploadAudio, btnCheckMetadata;
    private TextView resultTextView;
    private FraudApiService apiService;
    private TextView classificationText;
    private TextView spamProbabilityText;
    private TextView numberText;
    private TextView locationText;
    private TextView voipText;
    private TextView riskLevelText;


    ActivityResultLauncher<String> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    Log.d("flow_Check", "File selected: " + uri.toString());
                    uploadAudioFile(uri);
                } else {
                    Toast.makeText(this, "File selection cancelled", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resultTextView = findViewById(R.id.transcriptTextView);
        btnUploadAudio = findViewById(R.id.btnUploadAudio);
        btnCheckMetadata = findViewById(R.id.btnCheckMetadata);
        apiService = ApiClient.getInstance().create(FraudApiService.class);
        classificationText = findViewById(R.id.classificationText);
        spamProbabilityText = findViewById(R.id.spamProbabilityText);
        numberText = findViewById(R.id.numberText);
        locationText = findViewById(R.id.locationText);
        voipText = findViewById(R.id.voipText);
        riskLevelText = findViewById(R.id.riskLevelText);


        checkAndRequestPermissions();

        btnUploadAudio.setOnClickListener(view -> {
            if (hasPermissions()) {
                filePickerLauncher.launch("audio/*");
            } else {
                Toast.makeText(this, "Please grant all permissions to continue", Toast.LENGTH_SHORT).show();
                checkAndRequestPermissions();
            }
        });

        btnCheckMetadata.setOnClickListener(view -> {
            if (hasPermissions()) {
                checkCallerMetadata();
            } else {
                Toast.makeText(this, "Permissions required to proceed", Toast.LENGTH_SHORT).show();
                checkAndRequestPermissions();
            }
        });
    }

    private void checkAndRequestPermissions() {
        if (!hasPermissions()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.READ_MEDIA_AUDIO,
                        Manifest.permission.READ_PHONE_STATE
                }, REQUEST_PERMISSIONS_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.READ_PHONE_STATE
                }, REQUEST_PERMISSIONS_CODE);
            }
        }
    }

    private boolean hasPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (!allGranted) {
                Toast.makeText(this, "Some permissions were denied", Toast.LENGTH_LONG).show();
                openAppSettings();
            }
        }
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    private void uploadAudioFile(Uri uri) {
        try {
            File file = createTempFileFromUri(uri);
            Log.d("flow_Check", "Temp file created: " + file.getAbsolutePath());

            RequestBody requestFile = RequestBody.create(file, MediaType.parse("audio/*"));
            MultipartBody.Part body = MultipartBody.Part.createFormData("audio", file.getName(), requestFile);

            Call<SpeechResponse> call = apiService.analyzeSpeech(body);
            call.enqueue(new Callback<SpeechResponse>() {
                @Override
                public void onResponse(Call<SpeechResponse> call, Response<SpeechResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        SpeechResponse speechResponse = response.body();
                        String transcription = speechResponse.getTranscription();
                        String classi = speechResponse.getClassification();

                        if (transcription == null || transcription.isEmpty()) transcription = "null";
                        if (classi == null || classi.isEmpty()) classi = "unknown";

                        resultTextView.setText("Transcript: " + transcription + "\nClassification: " + classi.toUpperCase());
                    } else {
                        resultTextView.setText("Error: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<SpeechResponse> call, Throwable t) {
                    resultTextView.setText("Failure: " + t.getMessage());
                }
            });

        } catch (Exception e) {
            Log.e("UploadAudioFile", "Error: ", e);
            Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private File createTempFileFromUri(Uri uri) throws Exception {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        if (inputStream == null) throw new Exception("Failed to open URI");

        File tempFile = File.createTempFile("upload_", ".wav", getCacheDir());
        FileOutputStream outputStream = new FileOutputStream(tempFile);

        byte[] buffer = new byte[4096];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }

        outputStream.close();
        inputStream.close();

        return tempFile;
    }

    private void checkCallerMetadata() {
        JsonObject metadata = new JsonObject();
        metadata.addProperty("call_duration", 30);
        metadata.addProperty("time_of_day", 15);
        metadata.addProperty("day_of_week", 2);
        metadata.addProperty("caller_location_risk", 0.75);

        FraudApiService service = ApiClient.getInstance().create(FraudApiService.class);
        Call<PredictionResponse> call = service.predictMetadata(metadata);

        call.enqueue(new Callback<PredictionResponse>() {
            @Override
            public void onResponse(Call<PredictionResponse> call, Response<PredictionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PredictionResponse result = response.body();

                    // Update UI accordingly
                    classificationText.setText(result.getClassification());
                    spamProbabilityText.setText(result.getSpamProbability() + "%");

                    numberText.setText(result.getPhoneNumber());
                    locationText.setText(result.getLocation());
                    voipText.setText(result.isVoip() ? "Yes" : "No");
                    riskLevelText.setText(result.getRiskLevel());
                } else {
                    Toast.makeText(MainActivity.this, "Failed to get prediction", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PredictionResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }

    public void onTryAnotherClick(View view) {
        // Reset all views
        TextView transcriptView = findViewById(R.id.transcriptTextView);
        TextView classificationView = findViewById(R.id.classificationText);
        TextView spamProbView = findViewById(R.id.spamProbabilityText);
        TextView numberView = findViewById(R.id.numberText);
        TextView locationView = findViewById(R.id.locationText);
        TextView voipView = findViewById(R.id.voipText);
        TextView riskLevelView = findViewById(R.id.riskLevelText);

        transcriptView.setText("Transcript: --");
        classificationView.setText("Classification: --");
        spamProbView.setText("Spam Probability: --%");
        numberView.setText("📱 Number: --");
        locationView.setText("📍 Location: --");
        voipView.setText("🌐 VoIP Detected: --");
        riskLevelView.setText("⚠️ Risk Level: --");

        Toast.makeText(this, "Cleared! Ready for new call.", Toast.LENGTH_SHORT).show();
    }

    public void onViewHistoryClick(View view) {
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
    }
    public static class HistoryActivity extends MainActivity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_history);

            // You can load call history here from SharedPreferences, DB, or temp storage
        }
    }


}