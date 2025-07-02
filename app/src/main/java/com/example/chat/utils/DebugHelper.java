package com.example.chat.utils;

import android.util.Log;
import com.example.chat.models.Friend;
import com.example.chat.models.UserInfo;
import com.example.chat.models.FriendListResponse;
import com.example.chat.models.UserListResponse;

/**
 * Debug helper for troubleshooting network and parsing issues
 */
public class DebugHelper {
    private static final String TAG = "DebugHelper";

    /**
     * Debug FriendListResponse parsing
     */
    public static void debugFriendListResponse(FriendListResponse response) {
        if (response == null) {
            Log.e(TAG, "FriendListResponse is NULL");
            return;
        }

        Log.d(TAG, "=== FriendListResponse Debug ===");
        Log.d(TAG, "Total: " + response.getTotal());
        Log.d(TAG, "Page: " + response.getPage());
        Log.d(TAG, "Total Pages: " + response.getTotalPages());

        if (response.getFriends() == null) {
            Log.e(TAG, "Friends list is NULL");
            return;
        }

        Log.d(TAG, "Friends count: " + response.getFriends().size());

        for (int i = 0; i < response.getFriends().size() && i < 3; i++) {
            Friend friend = response.getFriends().get(i);
            debugFriend(friend, i);
        }
    }

    /**
     * Debug UserListResponse parsing
     */
    public static void debugUserListResponse(UserListResponse response) {
        if (response == null) {
            Log.e(TAG, "UserListResponse is NULL");
            return;
        }

        Log.d(TAG, "=== UserListResponse Debug ===");
        Log.d(TAG, "Total: " + response.getTotal());
        Log.d(TAG, "Page: " + response.getPage());
        Log.d(TAG, "Total Pages: " + response.getTotalPages());

        if (response.getUsers() == null) {
            Log.e(TAG, "Users list is NULL");
            return;
        }

        Log.d(TAG, "Users count: " + response.getUsers().size());

        for (int i = 0; i < response.getUsers().size() && i < 3; i++) {
            UserInfo user = response.getUsers().get(i);
            debugUserInfo(user, i);
        }
    }

    /**
     * Debug Friend object
     */
    public static void debugFriend(Friend friend, int index) {
        if (friend == null) {
            Log.e(TAG, "Friend[" + index + "] is NULL");
            return;
        }

        Log.d(TAG, "--- Friend[" + index + "] ---");
        Log.d(TAG, "ID: " + friend.getId());
        Log.d(TAG, "User ID: " + friend.getUserId());
        Log.d(TAG, "Friend ID: " + friend.getFriendId());
        Log.d(TAG, "Status: " + friend.getStatus());
        Log.d(TAG, "Active Status: " + friend.getActiveStatus());
        Log.d(TAG, "Created At: " + friend.getCreatedAt());

        // Debug friend info
        UserInfo friendUser = friend.getFriendUser();
        if (friendUser != null) {
            Log.d(TAG, "Friend User found via getFriendUser()");
            debugUserInfo(friendUser, -1);
        } else {
            Log.e(TAG, "Friend User is NULL");

            // Debug individual fields
            Log.d(TAG, "friend.getFriendInfo(): " + friend.getFriendInfo());
            Log.d(TAG, "friend.getFriend(): " + friend.getFriend());
            Log.d(TAG, "friend.getUser(): " + friend.getUser());
        }
    }

    /**
     * Debug UserInfo object
     */
    public static void debugUserInfo(UserInfo user, int index) {
        if (user == null) {
            Log.e(TAG, "UserInfo[" + index + "] is NULL");
            return;
        }

        String prefix = index >= 0 ? "UserInfo[" + index + "]" : "UserInfo";
        Log.d(TAG, "--- " + prefix + " ---");
        Log.d(TAG, "ID: " + user.getId());
        Log.d(TAG, "Username: " + user.getUsername());
        Log.d(TAG, "Email: " + user.getEmail());
        Log.d(TAG, "Bio: " + user.getBio());
        Log.d(TAG, "Avatar: " + user.getAvatar());
        Log.d(TAG, "Verify: " + user.getVerify());
        Log.d(TAG, "Active Status: " + user.getActiveStatus());
    }

    /**
     * Log network request details
     */
    public static void logNetworkRequest(String endpoint, String authHeader) {
        Log.d(TAG, "=== Network Request ===");
        Log.d(TAG, "Endpoint: " + endpoint);
        Log.d(TAG, "Auth Header: " + (authHeader != null ? "Bearer ***" : "NULL"));
    }

    /**
     * Log API error details
     */
    public static void logApiError(int statusCode, String message, String endpoint) {
        Log.e(TAG, "=== API Error ===");
        Log.e(TAG, "Endpoint: " + endpoint);
        Log.e(TAG, "Status Code: " + statusCode);
        Log.e(TAG, "Message: " + message);
    }

    /**
     * Log network error details
     */
    public static void logNetworkError(String message, String endpoint, Throwable throwable) {
        Log.e(TAG, "=== Network Error ===");
        Log.e(TAG, "Endpoint: " + endpoint);
        Log.e(TAG, "Message: " + message);
        if (throwable != null) {
            Log.e(TAG, "Exception: " + throwable.getClass().getSimpleName());
            Log.e(TAG, "Exception Message: " + throwable.getMessage());
        }
    }
}