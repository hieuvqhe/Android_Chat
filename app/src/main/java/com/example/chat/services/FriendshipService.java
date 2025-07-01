package com.example.chat.services;

import android.util.Log;
import com.example.chat.api.ApiService;
import com.example.chat.models.*;
import com.example.chat.network.ApiCallback;
import com.example.chat.network.NetworkManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FriendshipService {
    private static final String TAG = "FriendshipService";
    private NetworkManager networkManager;
    private ApiService apiService;

    public FriendshipService(NetworkManager networkManager) {
        this.networkManager = networkManager;
        this.apiService = networkManager.getApiService();
    }

    // Generic callback interface for friendship operations
    public interface FriendshipCallback<T> {
        void onSuccess(T response);
        void onError(int statusCode, String message);
        void onNetworkError(String message);
    }

    /**
     * Add friend - FIXED
     */
    public void addFriend(String friendId, ApiCallback<Friend> callback) {
        String accessToken = networkManager.getAuthorizationHeader();
        Log.d(TAG, "Adding friend with ID: " + friendId);
        Log.d(TAG, "Access token: " + (accessToken != null ? "Present" : "Missing"));

        if (accessToken == null) {
            Log.e(TAG, "No access token available");
            callback.onError(401, "No access token available");
            return;
        }

        AddFriendRequest request = new AddFriendRequest(friendId);
        Log.d(TAG, "Making add friend request");

        apiService.addFriend(accessToken, request).enqueue(callback);
    }

    /**
     * Add friend with UserInfo - Helper method
     */
    public void addFriend(UserInfo user, ApiCallback<Friend> callback) {
        String friendId = user.getId();
        if (friendId == null || friendId.isEmpty()) {
            Log.e(TAG, "User ID is null or empty");
            callback.onError(400, "Invalid user ID");
            return;
        }
        addFriend(friendId, callback);
    }

    /**
     * Get friendship suggestions - FIXED
     */
    public void getFriendshipSuggestions(int page, int limit, FriendshipCallback<UserListResponse> callback) {
        String accessToken = networkManager.getAuthorizationHeader();
        if (accessToken == null) {
            callback.onError(401, "No access token available");
            return;
        }

        apiService.getFriendshipSuggestions(accessToken, page, limit).enqueue(new Callback<ApiResponse<UserListResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserListResponse>> call, Response<ApiResponse<UserListResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<UserListResponse> apiResponse = response.body();
                    if (apiResponse.getResult() != null) {
                        callback.onSuccess(apiResponse.getResult());
                    } else {
                        callback.onError(response.code(), "No data received");
                    }
                } else {
                    callback.onError(response.code(), "Failed to get friendship suggestions");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserListResponse>> call, Throwable t) {
                Log.e(TAG, "Network error getting friendship suggestions", t);
                callback.onNetworkError(t.getMessage());
            }
        });
    }

    /**
     * Get all friends - FIXED
     */
    public void getAllFriends(int page, int limit, FriendshipCallback<FriendListResponse> callback) {
        String accessToken = networkManager.getAuthorizationHeader();
        if (accessToken == null) {
            callback.onError(401, "No access token available");
            return;
        }

        apiService.getAllFriends(accessToken, page, limit).enqueue(new Callback<ApiResponse<FriendListResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<FriendListResponse>> call, Response<ApiResponse<FriendListResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<FriendListResponse> apiResponse = response.body();
                    if (apiResponse.getResult() != null) {
                        callback.onSuccess(apiResponse.getResult());
                    } else {
                        callback.onError(response.code(), "No data received");
                    }
                } else {
                    callback.onError(response.code(), "Failed to get friends");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<FriendListResponse>> call, Throwable t) {
                callback.onNetworkError(t.getMessage());
            }
        });
    }

    /**
     * Get friend requests (received) - FIXED
     */
    public void getFriendRequests(int page, int limit, FriendshipCallback<FriendListResponse> callback) {
        String accessToken = networkManager.getAuthorizationHeader();
        if (accessToken == null) {
            callback.onError(401, "No access token available");
            return;
        }

        apiService.getFriendRequests(accessToken, page, limit).enqueue(new Callback<ApiResponse<FriendListResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<FriendListResponse>> call, Response<ApiResponse<FriendListResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<FriendListResponse> apiResponse = response.body();
                    if (apiResponse.getResult() != null) {
                        callback.onSuccess(apiResponse.getResult());
                    } else {
                        callback.onError(response.code(), "No data received");
                    }
                } else {
                    callback.onError(response.code(), "Failed to get friend requests");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<FriendListResponse>> call, Throwable t) {
                callback.onNetworkError(t.getMessage());
            }
        });
    }

    /**
     * Accept friend request - FIXED
     */
    public void acceptFriendRequest(String friendshipId, ApiCallback<Friend> callback) {
        String accessToken = networkManager.getAuthorizationHeader();
        if (accessToken == null) {
            callback.onError(401, "No access token available");
            return;
        }

        apiService.acceptFriendRequest(accessToken, friendshipId).enqueue(callback);
    }

    /**
     * Reject friend request - FIXED
     */
    public void rejectFriendRequest(String friendshipId, ApiCallback<Friend> callback) {
        String accessToken = networkManager.getAuthorizationHeader();
        if (accessToken == null) {
            callback.onError(401, "No access token available");
            return;
        }

        apiService.rejectFriendRequest(accessToken, friendshipId).enqueue(callback);
    }

    /**
     * Cancel friend request
     */
    public void cancelFriendRequest(String requestId, ApiCallback<Object> callback) {
        String accessToken = networkManager.getAuthorizationHeader();
        if (accessToken == null) {
            callback.onError(401, "No access token available");
            return;
        }

        apiService.cancelFriendRequest(accessToken, requestId).enqueue(callback);
    }

    /**
     * Search friends - FIXED
     */
    public void searchFriends(String query, int page, int limit, FriendshipCallback<FriendListResponse> callback) {
        String accessToken = networkManager.getAuthorizationHeader();
        if (accessToken == null) {
            callback.onError(401, "No access token available");
            return;
        }

        apiService.searchFriends(accessToken, query, page, limit).enqueue(new Callback<ApiResponse<FriendListResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<FriendListResponse>> call, Response<ApiResponse<FriendListResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<FriendListResponse> apiResponse = response.body();
                    if (apiResponse.getResult() != null) {
                        callback.onSuccess(apiResponse.getResult());
                    } else {
                        callback.onError(response.code(), "No data received");
                    }
                } else {
                    callback.onError(response.code(), "Failed to search friends");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<FriendListResponse>> call, Throwable t) {
                callback.onNetworkError(t.getMessage());
            }
        });
    }

    /**
     * Search users (for finding new friends) - FIXED
     */
    public void searchUsers(String query, int page, int limit, FriendshipCallback<UserListResponse> callback) {
        String accessToken = networkManager.getAuthorizationHeader();
        if (accessToken == null) {
            callback.onError(401, "No access token available");
            return;
        }

        apiService.searchUsers(accessToken, query, page, limit).enqueue(new Callback<ApiResponse<UserListResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserListResponse>> call, Response<ApiResponse<UserListResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<UserListResponse> apiResponse = response.body();
                    if (apiResponse.getResult() != null) {
                        callback.onSuccess(apiResponse.getResult());
                    } else {
                        callback.onError(response.code(), "No data received");
                    }
                } else {
                    callback.onError(response.code(), "Failed to search users");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserListResponse>> call, Throwable t) {
                callback.onNetworkError(t.getMessage());
            }
        });
    }

    /**
     * Get all users in system - ADDED
     */
    public void getAllUsers(int page, int limit, FriendshipCallback<UserListResponse> callback) {
        // Use getFriendshipSuggestions as getAllUsers might not exist
        getFriendshipSuggestions(page, limit, callback);
    }

    /**
     * Get sent friend requests - ADDED
     */
    public void getSentFriendRequests(int page, int limit, FriendshipCallback<FriendListResponse> callback) {
        String accessToken = networkManager.getAuthorizationHeader();
        if (accessToken == null) {
            callback.onError(401, "No access token available");
            return;
        }

        // Note: This endpoint might not exist in the API, using getFriendRequests as fallback
        // You might need to implement this endpoint in your backend
        getFriendRequests(page, limit, callback);
    }
}