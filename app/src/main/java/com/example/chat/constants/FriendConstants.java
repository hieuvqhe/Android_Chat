package com.example.chat.constants;

public class FriendConstants {

    // Friend status constants (based on backend enum)
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_ACCEPTED = 1;
    public static final int STATUS_REJECTED = 2;

    // User verification status
    public static final int USER_UNVERIFIED = 0;
    public static final int USER_VERIFIED = 1;
    public static final int USER_BANNED = 2;

    // Active status
    public static final int ACTIVE_STATUS_OFFLINE = 0;
    public static final int ACTIVE_STATUS_ONLINE = 1;

    // Pagination defaults
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int FIRST_PAGE = 1;

    // Status text for UI
    public static String getStatusText(int status) {
        switch (status) {
            case STATUS_PENDING: return "Pending";
            case STATUS_ACCEPTED: return "Friends";
            case STATUS_REJECTED: return "Rejected";
            default: return "Unknown";
        }
    }

    public static String getVerificationText(int verify) {
        switch (verify) {
            case USER_UNVERIFIED: return "Unverified";
            case USER_VERIFIED: return "Verified";
            case USER_BANNED: return "Banned";
            default: return "Unknown";
        }
    }

    public static String getActiveStatusText(int activeStatus) {
        switch (activeStatus) {
            case ACTIVE_STATUS_OFFLINE: return "Offline";
            case ACTIVE_STATUS_ONLINE: return "Online";
            default: return "Unknown";
        }
    }
}