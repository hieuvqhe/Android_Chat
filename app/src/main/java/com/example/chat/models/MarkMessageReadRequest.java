package com.example.chat.models;

import com.google.gson.annotations.SerializedName;

public class MarkMessageReadRequest {
    @SerializedName("conversation_id")
    private String conversationId;

    public MarkMessageReadRequest() {}

    public MarkMessageReadRequest(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
}
