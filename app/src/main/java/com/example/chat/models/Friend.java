package com.example.chat.models;

import com.google.gson.annotations.SerializedName;

public class Friend {
    @SerializedName("_id")
    private String id;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("friend_id")
    private String friendId;

    @SerializedName("status")
    private int status;

    @SerializedName("activeStatus")
    private int activeStatus;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    @SerializedName("friend_info")
    private UserInfo friendInfo;

    @SerializedName("sender_info")
    private UserInfo senderInfo;

    // Constructors
    public Friend() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getFriendId() { return friendId; }
    public void setFriendId(String friendId) { this.friendId = friendId; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public int getActiveStatus() { return activeStatus; }
    public void setActiveStatus(int activeStatus) { this.activeStatus = activeStatus; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public UserInfo getFriendInfo() { return friendInfo; }
    public void setFriendInfo(UserInfo friendInfo) { this.friendInfo = friendInfo; }

    public UserInfo getSenderInfo() { return senderInfo; }
    public void setSenderInfo(UserInfo senderInfo) { this.senderInfo = senderInfo; }
}