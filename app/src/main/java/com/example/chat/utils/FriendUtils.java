package com.example.chat.utils;

import android.text.format.DateUtils;
import com.example.chat.models.Friend;
import com.example.chat.models.UserInfo;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FriendUtils {

    /**
     * Get formatted date string from Date object
     */
    public static String getFormattedDate(Date date) {
        if (date == null) return "";

        try {
            // Get current time
            long now = System.currentTimeMillis();
            long dateTime = date.getTime();

            // If within last 24 hours, show relative time
            if (now - dateTime < DateUtils.DAY_IN_MILLIS) {
                return DateUtils.getRelativeTimeSpanString(
                        dateTime,
                        now,
                        DateUtils.MINUTE_IN_MILLIS,
                        DateUtils.FORMAT_ABBREV_RELATIVE
                ).toString();
            }

            // If within last week, show day name
            if (now - dateTime < 7 * DateUtils.DAY_IN_MILLIS) {
                SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
                return dayFormat.format(date);
            }

            // If within current year, show month and day
            SimpleDateFormat currentYearFormat = new SimpleDateFormat("MMM d", Locale.getDefault());
            SimpleDateFormat fullFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

            // Check if same year
            SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
            String currentYear = yearFormat.format(new Date());
            String dateYear = yearFormat.format(date);

            if (currentYear.equals(dateYear)) {
                return currentYearFormat.format(date);
            } else {
                return fullFormat.format(date);
            }

        } catch (Exception e) {
            // Fallback to simple format
            SimpleDateFormat fallbackFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
            return fallbackFormat.format(date);
        }
    }

    /**
     * Get user display name
     */
    public static String getUserDisplayName(UserInfo user) {
        if (user == null) return "Unknown User";

        if (user.getUsername() != null && !user.getUsername().trim().isEmpty()) {
            return user.getUsername();
        }

        if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
            return user.getEmail();
        }

        return "Unknown User";
    }

    /**
     * Get user bio or default message
     */
    public static String getUserBio(UserInfo user) {
        if (user == null || user.getBio() == null || user.getBio().trim().isEmpty()) {
            return "No bio available";
        }
        return user.getBio();
    }

    /**
     * Get user avatar URL
     */
    public static String getUserAvatarUrl(UserInfo user) {
        if (user == null || user.getAvatar() == null || user.getAvatar().trim().isEmpty()) {
            return null;
        }
        return user.getAvatar();
    }

    /**
     * Check if user is verified
     */
    public static boolean isUserVerified(UserInfo user) {
        return user != null && user.isVerified();
    }

    /**
     * Get the correct user info from Friend object
     * This handles the case where Friend might have different user info in different fields
     */
    public static UserInfo getFriendUser(Friend friend) {
        if (friend == null) return null;

        // Try to get user info from friend field first
        if (friend.getFriend() != null) {
            return friend.getFriend();
        }

        // Try to get user info from user field
        if (friend.getUser() != null) {
            return friend.getUser();
        }

        // If no user info object, create one from available data
        // This might happen if the API doesn't populate user info
        UserInfo userInfo = new UserInfo();
        userInfo.setId(friend.getFriendId());
        return userInfo;
    }

    /**
     * Get current user info from Friend object
     */
    public static UserInfo getCurrentUser(Friend friend) {
        if (friend == null) return null;

        // Try to get current user info
        if (friend.getUser() != null) {
            return friend.getUser();
        }

        // Create minimal user info if not available
        UserInfo userInfo = new UserInfo();
        userInfo.setId(friend.getUserId());
        return userInfo;
    }

    /**
     * Format friendship status
     */
    public static String getFriendshipStatusText(Friend friend) {
        if (friend == null) return "Unknown";

        switch (friend.getStatus()) {
            case 1:
                return "Pending";
            case 2:
                return "Accepted";
            case 3:
                return "Rejected";
            default:
                return "Unknown";
        }
    }

    /**
     * Check if friendship is pending
     */
    public static boolean isFriendshipPending(Friend friend) {
        return friend != null && friend.isPending();
    }

    /**
     * Check if friendship is accepted
     */
    public static boolean isFriendshipAccepted(Friend friend) {
        return friend != null && friend.isAccepted();
    }

    /**
     * Get time ago string for friend request
     */
    public static String getTimeAgo(Date date) {
        if (date == null) return "";

        try {
            long now = System.currentTimeMillis();
            long dateTime = date.getTime();

            return DateUtils.getRelativeTimeSpanString(
                    dateTime,
                    now,
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE
            ).toString();

        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Create a user initial for avatar placeholder
     */
    public static String getUserInitial(UserInfo user) {
        if (user == null) return "?";

        String displayName = getUserDisplayName(user);
        if (displayName.length() > 0) {
            return String.valueOf(displayName.charAt(0)).toUpperCase();
        }

        return "?";
    }

    /**
     * Validate user ID
     */
    public static boolean isValidUserId(String userId) {
        return userId != null && !userId.trim().isEmpty() && userId.length() > 10;
    }

    /**
     * Validate user info
     */
    public static boolean isValidUser(UserInfo user) {
        return user != null && isValidUserId(user.getId());
    }

    /**
     * Check if user is online based on active status
     */
    public static boolean isUserOnline(int activeStatus) {
        return activeStatus == 1;
    }
}