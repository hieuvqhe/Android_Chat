package com.example.chat.models;

import com.google.gson.annotations.SerializedName;

public class ForgotPasswordRequest {
    @SerializedName("email")
    private String email;

    public ForgotPasswordRequest(String email) {
        this.email = email;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}