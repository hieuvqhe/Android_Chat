package com.example.chat.utils;

import com.example.chat.constants.FriendConstants;
import com.example.chat.models.Friend;
import com.example.chat.models.UserInfo;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FriendUtils {

    /**
     * Check if user is verified
     */
    public static boolean isUserVerified(UserInfo user) {
        return user != null && user.getVerify() == FriendConstants.USER_VERIFIED;
    }

    /**
     * Check if user is online
     */
    public static boolean isUserOnline(int activeStatus) {
        return activeStatus == FriendConstants.ACTIVE_STATUS_ONLINE;
    }

    /**
     * Check if friendship is accepted
     */
    public static boolean isFriendshipAccepted(Friend friend) {
        return friend != null && friend.getStatus() == FriendConstants.STATUS_ACCEPTED;
    }

    /**
     * Check if friendship is pending
     */
    public static boolean isFriendshipPending(Friend friend) {
        return friend != null && friend.getStatus() == FriendConstants.STATUS_PENDING;
    }

    /**
     * Check if friendship is rejected
     */
    public static boolean isFriendshipRejected(Friend friend) {
        return friend != null && friend.getStatus() == FriendConstants.STATUS_REJECTED;
    }

    /**
     * Get user display name (username or email if username is null)
     */
    public static String getUserDisplayName(UserInfo user) {
        if (user == null) return "Unknown User";

        if (user.getUsername() != null && !user.getUsername().trim().isEmpty()) {
            return user.getUsername();
        } else if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
            return user.getEmail();
        } else {
            return "User " + user.getId();
        }
    }

    /**
     * Get formatted date string
     */
    public static String getFormattedDate(String dateString) {
        try {
            // Assuming the date comes in ISO format from backend
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (Exception e) {
            return dateString; // Return original if parsing fails
        }
    }

    /**
     * Get user avatar URL or default
     */
    public static String getUserAvatarUrl(UserInfo user) {
        if (user != null && user.getAvatar() != null && !user.getAvatar().trim().isEmpty()) {
            return user.getAvatar();
        }
        return null; // Return null to use default avatar
    }

    /**
     * Get user bio or default message
     */
    public static String getUserBio(UserInfo user) {
        if (user != null && user.getBio() != null && !user.getBio().trim().isEmpty()) {
            return user.getBio();
        }
        return "No bio available";
    }

    /**
     * Get the actual friend user from a Friend object
     * (Since Friend can contain either friend_info or sender_info)
     */
    public static UserInfo getFriendUser(Friend friend) {
        if (friend == null) return null;

        // If friend_info exists, return it (this is usually the friend's info)
        if (friend.getFriendInfo() != null) {
            return friend.getFriendInfo();
        }

        // If sender_info exists, return it (this is usually in friend requests)
        if (friend.getSenderInfo() != null) {
            return friend.getSenderInfo();
        }

        return null;
    }

    /**
     * Check if user can send friend request to another user
     */
    public static boolean canSendFriendRequest(UserInfo user) {
        return user != null && isUserVerified(user);
    }

    /**
     * Get status color resource ID (you need to define these colors in colors.xml)
     */
    public static int getStatusColorRes(int status) {
        switch (status) {
            case FriendConstants.STATUS_PENDING:
                return android.R.color.holo_orange_light;
            case FriendConstants.STATUS_ACCEPTED:
                return android.R.color.holo_green_light;
            case FriendConstants.STATUS_REJECTED:
                return android.R.color.holo_red_light;
            default:
                return android.R.color.darker_gray;
        }
    }

    /**
     * Validate search query
     */
    public static boolean isValidSearchQuery(String query) {
        return query != null && query.trim().length() >= 2; // Minimum 2 characters
    }

    /**
     * Get default page size for pagination
     */
    public static int getDefaultPageSize() {
        return FriendConstants.DEFAULT_PAGE_SIZE;
    }

    /**
     * Check if more pages are available
     */
    public static boolean hasMorePages(int currentPage, int totalPages) {
        return currentPage < totalPages;
    }
}