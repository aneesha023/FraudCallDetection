package com.miniproject;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import androidx.annotation.Nullable;

import com.miniproject.network.ApiClient;
import com.miniproject.network.FraudApiService;
import com.miniproject.network.SpeechResponse;

import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LiveSpeechService extends Service {
    private SpeechRecognizer speechRecognizer;
    private FraudApiService apiService;

    @Override
    public void onCreate() {
        super.onCreate();
        apiService = ApiClient.getInstance().create(FraudApiService.class);

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle params) {}

            @Override public void onBeginningOfSpeech() {}

            @Override public void onRmsChanged(float rmsdB) {}

            @Override public void onBufferReceived(byte[] buffer) {}

            @Override public void onEndOfSpeech() {}

            @Override public void onError(int error) {
                Log.e("LiveSpeechService", "Speech recognition error: " + error);
            }

            @Override public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String finalTranscript = matches.get(0);
                    Log.d("LiveSpeech", "Final: " + finalTranscript);
                    sendTranscriptToBackend(finalTranscript);
                }
            }

            @Override public void onPartialResults(Bundle partialResults) {
                ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String partialTranscript = matches.get(0);
                    Log.d("LiveSpeech", "Partial: " + partialTranscript);
                    sendTranscriptToBackend(partialTranscript); // You can debounce this if needed
                }
            }

            @Override public void onEvent(int eventType, Bundle params) {}
        });

        speechRecognizer.startListening(intent);
    }

    private void sendTranscriptToBackend(String transcript) {
        RequestBody requestBody = RequestBody.create(transcript, MediaType.parse("text/plain"));
        Call<SpeechResponse> call = apiService.analyzeTranscript(requestBody); // Send transcript to backend for fraud detection

        call.enqueue(new Callback<SpeechResponse>() {
            @Override
            public void onResponse(Call<SpeechResponse> call, Response<SpeechResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SpeechResponse res = response.body();
                    Log.d("LiveSpeechService", "Backend Result: " + res.getClassification());
                    // You can broadcast this to the app or show a notification
                } else {
                    Log.e("LiveSpeechService", "Failed response from server");
                }
            }

            @Override
            public void onFailure(Call<SpeechResponse> call, Throwable t) {
                Log.e("LiveSpeechService", "API Call failed: " + t.getMessage());
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Return START_STICKY if you want to keep the service running
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
            speechRecognizer.destroy();
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
