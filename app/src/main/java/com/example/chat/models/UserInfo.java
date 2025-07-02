package com.example.chat.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

/**
 * Model class for User Information
 */
public class UserInfo {

    @SerializedName("_id")
    private String id;

    @SerializedName("username")
    private String username;

    @SerializedName("email")
    private String email;

    @SerializedName("bio")
    private String bio;

    @SerializedName("avatar")
    private String avatar;

    @SerializedName("created_at")
    private Date createdAt;

    @SerializedName("updated_at")
    private Date updatedAt;

    @SerializedName("verify")
    private int verify; // 0 = unverified, 1 = verified

    @SerializedName("active_status")
    private int activeStatus; // 0 = offline, 1 = online

    @SerializedName("date_of_birth")
    private Date dateOfBirth;

    @SerializedName("phone_number")
    private String phoneNumber;

    @SerializedName("cover_photo")
    private String coverPhoto;

    @SerializedName("website")
    private String website;

    @SerializedName("location")
    private String location;

    public UserInfo() {}

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

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
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

    public int getVerify() {
        return verify;
    }

    public void setVerify(int verify) {
        this.verify = verify;
    }

    public int getActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(int activeStatus) {
        this.activeStatus = activeStatus;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCoverPhoto() {
        return coverPhoto;
    }

    public void setCoverPhoto(String coverPhoto) {
        this.coverPhoto = coverPhoto;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    // Helper methods
    public boolean isVerified() {
        return verify == 1;
    }

    public boolean isOnline() {
        return activeStatus == 1;
    }

    public String getDisplayName() {
        if (username != null && !username.trim().isEmpty() && !username.equals("null")) {
            return username;
        } else if (email != null && !email.trim().isEmpty() && !email.equals("null")) {
            return email;
        }
        return "Unknown User";
    }

    // FIXED: Add null-safe getters for common operations
    public String getSafeBio() {
        if (bio != null && !bio.trim().isEmpty() && !bio.equals("null")) {
            return bio;
        }
        return "";
    }

    public String getSafeAvatar() {
        if (avatar != null && !avatar.trim().isEmpty() && !avatar.equals("null")) {
            return avatar;
        }
        return null;
    }

    public String getSafeEmail() {
        if (email != null && !email.trim().isEmpty() && !email.equals("null")) {
            return email;
        }
        return "";
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", bio='" + bio + '\'' +
                ", verify=" + verify +
                ", activeStatus=" + activeStatus +
                '}';
    }
}