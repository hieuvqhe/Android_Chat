package com.example.chat.models;

import com.google.gson.annotations.SerializedName;

public class ResetPasswordRequest {
    @SerializedName("password")
    private String password;

    @SerializedName("confirm_password")
    private String confirmPassword;

    @SerializedName("forgot_password_token")
    private String forgotPasswordToken;

    public ResetPasswordRequest(String password, String confirmPassword, String forgotPasswordToken) {
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.forgotPasswordToken = forgotPasswordToken;
    }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }

    public String getForgotPasswordToken() { return forgotPasswordToken; }
    public void setForgotPasswordToken(String forgotPasswordToken) { this.forgotPasswordToken = forgotPasswordToken; }
}