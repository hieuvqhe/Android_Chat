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
    private int status; // FIXED: 0 = pending, 1 = accepted, 2 = rejected (based on actual API)

    @SerializedName("activeStatus")
    private int activeStatus; // 1 = active, 0 = inactive

    // User info (if populated by server)
    @SerializedName("user")
    private UserInfo user;

    // FIXED: Backend trả về "friend_info", không phải "friend"
    @SerializedName("friend_info")
    private UserInfo friendInfo;

    // Keep the old field for backward compatibility
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

    public UserInfo getFriendInfo() {
        return friendInfo;
    }

    public void setFriendInfo(UserInfo friendInfo) {
        this.friendInfo = friendInfo;
    }

    // FIXED: Helper methods with correct status values (0,1,2)
    public boolean isPending() {
        return status == 0;  // Changed from 1 to 0
    }

    public boolean isAccepted() {
        return status == 1;  // Changed from 2 to 1
    }

    public boolean isRejected() {
        return status == 2;  // Changed from 3 to 2
    }

    public boolean isActive() {
        return activeStatus == 1;
    }

    // FIXED: Get friend user info with priority order
    public UserInfo getFriendUser() {
        // Priority: friendInfo > friend > user
        if (friendInfo != null) {
            return friendInfo;
        } else if (friend != null) {
            return friend;
        } else if (user != null) {
            return user;
        }
        return null;
    }
}