package com.example.chat.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;
import java.util.List;

public class Message {
    @SerializedName("_id")
    private String id;

    @SerializedName("conversation_id")
    private String conversationId;

    @SerializedName("sender_id")
    private String senderId;

    @SerializedName("content")
    private String content;

    @SerializedName("message_type")
    private String messageTypeString; // "text", "image", "video", "file", "audio"

    // Computed field for backward compatibility
    private transient int messageType;

    @SerializedName("medias")
    private List<Media> medias;

    @SerializedName("reply_to")
    private String replyTo; // Message ID being replied to

    @SerializedName("status")
    private String statusString; // "sent", "delivered", "read"

    // Computed field for backward compatibility
    private transient int status;

    @SerializedName("edited")
    private boolean edited;

    @SerializedName("edited_at")
    private Date editedAt;

    @SerializedName("created_at")
    private Date createdAt;

    @SerializedName("updated_at")
    private Date updatedAt;

    // Additional fields populated by server
    @SerializedName("sender")
    private UserInfo sender;

    @SerializedName("reply_to_message")
    private Message replyToMessage;

    public Message() {}

    public Message(String conversationId, String senderId, String content, int messageType) {
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.content = content;
        this.messageType = messageType;
        this.statusString = "sent"; // Default to sent
        this.edited = false;
        this.createdAt = new Date();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getMessageType() {
        // Convert string to int if needed
        if (messageTypeString != null) {
            switch (messageTypeString.toLowerCase()) {
                case "text": return 0;
                case "image": return 1;
                case "video": return 2;
                case "file": return 3;
                case "audio": return 4;
                default: return 0; // Default to text
            }
        }
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
        // Also set string representation
        switch (messageType) {
            case 0: this.messageTypeString = "text"; break;
            case 1: this.messageTypeString = "image"; break;
            case 2: this.messageTypeString = "video"; break;
            case 3: this.messageTypeString = "file"; break;
            case 4: this.messageTypeString = "audio"; break;
            default: this.messageTypeString = "text"; break;
        }
    }

    public String getMessageTypeString() {
        return messageTypeString;
    }

    public void setMessageTypeString(String messageTypeString) {
        this.messageTypeString = messageTypeString;
    }

    public List<Media> getMedias() {
        return medias;
    }

    public void setMedias(List<Media> medias) {
        this.medias = medias;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    public int getStatus() {
        // Convert string to int if needed
        if (statusString != null) {
            switch (statusString.toLowerCase()) {
                case "sent": return 0;
                case "delivered": return 1;
                case "read": return 2;
                default: return 0; // Default to sent
            }
        }
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
        // Also set string representation
        switch (status) {
            case 0: this.statusString = "sent"; break;
            case 1: this.statusString = "delivered"; break;
            case 2: this.statusString = "read"; break;
            default: this.statusString = "sent"; break;
        }
    }

    public String getStatusString() {
        return statusString;
    }

    public void setStatusString(String statusString) {
        this.statusString = statusString;
    }

    public boolean isEdited() {
        return edited;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }

    public Date getEditedAt() {
        return editedAt;
    }

    public void setEditedAt(Date editedAt) {
        this.editedAt = editedAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public UserInfo getSender() {
        return sender;
    }

    public void setSender(UserInfo sender) {
        this.sender = sender;
    }

    public Message getReplyToMessage() {
        return replyToMessage;
    }

    public void setReplyToMessage(Message replyToMessage) {
        this.replyToMessage = replyToMessage;
    }

    // Helper methods
    public boolean isTextMessage() {
        return messageType == MessageType.TEXT;
    }

    public boolean isImageMessage() {
        return messageType == MessageType.IMAGE;
    }

    public boolean isVideoMessage() {
        return messageType == MessageType.VIDEO;
    }

    public boolean isFileMessage() {
        return messageType == MessageType.FILE;
    }

    public boolean isAudioMessage() {
        return messageType == MessageType.AUDIO;
    }

    public boolean isSent() {
        return status == MessageStatus.SENT;
    }

    public boolean isDelivered() {
        return status == MessageStatus.DELIVERED;
    }

    public boolean isRead() {
        return status == MessageStatus.READ;
    }

    public boolean hasReply() {
        return replyTo != null && !replyTo.isEmpty();
    }

    public boolean hasMedia() {
        return medias != null && !medias.isEmpty();
    }

    public String getSenderName() {
        return sender != null ? sender.getUsername() : "Unknown";
    }

    public String getSenderAvatar() {
        return sender != null ? sender.getAvatar() : null;
    }

    // Message Type Constants
    public static class MessageType {
        public static final int TEXT = 0;
        public static final int IMAGE = 1;
        public static final int VIDEO = 2;
        public static final int FILE = 3;
        public static final int AUDIO = 4;
    }

    // Message Status Constants
    public static class MessageStatus {
        public static final int SENT = 0;
        public static final int DELIVERED = 1;
        public static final int READ = 2;
    }

    // Media class for attachments
    public static class Media {
        @SerializedName("url")
        private String url;

        @SerializedName("type")
        private String type; // image, video, file, audio

        @SerializedName("name")
        private String name;

        @SerializedName("size")
        private long size;

        public Media() {}

        public Media(String url, String type, String name, long size) {
            this.url = url;
            this.type = type;
            this.name = name;
            this.size = size;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }
    }
}