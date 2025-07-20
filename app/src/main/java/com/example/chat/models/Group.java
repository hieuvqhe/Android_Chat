package com.example.chat.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;
import java.util.List;

// Group.java
public class Group {
    @SerializedName("_id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("avatar")
    private String avatar;

    @SerializedName("created_by")
    private String createdBy;

    @SerializedName("member_count")
    private int memberCount;

    @SerializedName("user_role")
    private String userRole; // Owner, Admin, Member

    @SerializedName("created_at")
    private Date createdAt;

    @SerializedName("updated_at")
    private Date updatedAt;

    // Additional fields
    @SerializedName("members")
    private List<GroupMember> members;

    @SerializedName("conversation_id")
    private String conversationId;

    public Group() {}

    public Group(String name, String description, String avatar, String createdBy) {
        this.name = name;
        this.description = description;
        this.avatar = avatar;
        this.createdBy = createdBy;
        this.createdAt = new Date();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
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

    public List<GroupMember> getMembers() {
        return members;
    }

    public void setMembers(List<GroupMember> members) {
        this.members = members;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    // Helper methods
    public boolean isOwner() {
        return GroupMemberRole.OWNER.equals(userRole);
    }

    public boolean isAdmin() {
        return GroupMemberRole.ADMIN.equals(userRole) || isOwner();
    }

    public boolean isMember() {
        return GroupMemberRole.MEMBER.equals(userRole);
    }

    public String getDisplayAvatar() {
        return avatar != null && !avatar.isEmpty() ? avatar :
                "https://w7.pngwing.com/pngs/821/381/png-transparent-computer-user-icon-peolpe-avatar-group.png";
    }

    // Role constants
    public static class GroupMemberRole {
        public static final String OWNER = "Owner";
        public static final String ADMIN = "Admin";
        public static final String MEMBER = "Member";
    }
}

