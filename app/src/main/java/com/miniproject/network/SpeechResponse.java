package com.miniproject.network;

import com.google.gson.annotations.SerializedName;

public class SpeechResponse {

    @SerializedName("transcription")
    private String transcription;

    @SerializedName("classification")
    private String classification;

    @SerializedName("spam_probability")
    private float spamProbability;

    // Default constructor
    public SpeechResponse() {}

    // Parameterized constructor (optional)
    public SpeechResponse(String transcription, String classification) {
        this.transcription = transcription;
        this.classification = classification;
    }

    public String getTranscription() {
        return transcription;
    }

    public void setTranscription(String transcription) {
        this.transcription = transcription;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    @Override
    public String toString() {
        return "SpeechResponse{" +
                "transcription='" + transcription + '\'' +
                ", classification='" + classification + '\'' +
                '}';
    }

    public float getSpamProbability() {
        return spamProbability;
    }
}
