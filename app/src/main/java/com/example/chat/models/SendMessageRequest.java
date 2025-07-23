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
    private String messageType; // "text", "image", "video", "file", "audio"

    @SerializedName("medias")
    private List<Message.Media> medias;

    @SerializedName("reply_to")
    private String replyTo;

    public SendMessageRequest() {}

    public SendMessageRequest(String conversationId, String content) {
        this.conversationId = conversationId;
        this.content = content;
        this.messageType = "text"; // Default to text
    }

    public SendMessageRequest(String conversationId, String content, String messageType, List<Message.Media> medias) {
        this.conversationId = conversationId;
        this.content = content;
        this.messageType = messageType;
        this.medias = medias;
    }

    // Backward compatibility constructor
    public SendMessageRequest(String conversationId, String content, int messageTypeInt, List<Message.Media> medias) {
        this.conversationId = conversationId;
        this.content = content;
        this.setMessageType(messageTypeInt); // Use the conversion method
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

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    // Convenience method for backward compatibility
    public void setMessageType(int messageTypeInt) {
        switch (messageTypeInt) {
            case 0: this.messageType = "text"; break;
            case 1: this.messageType = "image"; break;
            case 2: this.messageType = "video"; break;
            case 3: this.messageType = "file"; break;
            case 4: this.messageType = "audio"; break;
            default: this.messageType = "text"; break;
        }
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

