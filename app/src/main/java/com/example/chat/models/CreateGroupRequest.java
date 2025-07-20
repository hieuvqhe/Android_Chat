package com.example.chat.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CreateGroupRequest {
    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("member_ids")
    private List<String> memberIds;

    @SerializedName("avatar")
    private String avatar;

    public CreateGroupRequest() {}

    public CreateGroupRequest(String name, String description, List<String> memberIds) {
        this.name = name;
        this.description = description;
        this.memberIds = memberIds;
    }

    public CreateGroupRequest(String name, String description, List<String> memberIds, String avatar) {
        this.name = name;
        this.description = description;
        this.memberIds = memberIds;
        this.avatar = avatar;
    }

    // Getters and Setters
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

    public List<String> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<String> memberIds) {
        this.memberIds = memberIds;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
