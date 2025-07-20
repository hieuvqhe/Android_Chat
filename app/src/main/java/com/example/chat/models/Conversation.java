package com.example.chat.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;
import java.util.List;

public class Conversation {
    @SerializedName("_id")
    private String id;

    @SerializedName("sender_id")
    private String senderId;

    @SerializedName("receiver_id")
    private List<String> receiverIds;

    @SerializedName("type")
    private int type; // 0 = private, 1 = group

    @SerializedName("content")
    private String content; // Last message content

    @SerializedName("group_name")
    private String groupName;

    @SerializedName("group_description")
    private String groupDescription;

    @SerializedName("group_avatar")
    private String groupAvatar;

    @SerializedName("created_at")
    private Date createdAt;

    @SerializedName("update_at")
    private Date updatedAt;

    // Additional fields populated by server
    @SerializedName("other_participant")
    private UserInfo otherParticipant;

    @SerializedName("member_count")
    private int memberCount;

    @SerializedName("unread_count")
    private int unreadCount;

    @SerializedName("settings")
    private ConversationSettings settings;

    public Conversation() {}

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public List<String> getReceiverIds() {
        return receiverIds;
    }

    public void setReceiverIds(List<String> receiverIds) {
        this.receiverIds = receiverIds;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupDescription() {
        return groupDescription;
    }

    public void setGroupDescription(String groupDescription) {
        this.groupDescription = groupDescription;
    }

    public String getGroupAvatar() {
        return groupAvatar;
    }

    public void setGroupAvatar(String groupAvatar) {
        this.groupAvatar = groupAvatar;
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

    public UserInfo getOtherParticipant() {
        return otherParticipant;
    }

    public void setOtherParticipant(UserInfo otherParticipant) {
        this.otherParticipant = otherParticipant;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public ConversationSettings getSettings() {
        return settings;
    }

    public void setSettings(ConversationSettings settings) {
        this.settings = settings;
    }

    // Helper methods
    public boolean isPrivateChat() {
        return type == 0;
    }

    public boolean isGroupChat() {
        return type == 1;
    }

    public String getDisplayName() {
        if (isGroupChat()) {
            return groupName != null ? groupName : "Group Chat";
        } else {
            return otherParticipant != null ? otherParticipant.getUsername() : "Chat";
        }
    }

    public String getDisplayAvatar() {
        if (isGroupChat()) {
            return groupAvatar;
        } else {
            return otherParticipant != null ? otherParticipant.getAvatar() : null;
        }
    }

    public boolean isPinned() {
        return settings != null && settings.isPinned();
    }

    public boolean isMuted() {
        return settings != null && settings.isMuted();
    }

    public static class ConversationSettings {
        @SerializedName("pinned")
        private boolean pinned;

        @SerializedName("muted")
        private boolean muted;

        @SerializedName("muted_until")
        private Date mutedUntil;

        @SerializedName("archived")
        private boolean archived;

        @SerializedName("notifications_enabled")
        private boolean notificationsEnabled = true;

        public boolean isPinned() {
            return pinned;
        }

        public void setPinned(boolean pinned) {
            this.pinned = pinned;
        }

        public boolean isMuted() {
            return muted;
        }

        public void setMuted(boolean muted) {
            this.muted = muted;
        }

        public Date getMutedUntil() {
            return mutedUntil;
        }

        public void setMutedUntil(Date mutedUntil) {
            this.mutedUntil = mutedUntil;
        }

        public boolean isArchived() {
            return archived;
        }

        public void setArchived(boolean archived) {
            this.archived = archived;
        }

        public boolean isNotificationsEnabled() {
            return notificationsEnabled;
        }

        public void setNotificationsEnabled(boolean notificationsEnabled) {
            this.notificationsEnabled = notificationsEnabled;
        }
    }
}