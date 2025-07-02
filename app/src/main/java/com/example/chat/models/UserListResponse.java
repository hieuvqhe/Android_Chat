package com.example.chat.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class UserListResponse {
    // FIX: Backend trả về field name là "result", không phải "friend_suggestions"
    @SerializedName("result")
    private List<UserInfo> result;

    @SerializedName("total")
    private int total;

    @SerializedName("page")
    private int page;

    @SerializedName("total_pages")
    private int totalPages;

    public UserListResponse() {}

    public List<UserInfo> getResult() {
        return result;
    }

    public void setResult(List<UserInfo> result) {
        this.result = result;
    }

    // Add getUsers() method for compatibility
    public List<UserInfo> getUsers() {
        return result;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}