package com.example.chat.utils;

import com.example.chat.models.Friend;
import com.example.chat.models.UserInfo;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class for Friend-related operations
 */
public class FriendUtils {

    /**
     * Get UserInfo of the friend from Friend object
     * @param friend Friend object
     * @return UserInfo of the friend, or null if not available
     */
    public static UserInfo getFriendUser(Friend friend) {
        if (friend == null) return null;

        // FIXED: Use the new getFriendUser() method
        return friend.getFriendUser();
    }

    /**
     * Get display name for user
     * @param user UserInfo object
     * @return Display name string
     */
    public static String getUserDisplayName(UserInfo user) {
        if (user == null) return "Unknown User";

        if (user.getUsername() != null && !user.getUsername().trim().isEmpty()) {
            return user.getUsername();
        } else if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
            return user.getEmail();
        }

        return "Unknown User";
    }

    /**
     * Get user bio or default message
     * @param user UserInfo object
     * @return Bio string or default message
     */
    public static String getUserBio(UserInfo user) {
        if (user == null) return "No bio available";

        // FIXED: Handle null bio gracefully
        String bio = user.getBio();
        if (bio != null && !bio.trim().isEmpty() && !bio.equals("null")) {
            return bio;
        }

        return "No bio available";
    }

    /**
     * Get user avatar URL
     * @param user UserInfo object
     * @return Avatar URL string or null if not available
     */
    public static String getUserAvatarUrl(UserInfo user) {
        if (user == null) return null;

        String avatar = user.getAvatar();
        // FIXED: Handle null and "null" string
        if (avatar != null && !avatar.trim().isEmpty() && !avatar.equals("null")) {
            return avatar;
        }

        return null;
    }

    /**
     * Check if user is verified
     * @param user UserInfo object
     * @return true if verified, false otherwise
     */
    public static boolean isUserVerified(UserInfo user) {
        if (user == null) return false;

        // FIXED: Handle potential issues with getVerify()
        try {
            return user.getVerify() == 1;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if user is online based on active status
     * @param activeStatus Active status integer
     * @return true if online, false otherwise
     */
    public static boolean isUserOnline(int activeStatus) {
        return activeStatus == 1;
    }

    /**
     * Format date to readable string
     * @param date Date object
     * @return Formatted date string
     */
    public static String getFormattedDate(Date date) {
        if (date == null) return "";

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            return sdf.format(date);
        } catch (Exception e) {
            // FIXED: Handle any date formatting errors
            return "";
        }
    }

    /**
     * Format date with time to readable string
     * @param date Date object
     * @return Formatted date string with time
     */
    public static String getFormattedDateTime(Date date) {
        if (date == null) return "";

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            return sdf.format(date);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Get relative time string (e.g., "2 hours ago")
     * @param date Date object
     * @return Relative time string
     */
    public static String getRelativeTime(Date date) {
        if (date == null) return "";

        try {
            long now = System.currentTimeMillis();
            long time = date.getTime();
            long diff = now - time;

            if (diff < 60000) { // Less than 1 minute
                return "Just now";
            } else if (diff < 3600000) { // Less than 1 hour
                int minutes = (int) (diff / 60000);
                return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
            } else if (diff < 86400000) { // Less than 1 day
                int hours = (int) (diff / 3600000);
                return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
            } else if (diff < 604800000) { // Less than 1 week
                int days = (int) (diff / 86400000);
                return days + " day" + (days > 1 ? "s" : "") + " ago";
            } else {
                return getFormattedDate(date);
            }
        } catch (Exception e) {
            return getFormattedDate(date);
        }
    }

    /**
     * Get friendship status text - FIXED: Updated for new status values
     * @param friend Friend object
     * @return Status text
     */
    public static String getFriendshipStatusText(Friend friend) {
        if (friend == null) return "Unknown";

        switch (friend.getStatus()) {
            case 0:
                return "Pending";
            case 1:
                return "Friends";
            case 2:
                return "Rejected";
            default:
                return "Unknown";
        }
    }

    /**
     * Check if friendship is in pending status - FIXED: Status 0 = pending
     * @param friend Friend object
     * @return true if pending, false otherwise
     */
    public static boolean isPendingFriendship(Friend friend) {
        return friend != null && friend.getStatus() == 0;
    }

    /**
     * Check if friendship is accepted - FIXED: Status 1 = accepted
     * @param friend Friend object
     * @return true if accepted, false otherwise
     */
    public static boolean isAcceptedFriendship(Friend friend) {
        return friend != null && friend.getStatus() == 1;
    }

    /**
     * Check if friendship is rejected - FIXED: Status 2 = rejected
     * @param friend Friend object
     * @return true if rejected, false otherwise
     */
    public static boolean isRejectedFriendship(Friend friend) {
        return friend != null && friend.getStatus() == 2;
    }

    /**
     * FIXED: Add null-safe email getter
     * @param user UserInfo object
     * @return Email string or empty string
     */
    public static String getUserEmail(UserInfo user) {
        if (user == null) return "";

        String email = user.getEmail();
        if (email != null && !email.trim().isEmpty() && !email.equals("null")) {
            return email;
        }

        return "";
    }
}