// com/miniproject/network/PredictionResponse.java
package com.miniproject.network;

import com.google.gson.annotations.SerializedName;


import com.google.gson.annotations.SerializedName;

public class PredictionResponse {

    @SerializedName("classification")
    private String classification;

    @SerializedName("spam_probability")
    private int spamProbability;

    @SerializedName("phone_number")
    private String phoneNumber;

    @SerializedName("location")
    private String location;

    @SerializedName("voip")
    private boolean voip;

    @SerializedName("risk_level")
    private String riskLevel;

    public String getClassification() {
        return classification;
    }

    public int getSpamProbability() {
        return spamProbability;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getLocation() {
        return location;
    }

    public boolean isVoip() {
        return voip;
    }

    public String getRiskLevel() {
        return riskLevel;
    }
}


