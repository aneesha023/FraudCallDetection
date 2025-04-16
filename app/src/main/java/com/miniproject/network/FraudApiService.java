package com.miniproject.network;
import com.google.gson.JsonObject;


import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface FraudApiService {

    @POST("/api/analyze_transcript")
    Call<SpeechResponse> analyzeTranscript(@Body RequestBody transcriptText);


    @Multipart
    @POST("/analyze_speech")
    Call<SpeechResponse> analyzeSpeech(
            @Part MultipartBody.Part audio
    );


    @Multipart
    @POST("/predict_transcript")
    Call<SpeechResponse> uploadAudio(@Part MultipartBody.Part file);

    @Headers("Content-Type: application/json")
    @POST("/predict")
    Call<PredictionResponse> predictMetadata(@Body JsonObject metadata);

}