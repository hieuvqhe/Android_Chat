package com.example.chat.models;

import com.google.gson.annotations.SerializedName;

// FIXED: API Response với pagination fields ở top level
public class ApiResponse<T> {
    @SerializedName("message")
    private String message;

    @SerializedName("result")
    private T result;

    // ADDED: Pagination fields ở top level (như API thực tế)
    @SerializedName("page")
    private int page;

    @SerializedName("total_pages")
    private int totalPages;

    // Note: API không có "total" field, chỉ có "total_pages"

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    // ADDED: Pagination getters
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

    // Helper method to calculate total items estimate
    public int getEstimatedTotal() {
        // Since API doesn't provide total, estimate based on pages
        return totalPages * 20; // Assuming 20 items per page
    }
}