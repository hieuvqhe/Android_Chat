package com.example.chat.models;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

// GroupMember.java
public class GroupMember {
    @SerializedName("_id")
    private String id;

    @SerializedName("group_id")
    private String groupId;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("role")
    private String role; // Owner, Admin, Member

    @SerializedName("status")
    private String status; // Active, Left, Removed

    @SerializedName("joined_at")
    private Date joinedAt;

    @SerializedName("left_at")
    private Date leftAt;

    @SerializedName("added_by")
    private String addedBy;

    // User info populated by server
    @SerializedName("user")
    private UserInfo user;

    public GroupMember() {}

    public GroupMember(String groupId, String userId, String role, String addedBy) {
        this.groupId = groupId;
        this.userId = userId;
        this.role = role;
        this.addedBy = addedBy;
        this.status = GroupMemberStatus.ACTIVE;
        this.joinedAt = new Date();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Date joinedAt) {
        this.joinedAt = joinedAt;
    }

    public Date getLeftAt() {
        return leftAt;
    }

    public void setLeftAt(Date leftAt) {
        this.leftAt = leftAt;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    // Helper methods
    public boolean isOwner() {
        return Group.GroupMemberRole.OWNER.equals(role);
    }

    public boolean isAdmin() {
        return Group.GroupMemberRole.ADMIN.equals(role) || isOwner();
    }

    public boolean isMember() {
        return Group.GroupMemberRole.MEMBER.equals(role);
    }

    public boolean isActive() {
        return GroupMemberStatus.ACTIVE.equals(status);
    }

    public boolean hasLeft() {
        return GroupMemberStatus.LEFT.equals(status);
    }

    public boolean isRemoved() {
        return GroupMemberStatus.REMOVED.equals(status);
    }

    public String getUserName() {
        return user != null ? user.getUsername() : "Unknown";
    }

    public String getUserAvatar() {
        return user != null ? user.getAvatar() : null;
    }

    // Status constants
    public static class GroupMemberStatus {
        public static final String ACTIVE = "Active";
        public static final String LEFT = "Left";
        public static final String REMOVED = "Removed";
    }
}
