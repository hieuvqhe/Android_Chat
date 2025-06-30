package com.example.chat.models;

import com.google.gson.annotations.SerializedName;

public class ForgotPasswordResponseData {
    @SerializedName("note")
    private String note;

    @SerializedName("forgot_password_token")
    private String forgotPasswordToken;

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getForgotPasswordToken() { return forgotPasswordToken; }
    public void setForgotPasswordToken(String forgotPasswordToken) { this.forgotPasswordToken = forgotPasswordToken; }
}