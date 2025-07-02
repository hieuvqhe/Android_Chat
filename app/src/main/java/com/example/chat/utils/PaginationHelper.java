package com.example.chat.utils;

/**
 * Helper class for handling pagination logic
 */
public class PaginationHelper {

    private int currentPage;
    private int pageSize;
    private int totalPages;
    private boolean isLoading;
    private boolean hasMore;

    public PaginationHelper(int pageSize) {
        this.pageSize = pageSize;
        this.currentPage = 1;
        this.totalPages = 0;
        this.isLoading = false;
        this.hasMore = true;
    }

    /**
     * Reset pagination to initial state
     */
    public void reset() {
        this.currentPage = 1;
        this.totalPages = 0;
        this.isLoading = false;
        this.hasMore = true;
    }

    /**
     * Update pagination info after receiving response
     */
    public void updatePagination(int totalPages) {
        this.totalPages = totalPages;
        this.hasMore = currentPage < totalPages;
        this.isLoading = false;
    }

    /**
     * Increment current page for next load
     */
    public void incrementPage() {
        if (canLoadMore()) {
            currentPage++;
        }
    }

    /**
     * Check if more data can be loaded
     */
    public boolean canLoadMore() {
        return hasMore && !isLoading && (totalPages == 0 || currentPage < totalPages);
    }

    /**
     * Get next page number without incrementing current page
     */
    public int getNextPage() {
        return currentPage + 1;
    }

    // Getters and Setters
    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
        this.hasMore = currentPage < totalPages;
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

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }

    /**
     * Check if this is the first page
     */
    public boolean isFirstPage() {
        return currentPage == 1;
    }

    /**
     * Check if this is the last page
     */
    public boolean isLastPage() {
        return totalPages > 0 && currentPage >= totalPages;
    }

    /**
     * Get total items count estimate based on current page and page size
     */
    public int getEstimatedTotalItems() {
        if (totalPages > 0) {
            return totalPages * pageSize;
        }
        return 0;
    }

    /**
     * Get current offset for database queries
     */
    public int getCurrentOffset() {
        return (currentPage - 1) * pageSize;
    }

    @Override
    public String toString() {
        return "PaginationHelper{" +
                "currentPage=" + currentPage +
                ", pageSize=" + pageSize +
                ", totalPages=" + totalPages +
                ", isLoading=" + isLoading +
                ", hasMore=" + hasMore +
                '}';
    }
}