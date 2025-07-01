package com.example.chat.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class UserInfo {
    @SerializedName("_id")
    private String id;

    @SerializedName("username")
    private String username;

    @SerializedName("email")
    private String email;

    @SerializedName("avatar")
    private String avatar;

    @SerializedName("bio")
    private String bio;

    @SerializedName("location")
    private String location;

    @SerializedName("website")
    private String website;

    @SerializedName("verify")
    private int verify; // 0 = unverified, 1 = verified, 2 = banned

    @SerializedName("created_at")
    private Date createdAt;

    @SerializedName("updated_at")
    private Date updatedAt;

    // Additional fields that might be present
    @SerializedName("date_of_birth")
    private Date dateOfBirth;

    @SerializedName("cover_photo")
    private String coverPhoto;

    @SerializedName("active_status")
    private int activeStatus; // For online/offline status

    public UserInfo() {}

    public UserInfo(String id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public int getVerify() {
        return verify;
    }

    public void setVerify(int verify) {
        this.verify = verify;
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

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getCoverPhoto() {
        return coverPhoto;
    }

    public void setCoverPhoto(String coverPhoto) {
        this.coverPhoto = coverPhoto;
    }

    public int getActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(int activeStatus) {
        this.activeStatus = activeStatus;
    }

    // Helper methods
    public boolean isVerified() {
        return verify == 1;
    }

    public boolean isBanned() {
        return verify == 2;
    }

    public boolean isOnline() {
        return activeStatus == 1;
    }

    public String getDisplayName() {
        return username != null && !username.isEmpty() ? username : email;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        UserInfo userInfo = (UserInfo) obj;
        return id != null ? id.equals(userInfo.id) : userInfo.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", verified=" + isVerified() +
                '}';
    }
}