package com.example.chat.models;

import com.google.gson.annotations.SerializedName;

public class EmailVerificationRequest {
    @SerializedName("email_verify_token")
    private String emailVerifyToken;

    public EmailVerificationRequest(String emailVerifyToken) {
        this.emailVerifyToken = emailVerifyToken;
    }

    public String getEmailVerifyToken() { return emailVerifyToken; }
    public void setEmailVerifyToken(String emailVerifyToken) { this.emailVerifyToken = emailVerifyToken; }
}