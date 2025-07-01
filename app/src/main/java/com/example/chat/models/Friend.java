package com.example.chat.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class Friend {
    @SerializedName("_id")
    private String id;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("friend_id")
    private String friendId;

    @SerializedName("created_at")
    private Date createdAt;

    @SerializedName("updated_at")
    private Date updatedAt;

    @SerializedName("status")
    private int status; // 1 = pending, 2 = accepted, 3 = rejected

    @SerializedName("activeStatus")
    private int activeStatus; // 1 = active, 0 = inactive

    // User info (if populated by server)
    @SerializedName("user")
    private UserInfo user;

    @SerializedName("friend")
    private UserInfo friend;

    public Friend() {}

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

    public String getFriendId() {
        return friendId;
    }

    public void setFriendId(String friendId) {
        this.friendId = friendId;
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(int activeStatus) {
        this.activeStatus = activeStatus;
    }

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    public UserInfo getFriend() {
        return friend;
    }

    public void setFriend(UserInfo friend) {
        this.friend = friend;
    }

    // Helper methods
    public boolean isPending() {
        return status == 1;
    }

    public boolean isAccepted() {
        return status == 2;
    }

    public boolean isRejected() {
        return status == 3;
    }

    public boolean isActive() {
        return activeStatus == 1;
    }

    // Add getFriendInfo() method for compatibility
    public UserInfo getFriendInfo() {
        return friend != null ? friend : user;
    }
}