package com.example.chat.constants;

/**
 * Constants related to Friend functionality
 */
public class FriendConstants {

    // FIXED: Friend status constants (based on actual API response)
    public static final int STATUS_PENDING = 0;  // Changed from 1 to 0
    public static final int STATUS_ACCEPTED = 1; // Changed from 2 to 1
    public static final int STATUS_REJECTED = 2; // Changed from 3 to 2

    // Active status constants
    public static final int ACTIVE_STATUS_OFFLINE = 0;
    public static final int ACTIVE_STATUS_ONLINE = 1;

    // Verification status constants
    public static final int VERIFICATION_STATUS_UNVERIFIED = 0;
    public static final int VERIFICATION_STATUS_VERIFIED = 1;

    // Request types
    public static final String REQUEST_TYPE_SENT = "sent";
    public static final String REQUEST_TYPE_RECEIVED = "received";

    // Pagination defaults
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int DEFAULT_FIRST_PAGE = 1;

    // Search constants
    public static final int MIN_SEARCH_QUERY_LENGTH = 2;
    public static final int SEARCH_DEBOUNCE_DELAY_MS = 500;

    // UI constants
    public static final int BUTTON_DISABLE_DURATION_MS = 2000;
    public static final int ANIMATION_DURATION_MS = 300;

    // Error messages
    public static final String ERROR_NO_INTERNET = "No internet connection";
    public static final String ERROR_NETWORK = "Network error";
    public static final String ERROR_SERVER = "Server error";
    public static final String ERROR_UNAUTHORIZED = "Please log in first";
    public static final String ERROR_UNKNOWN = "An unknown error occurred";

    // Success messages
    public static final String SUCCESS_FRIEND_REQUEST_SENT = "Friend request sent";
    public static final String SUCCESS_FRIEND_REQUEST_ACCEPTED = "Friend request accepted";
    public static final String SUCCESS_FRIEND_REQUEST_REJECTED = "Friend request rejected";
    public static final String SUCCESS_FRIEND_REQUEST_CANCELLED = "Friend request cancelled";
    public static final String SUCCESS_UNFRIEND = "Friend removed";

    // Layout animation types
    public static final String ANIM_SLIDE_IN_RIGHT = "slide_in_right";
    public static final String ANIM_SLIDE_OUT_LEFT = "slide_out_left";
    public static final String ANIM_FADE_IN = "fade_in";
    public static final String ANIM_FADE_OUT = "fade_out";

    // Bundle keys
    public static final String BUNDLE_KEY_USER_ID = "user_id";
    public static final String BUNDLE_KEY_FRIEND_ID = "friend_id";
    public static final String BUNDLE_KEY_FRIENDSHIP_ID = "friendship_id";
    public static final String BUNDLE_KEY_USER_INFO = "user_info";

    // Intent extras
    public static final String EXTRA_USER_PROFILE = "extra_user_profile";
    public static final String EXTRA_FRIEND_INFO = "extra_friend_info";
    public static final String EXTRA_CONVERSATION_ID = "extra_conversation_id";

    // Shared preferences keys
    public static final String PREF_FRIENDS_CACHE = "friends_cache";
    public static final String PREF_LAST_SYNC_TIME = "last_sync_time";

    // Cache expiration time (in milliseconds)
    public static final long CACHE_EXPIRATION_TIME = 5 * 60 * 1000; // 5 minutes
}