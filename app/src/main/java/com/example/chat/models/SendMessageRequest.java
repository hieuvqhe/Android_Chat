package com.example.chat.models;
import com.google.gson.annotations.SerializedName;
import java.util.Date;
import java.util.List;
public class SendMessageRequest {
    @SerializedName("conversation_id")
    private String conversationId;

    @SerializedName("content")
    private String content;

    @SerializedName("message_type")
    private int messageType;

    @SerializedName("medias")
    private List<Message.Media> medias;

    @SerializedName("reply_to")
    private String replyTo;

    public SendMessageRequest() {}

    public SendMessageRequest(String conversationId, String content) {
        this.conversationId = conversationId;
        this.content = content;
        this.messageType = Message.MessageType.TEXT;
    }

    public SendMessageRequest(String conversationId, String content, int messageType, List<Message.Media> medias) {
        this.conversationId = conversationId;
        this.content = content;
        this.messageType = messageType;
        this.medias = medias;
    }

    // Getters and Setters
    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public List<Message.Media> getMedias() {
        return medias;
    }

    public void setMedias(List<Message.Media> medias) {
        this.medias = medias;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }
}

