package com.example.chat.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class User implements Serializable {
    @SerializedName("_id")
    private String id;

    @SerializedName("email")
    private String email;

    @SerializedName("username")
    private String username;

    @SerializedName("name")
    private String name;

    @SerializedName("verify")
    private Integer verify; // Use Integer to handle null values

    @SerializedName("bio")
    private String bio;

    @SerializedName("location")
    private String location;

    @SerializedName("website")
    private String website;

    @SerializedName("avatar")
    private String avatar;

    @SerializedName("cover_photo")
    private String coverPhoto;

    @SerializedName("date_of_birth")
    private String dateOfBirth;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    // Constructors
    public User() {}

    public User(String email, String username, String name) {
        this.email = email;
        this.username = username;
        this.name = name;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getVerify() { return verify; }
    public void setVerify(Integer verify) { this.verify = verify; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getCoverPhoto() { return coverPhoto; }
    public void setCoverPhoto(String coverPhoto) { this.coverPhoto = coverPhoto; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    // Helper methods
    public boolean isVerified() {
        return verify != null && verify == 1;
    }

    public String getDisplayName() {
        if (username != null && !username.trim().isEmpty() && !username.equals("null")) {
            return username;
        } else if (name != null && !name.trim().isEmpty() && !name.equals("null")) {
            return name;
        } else if (email != null && !email.trim().isEmpty() && !email.equals("null")) {
            return email;
        }
        return "Unknown User";
    }

    // Safe getters for UI display
    public String getSafeBio() {
        if (bio != null && !bio.trim().isEmpty() && !bio.equals("null")) {
            return bio;
        }
        return "";
    }

    public String getSafeLocation() {
        if (location != null && !location.trim().isEmpty() && !location.equals("null")) {
            return location;
        }
        return "";
    }

    public String getSafeWebsite() {
        if (website != null && !website.trim().isEmpty() && !website.equals("null")) {
            return website;
        }
        return "";
    }

    public String getSafeAvatar() {
        if (avatar != null && !avatar.trim().isEmpty() && !avatar.equals("null")) {
            return avatar;
        }
        return null;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", verify=" + verify +
                '}';
    }
}