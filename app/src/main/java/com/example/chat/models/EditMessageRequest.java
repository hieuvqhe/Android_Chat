package com.example.chat.models;

import com.google.gson.annotations.SerializedName;

public class EditMessageRequest {
    @SerializedName("content")
    private String content;

    public EditMessageRequest() {}

    public EditMessageRequest(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}