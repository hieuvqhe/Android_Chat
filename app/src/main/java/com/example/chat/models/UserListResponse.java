package com.example.chat.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class UserListResponse {
    @SerializedName("result")
    private List<UserInfo> users;

    @SerializedName("page")
    private int page;

    @SerializedName("total_pages")
    private int totalPages;

    @SerializedName("message")
    private String message;

    // Constructors
    public UserListResponse() {}

    // Getters and Setters
    public List<UserInfo> getUsers() { return users; }
    public void setUsers(List<UserInfo> users) { this.users = users; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}