package com.example.chat.services;

import com.example.chat.api.ApiService;
import com.example.chat.models.*;
import com.example.chat.network.ApiCallback;
import com.example.chat.network.NetworkManager;
import retrofit2.Call;

public class FriendshipService {
    private ApiService apiService;
    private NetworkManager networkManager;

    public FriendshipService(NetworkManager networkManager) {
        this.networkManager = networkManager;
        this.apiService = networkManager.getApiService();
    }

    // ==================== FRIEND MANAGEMENT METHODS ====================

    /**
     * Send friend request to a user
     */
    public void addFriend(String friendId, ApiCallback<Friend> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Not authenticated");
            return;
        }

        AddFriendRequest request = new AddFriendRequest(friendId);
        Call<ApiResponse<Friend>> call = apiService.addFriend(authHeader, request);
        call.enqueue(callback);
    }

    /**
     * Remove friend (unfriend)
     */
    public void unfriend(String friendshipId, ApiCallback<Object> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Not authenticated");
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
            callback.onError(401, "Not authenticated");
            return;
        }

        Call<UserListResponse> call = apiService.getFriendshipSuggestions(authHeader, page, limit);
        call.enqueue(new retrofit2.Callback<UserListResponse>() {
            @Override
            public void onResponse(Call<UserListResponse> call, retrofit2.Response<UserListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(response.code(), "Failed to get friendship suggestions");
                }
            }

            @Override
            public void onFailure(Call<UserListResponse> call, Throwable t) {
                callback.onNetworkError("Network error: " + t.getMessage());
            }
        });
    }

    /**
     * Get all friends
     */
    public void getAllFriends(int page, int limit, FriendshipCallback<FriendListResponse> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Not authenticated");
            return;
        }

        Call<FriendListResponse> call = apiService.getAllFriends(authHeader, page, limit);
        call.enqueue(new retrofit2.Callback<FriendListResponse>() {
            @Override
            public void onResponse(Call<FriendListResponse> call, retrofit2.Response<FriendListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(response.code(), "Failed to get friends");
                }
            }

            @Override
            public void onFailure(Call<FriendListResponse> call, Throwable t) {
                callback.onNetworkError("Network error: " + t.getMessage());
            }
        });
    }

    /**
     * Get pending friend requests
     */
    public void getFriendRequests(int page, int limit, FriendshipCallback<FriendListResponse> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Not authenticated");
            return;
        }

        Call<FriendListResponse> call = apiService.getFriendRequests(authHeader, page, limit);
        call.enqueue(new retrofit2.Callback<FriendListResponse>() {
            @Override
            public void onResponse(Call<FriendListResponse> call, retrofit2.Response<FriendListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(response.code(), "Failed to get friend requests");
                }
            }

            @Override
            public void onFailure(Call<FriendListResponse> call, Throwable t) {
                callback.onNetworkError("Network error: " + t.getMessage());
            }
        });
    }

    /**
     * Accept friend request
     */
    public void acceptFriendRequest(String friendshipId, ApiCallback<Friend> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Not authenticated");
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
            callback.onError(401, "Not authenticated");
            return;
        }

        Call<ApiResponse<Friend>> call = apiService.rejectFriendRequest(authHeader, friendshipId);
        call.enqueue(callback);
    }

    /**
     * Search friends
     */
    public void searchFriends(String searchQuery, int page, int limit, FriendshipCallback<FriendListResponse> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Not authenticated");
            return;
        }

        Call<FriendListResponse> call = apiService.searchFriends(authHeader, searchQuery, page, limit);
        call.enqueue(new retrofit2.Callback<FriendListResponse>() {
            @Override
            public void onResponse(Call<FriendListResponse> call, retrofit2.Response<FriendListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(response.code(), "Failed to search friends");
                }
            }

            @Override
            public void onFailure(Call<FriendListResponse> call, Throwable t) {
                callback.onNetworkError("Network error: " + t.getMessage());
            }
        });
    }

    /**
     * Cancel friend request
     */
    public void cancelFriendRequest(String requestId, ApiCallback<Object> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Not authenticated");
            return;
        }

        Call<ApiResponse<Object>> call = apiService.cancelFriendRequest(authHeader, requestId);
        call.enqueue(callback);
    }

    /**
     * Get all users in system
     */
    public void getAllUsers(int page, int limit, FriendshipCallback<UserListResponse> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Not authenticated");
            return;
        }

        Call<UserListResponse> call = apiService.getAllUsers(authHeader, page, limit);
        call.enqueue(new retrofit2.Callback<UserListResponse>() {
            @Override
            public void onResponse(Call<UserListResponse> call, retrofit2.Response<UserListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(response.code(), "Failed to get users");
                }
            }

            @Override
            public void onFailure(Call<UserListResponse> call, Throwable t) {
                callback.onNetworkError("Network error: " + t.getMessage());
            }
        });
    }

    /**
     * Search users in system
     */
    public void searchUsers(String searchQuery, int page, int limit, FriendshipCallback<UserListResponse> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Not authenticated");
            return;
        }

        Call<UserListResponse> call = apiService.searchUsers(authHeader, searchQuery, page, limit);
        call.enqueue(new retrofit2.Callback<UserListResponse>() {
            @Override
            public void onResponse(Call<UserListResponse> call, retrofit2.Response<UserListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(response.code(), "Failed to search users");
                }
            }

            @Override
            public void onFailure(Call<UserListResponse> call, Throwable t) {
                callback.onNetworkError("Network error: " + t.getMessage());
            }
        });
    }

    // ==================== CALLBACK INTERFACE ====================

    /**
     * Callback interface for friendship operations that don't use ApiResponse wrapper
     */
    public interface FriendshipCallback<T> {
        void onSuccess(T result);
        void onError(int statusCode, String message);
        void onNetworkError(String message);
    }
}