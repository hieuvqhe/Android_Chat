package com.example.chat.models;

import com.google.gson.annotations.SerializedName;

public class CreatePrivateConversationRequest {
    @SerializedName("receiver_id")
    private String receiverId;

    public CreatePrivateConversationRequest() {}

    public CreatePrivateConversationRequest(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }
}