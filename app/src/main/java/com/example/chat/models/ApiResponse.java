package com.example.chat.models;

import com.google.gson.annotations.SerializedName;

// Base API Response
public class ApiResponse<T> {
    @SerializedName("message")
    private String message;

    @SerializedName("result")
    private T result;

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public T getResult() { return result; }
    public void setResult(T result) { this.result = result; }
}