package com.example.chat.services;

import android.util.Log;
import com.example.chat.api.ApiService;
import com.example.chat.models.*;
import com.example.chat.network.ApiCallback;
import com.example.chat.network.NetworkManager;
import retrofit2.Call;

public class FriendshipService {
    private static final String TAG = "FriendshipService";
    private ApiService apiService;
    private NetworkManager networkManager;

    public FriendshipService(NetworkManager networkManager) {
        this.networkManager = networkManager;
        this.apiService = networkManager.getApiService();
    }

    public interface FriendshipCallback<T> {
        void onSuccess(T response);
        void onError(int statusCode, String message);
        void onNetworkError(String message);
    }

    /**
     * Add friend
     */
    public void addFriend(String friendId, ApiCallback<Friend> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        AddFriendRequest request = new AddFriendRequest(friendId);
        Call<ApiResponse<Friend>> call = apiService.addFriend(authHeader, request);
        call.enqueue(callback);
    }

    /**
     * Unfriend
     */
    public void unfriend(String friendshipId, ApiCallback<Object> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        Call<ApiResponse<Object>> call = apiService.unfriend(authHeader, friendshipId);
        call.enqueue(callback);
    }

    /**
     * Get friendship suggestions
     */
    public void getFriendshipSuggestions(int page, int limit, FriendshipCallback<UserListResponse> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        Call<ApiResponse<UserListResponse>> call = apiService.getFriendshipSuggestions(authHeader, page, limit);
        call.enqueue(new ApiCallback<UserListResponse>() {
            @Override
            public void onSuccess(UserListResponse result, String message) {
                callback.onSuccess(result);
            }

            @Override
            public void onError(int statusCode, String message) {
                callback.onError(statusCode, message);
            }

            @Override
            public void onNetworkError(String message) {
                callback.onNetworkError(message);
            }
        });
    }

    /**
     * Get all friends
     */
    public void getAllFriends(int page, int limit, FriendshipCallback<FriendListResponse> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        Call<ApiResponse<FriendListResponse>> call = apiService.getAllFriends(authHeader, page, limit);
        call.enqueue(new ApiCallback<FriendListResponse>() {
            @Override
            public void onSuccess(FriendListResponse result, String message) {
                callback.onSuccess(result);
            }

            @Override
            public void onError(int statusCode, String message) {
                callback.onError(statusCode, message);
            }

            @Override
            public void onNetworkError(String message) {
                callback.onNetworkError(message);
            }
        });
    }

    /**
     * Get friend requests
     */
    public void getFriendRequests(int page, int limit, FriendshipCallback<FriendListResponse> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        Call<ApiResponse<FriendListResponse>> call = apiService.getFriendRequests(authHeader, page, limit);
        call.enqueue(new ApiCallback<FriendListResponse>() {
            @Override
            public void onSuccess(FriendListResponse result, String message) {
                callback.onSuccess(result);
            }

            @Override
            public void onError(int statusCode, String message) {
                callback.onError(statusCode, message);
            }

            @Override
            public void onNetworkError(String message) {
                callback.onNetworkError(message);
            }
        });
    }

    /**
     * Get sent friend requests - FIXED: Sử dụng đúng API endpoint
     */
    public void getSentFriendRequests(int page, int limit, FriendshipCallback<FriendListResponse> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        Call<ApiResponse<FriendListResponse>> call = apiService.getSentFriendRequests(authHeader, page, limit);
        call.enqueue(new ApiCallback<FriendListResponse>() {
            @Override
            public void onSuccess(FriendListResponse result, String message) {
                callback.onSuccess(result);
            }

            @Override
            public void onError(int statusCode, String message) {
                callback.onError(statusCode, message);
            }

            @Override
            public void onNetworkError(String message) {
                callback.onNetworkError(message);
            }
        });
    }

    /**
     * Accept friend request
     */
    public void acceptFriendRequest(String friendshipId, ApiCallback<Friend> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        Call<ApiResponse<Friend>> call = apiService.acceptFriendRequest(authHeader, friendshipId);
        call.enqueue(callback);
    }

    /**
     * Reject friend request
     */
    public void rejectFriendRequest(String friendshipId, ApiCallback<Friend> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        Call<ApiResponse<Friend>> call = apiService.rejectFriendRequest(authHeader, friendshipId);
        call.enqueue(callback);
    }

    /**
     * Search friends
     */
    public void searchFriends(String query, int page, int limit, FriendshipCallback<FriendListResponse> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        Call<ApiResponse<FriendListResponse>> call = apiService.searchFriends(authHeader, query, page, limit);
        call.enqueue(new ApiCallback<FriendListResponse>() {
            @Override
            public void onSuccess(FriendListResponse result, String message) {
                callback.onSuccess(result);
            }

            @Override
            public void onError(int statusCode, String message) {
                callback.onError(statusCode, message);
            }

            @Override
            public void onNetworkError(String message) {
                callback.onNetworkError(message);
            }
        });
    }

    /**
     * Cancel friend request
     */
    public void cancelFriendRequest(String requestId, ApiCallback<Object> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        Call<ApiResponse<Object>> call = apiService.cancelFriendRequest(authHeader, requestId);
        call.enqueue(callback);
    }

    /**
     * Get all users - FIXED: Sử dụng đúng API endpoint
     */
    public void getAllUsers(int page, int limit, FriendshipCallback<UserListResponse> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        Call<ApiResponse<UserListResponse>> call = apiService.getAllUsers(authHeader, page, limit);
        call.enqueue(new ApiCallback<UserListResponse>() {
            @Override
            public void onSuccess(UserListResponse result, String message) {
                callback.onSuccess(result);
            }

            @Override
            public void onError(int statusCode, String message) {
                callback.onError(statusCode, message);
            }

            @Override
            public void onNetworkError(String message) {
                callback.onNetworkError(message);
            }
        });
    }

    /**
     * Search users
     */
    public void searchUsers(String query, int page, int limit, FriendshipCallback<UserListResponse> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        Call<ApiResponse<UserListResponse>> call = apiService.searchUsers(authHeader, query, page, limit);
        call.enqueue(new ApiCallback<UserListResponse>() {
            @Override
            public void onSuccess(UserListResponse result, String message) {
                callback.onSuccess(result);
            }

            @Override
            public void onError(int statusCode, String message) {
                callback.onError(statusCode, message);
            }

            @Override
            public void onNetworkError(String message) {
                callback.onNetworkError(message);
            }
        });
    }
}