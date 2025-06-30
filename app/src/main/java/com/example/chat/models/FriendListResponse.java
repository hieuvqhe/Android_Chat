package com.example.chat.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class FriendListResponse {
    @SerializedName("result")
    private List<Friend> friends;

    @SerializedName("page")
    private int page;

    @SerializedName("total_pages")
    private int totalPages;

    @SerializedName("message")
    private String message;

    // Constructors
    public FriendListResponse() {}

    // Getters and Setters
    public List<Friend> getFriends() { return friends; }
    public void setFriends(List<Friend> friends) { this.friends = friends; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}