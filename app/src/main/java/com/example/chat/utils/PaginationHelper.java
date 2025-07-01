package com.example.chat.utils;

public class PaginationHelper {
    private int currentPage = 1;
    private int pageSize;
    private int totalPages = 1;
    private boolean isLoading = false;
    private boolean hasMore = true;

    public PaginationHelper(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setLoading(boolean loading) {
        this.isLoading = loading;
    }

    public boolean hasMore() {
        return hasMore;
    }

    public boolean canLoadMore() {
        return !isLoading && hasMore && currentPage < totalPages;
    }

    public void incrementPage() {
        if (currentPage < totalPages) {
            currentPage++;
        }
    }

    public int getNextPage() {
        return currentPage + 1;
    }

    public void updatePagination(int totalPages) {
        this.totalPages = totalPages;
        this.hasMore = currentPage < totalPages;
        this.isLoading = false;
    }

    public void reset() {
        currentPage = 1;
        totalPages = 1;
        isLoading = false;
        hasMore = true;
    }

    public void setCurrentPage(int page) {
        this.currentPage = page;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
        this.hasMore = currentPage < totalPages;
    }
}