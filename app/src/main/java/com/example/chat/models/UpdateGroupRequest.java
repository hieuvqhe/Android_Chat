package com.example.chat.models;

import com.google.gson.annotations.SerializedName;

public class UpdateGroupRequest {
    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("avatar")
    private String avatar;

    public UpdateGroupRequest() {}

    public UpdateGroupRequest(String name, String description, String avatar) {
        this.name = name;
        this.description = description;
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

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
