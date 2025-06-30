package com.example.chat.models;

import com.google.gson.annotations.SerializedName;

public class UserInfo {
    @SerializedName("_id")
    private String id;

    @SerializedName("email")
    private String email;

    @SerializedName("username")
    private String username;

    @SerializedName("avatar")
    private String avatar;

    @SerializedName("bio")
    private String bio;

    @SerializedName("verify")
    private int verify;

    @SerializedName("location")
    private String location;

    @SerializedName("website")
    private String website;

    // Constructors
    public UserInfo() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public int getVerify() { return verify; }
    public void setVerify(int verify) { this.verify = verify; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
}