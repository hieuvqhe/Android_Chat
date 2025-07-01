package com.example.chat.constants;

public class FriendConstants {

    // Friend Status
    public static final int STATUS_PENDING = 1;
    public static final int STATUS_ACCEPTED = 2;
    public static final int STATUS_REJECTED = 3;

    // Active Status
    public static final int ACTIVE_STATUS_OFFLINE = 0;
    public static final int ACTIVE_STATUS_ONLINE = 1;

    // User Verification Status
    public static final int VERIFY_STATUS_UNVERIFIED = 0;
    public static final int VERIFY_STATUS_VERIFIED = 1;
    public static final int VERIFY_STATUS_BANNED = 2;

    // Pagination
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 50;

    // Request Types
    public static final String REQUEST_TYPE_RECEIVED = "received";
    public static final String REQUEST_TYPE_SENT = "sent";

    // Intent Keys
    public static final String EXTRA_USER_ID = "extra_user_id";
    public static final String EXTRA_USERNAME = "extra_username";
    public static final String EXTRA_FRIEND_ID = "extra_friend_id";
    public static final String EXTRA_REQUEST_TYPE = "extra_request_type";

    // Error Messages
    public static final String ERROR_NETWORK = "Network error. Please check your connection.";
    public static final String ERROR_INVALID_USER = "Invalid user information.";
    public static final String ERROR_FRIEND_REQUEST_EXISTS = "Friend request already sent.";
    public static final String ERROR_ALREADY_FRIENDS = "You are already friends.";
    public static final String ERROR_SELF_FRIEND_REQUEST = "You cannot send friend request to yourself.";

    // Success Messages
    public static final String SUCCESS_FRIEND_REQUEST_SENT = "Friend request sent successfully.";
    public static final String SUCCESS_FRIEND_REQUEST_ACCEPTED = "Friend request accepted.";
    public static final String SUCCESS_FRIEND_REQUEST_REJECTED = "Friend request rejected.";
    public static final String SUCCESS_FRIEND_REQUEST_CANCELLED = "Friend request cancelled.";
    public static final String SUCCESS_UNFRIEND = "Successfully unfriended.";
}