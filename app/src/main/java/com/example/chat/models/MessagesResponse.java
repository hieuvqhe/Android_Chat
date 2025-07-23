package com.example.chat.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MessagesResponse {
    @SerializedName("messages")
    private List<Message> messages;
    
    @SerializedName("total")
    private int total;
    
    @SerializedName("page")
    private int page;
    
    @SerializedName("total_pages")
    private int totalPages;
    
    @SerializedName("has_more")
    private boolean hasMore;
    
    public MessagesResponse() {}
    
    // Getters and Setters
    public List<Message> getMessages() {
        return messages;
    }
    
    public void setMessages(List<Message> messages) {
        this.messages = messages;
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
    
    public boolean isHasMore() {
        return hasMore;
    }
    
    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }
}
