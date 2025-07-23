package com.example.chat.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class Notification {
    @SerializedName("_id")
    private String id;
    
    @SerializedName("user_id")
    private String userId;
    
    @SerializedName("type")
    private String type; // "message", "friend_request", "group_invite", etc.
    
    @SerializedName("title")
    private String title;
    
    @SerializedName("content")
    private String content;
    
    @SerializedName("data")
    private NotificationData data;
    
    @SerializedName("read")
    private boolean read = false;
    
    @SerializedName("created_at")
    private Date createdAt;
    
    @SerializedName("updated_at")
    private Date updatedAt;
    
    // Constructors
    public Notification() {}
    
    public Notification(String type, String title, String content) {
        this.type = type;
        this.title = title;
        this.content = content;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public NotificationData getData() {
        return data;
    }
    
    public void setData(NotificationData data) {
        this.data = data;
    }
    
    public boolean isRead() {
        return read;
    }
    
    public void setRead(boolean read) {
        this.read = read;
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
    
    // Helper methods
    public boolean isMessageNotification() {
        return "message".equals(type);
    }
    
    public boolean isFriendRequestNotification() {
        return "friend_request".equals(type);
    }
    
    public boolean isGroupInviteNotification() {
        return "group_invite".equals(type);
    }
    
    public String getTimeAgo() {
        if (createdAt == null) return "";
        
        long diff = System.currentTimeMillis() - createdAt.getTime();
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return days + "d ago";
        } else if (hours > 0) {
            return hours + "h ago";
        } else if (minutes > 0) {
            return minutes + "m ago";
        } else {
            return "Just now";
        }
    }
    
    // Nested class for notification data
    public static class NotificationData {
        @SerializedName("conversation_id")
        private String conversationId;
        
        @SerializedName("message_id")
        private String messageId;
        
        @SerializedName("sender_id")
        private String senderId;
        
        @SerializedName("sender_name")
        private String senderName;
        
        @SerializedName("sender_avatar")
        private String senderAvatar;
        
        @SerializedName("group_id")
        private String groupId;
        
        @SerializedName("group_name")
        private String groupName;
        
        @SerializedName("friend_request_id")
        private String friendRequestId;
        
        // Getters and Setters
        public String getConversationId() {
            return conversationId;
        }
        
        public void setConversationId(String conversationId) {
            this.conversationId = conversationId;
        }
        
        public String getMessageId() {
            return messageId;
        }
        
        public void setMessageId(String messageId) {
            this.messageId = messageId;
        }
        
        public String getSenderId() {
            return senderId;
        }
        
        public void setSenderId(String senderId) {
            this.senderId = senderId;
        }
        
        public String getSenderName() {
            return senderName;
        }
        
        public void setSenderName(String senderName) {
            this.senderName = senderName;
        }
        
        public String getSenderAvatar() {
            return senderAvatar;
        }
        
        public void setSenderAvatar(String senderAvatar) {
            this.senderAvatar = senderAvatar;
        }
        
        public String getGroupId() {
            return groupId;
        }
        
        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }
        
        public String getGroupName() {
            return groupName;
        }
        
        public void setGroupName(String groupName) {
            this.groupName = groupName;
        }
        
        public String getFriendRequestId() {
            return friendRequestId;
        }
        
        public void setFriendRequestId(String friendRequestId) {
            this.friendRequestId = friendRequestId;
        }
    }
    
    @Override
    public String toString() {
        return "Notification{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", read=" + read +
                ", createdAt=" + createdAt +
                '}';
    }
}
