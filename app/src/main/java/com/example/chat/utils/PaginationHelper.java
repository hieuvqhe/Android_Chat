package com.example.chat.utils;

public class PaginationHelper {
    private int currentPage = 1;
    private int totalPages = 1;
    private int pageSize = 20;
    private boolean isLoading = false;
    private boolean hasMoreData = true;

    public PaginationHelper(int pageSize) {
        this.pageSize = pageSize;
    }

    public void reset() {
        currentPage = 1;
        totalPages = 1;
        isLoading = false;
        hasMoreData = true;
    }

    public void setLoading(boolean loading) {
        this.isLoading = loading;
    }

    public void updatePagination(int totalPages) {
        this.totalPages = totalPages;
        this.hasMoreData = currentPage < totalPages;
        this.isLoading = false;
    }

    public boolean canLoadMore() {
        return !isLoading && hasMoreData;
    }

    public int getNextPage() {
        if (canLoadMore()) {
            return currentPage + 1;
        }
        return currentPage;
    }

    public void incrementPage() {
        if (canLoadMore()) {
            currentPage++;
        }
    }

    // Getters
    public int getCurrentPage() { return currentPage; }
    public int getTotalPages() { return totalPages; }
    public int getPageSize() { return pageSize; }
    public boolean isLoading() { return isLoading; }
    public boolean hasMoreData() { return hasMoreData; }
}