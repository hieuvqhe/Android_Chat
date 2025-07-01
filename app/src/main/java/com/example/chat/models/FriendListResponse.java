package com.example.chat.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class FriendListResponse {
    @SerializedName("friends")
    private List<Friend> result;

    @SerializedName("total")
    private int total;

    @SerializedName("page")
    private int page;

    @SerializedName("total_pages")
    private int totalPages;

    public FriendListResponse() {}

    public List<Friend> getResult() {
        return result;
    }

    public void setResult(List<Friend> result) {
        this.result = result;
    }

    // Add getFriends() method for compatibility
    public List<Friend> getFriends() {
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