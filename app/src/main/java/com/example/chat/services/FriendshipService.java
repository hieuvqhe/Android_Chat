package com.example.chat.services;

import android.util.Log;
import com.example.chat.api.ApiService;
import com.example.chat.models.*;
import com.example.chat.network.ApiCallback;
import com.example.chat.network.NetworkManager;
import retrofit2.Call;
import java.util.List;

public class FriendshipService {
    private static final String TAG = "FriendshipService";
    private ApiService apiService;
    private NetworkManager networkManager;

    public FriendshipService(NetworkManager networkManager) {
        this.networkManager = networkManager;
        this.apiService = networkManager.getApiService();
    }

    // UPDATED: Simplified callback interface
    public interface FriendshipCallback<T> {
        void onSuccess(T response, int page, int totalPages);
        void onError(int statusCode, String message);
        void onNetworkError(String message);
    }

    /**
     * Get all friends - FIXED with proper pagination
     */
    public void getAllFriends(int page, int limit, FriendshipCallback<List<Friend>> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            Log.e(TAG, "No auth header for getAllFriends");
            callback.onError(401, "Please log in first");
            return;
        }

        Log.d(TAG, "Getting all friends - Page: " + page + ", Limit: " + limit);
        Call<ApiResponse<List<Friend>>> call = apiService.getAllFriends(authHeader, page, limit);

        // Use Retrofit's Callback directly to access full response
        call.enqueue(new retrofit2.Callback<ApiResponse<List<Friend>>>() {
            @Override
            public void onResponse(retrofit2.Call<ApiResponse<List<Friend>>> call, retrofit2.Response<ApiResponse<List<Friend>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Friend>> apiResponse = response.body();
                    List<Friend> friends = apiResponse.getResult();

                    Log.d(TAG, "getAllFriends SUCCESS: " + apiResponse.getMessage());
                    Log.d(TAG, "Friends count: " + (friends != null ? friends.size() : "NULL"));
                    Log.d(TAG, "Pagination - Page: " + apiResponse.getPage() + ", Total Pages: " + apiResponse.getTotalPages());

                    callback.onSuccess(friends, apiResponse.getPage(), apiResponse.getTotalPages());
                } else {
                    Log.e(TAG, "getAllFriends ERROR: " + response.code());
                    callback.onError(response.code(), "Failed to load friends");
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ApiResponse<List<Friend>>> call, Throwable t) {
                Log.e(TAG, "getAllFriends NETWORK ERROR: " + t.getMessage());
                callback.onNetworkError(t.getMessage());
            }
        });
    }

    /**
     * Get friend requests - FIXED with pagination
     */
    public void getFriendRequests(int page, int limit, FriendshipCallback<List<Friend>> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        Call<ApiResponse<List<Friend>>> call = apiService.getFriendRequests(authHeader, page, limit);
        call.enqueue(new retrofit2.Callback<ApiResponse<List<Friend>>>() {
            @Override
            public void onResponse(retrofit2.Call<ApiResponse<List<Friend>>> call, retrofit2.Response<ApiResponse<List<Friend>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Friend>> apiResponse = response.body();
                    List<Friend> friends = apiResponse.getResult();

                    Log.d(TAG, "getFriendRequests SUCCESS: " + apiResponse.getMessage());
                    callback.onSuccess(friends, apiResponse.getPage(), apiResponse.getTotalPages());
                } else {
                    Log.e(TAG, "getFriendRequests ERROR: " + response.code());
                    callback.onError(response.code(), "Failed to load friend requests");
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ApiResponse<List<Friend>>> call, Throwable t) {
                Log.e(TAG, "getFriendRequests NETWORK ERROR: " + t.getMessage());
                callback.onNetworkError(t.getMessage());
            }
        });
    }

    /**
     * Get sent friend requests - FIXED with pagination
     */
    public void getSentFriendRequests(int page, int limit, FriendshipCallback<List<Friend>> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        Call<ApiResponse<List<Friend>>> call = apiService.getSentFriendRequests(authHeader, page, limit);
        call.enqueue(new retrofit2.Callback<ApiResponse<List<Friend>>>() {
            @Override
            public void onResponse(retrofit2.Call<ApiResponse<List<Friend>>> call, retrofit2.Response<ApiResponse<List<Friend>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Friend>> apiResponse = response.body();
                    List<Friend> friends = apiResponse.getResult();

                    Log.d(TAG, "getSentFriendRequests SUCCESS: " + apiResponse.getMessage());
                    callback.onSuccess(friends, apiResponse.getPage(), apiResponse.getTotalPages());
                } else {
                    Log.e(TAG, "getSentFriendRequests ERROR: " + response.code());
                    callback.onError(response.code(), "Failed to load sent requests");
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ApiResponse<List<Friend>>> call, Throwable t) {
                Log.e(TAG, "getSentFriendRequests NETWORK ERROR: " + t.getMessage());
                callback.onNetworkError(t.getMessage());
            }
        });
    }

    /**
     * Get all users - FIXED with pagination
     */
    public void getAllUsers(int page, int limit, FriendshipCallback<List<UserInfo>> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        Call<ApiResponse<List<UserInfo>>> call = apiService.getAllUsers(authHeader, page, limit);
        call.enqueue(new retrofit2.Callback<ApiResponse<List<UserInfo>>>() {
            @Override
            public void onResponse(retrofit2.Call<ApiResponse<List<UserInfo>>> call, retrofit2.Response<ApiResponse<List<UserInfo>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<UserInfo>> apiResponse = response.body();
                    List<UserInfo> users = apiResponse.getResult();

                    Log.d(TAG, "getAllUsers SUCCESS: " + apiResponse.getMessage());
                    callback.onSuccess(users, apiResponse.getPage(), apiResponse.getTotalPages());
                } else {
                    Log.e(TAG, "getAllUsers ERROR: " + response.code());
                    callback.onError(response.code(), "Failed to load users");
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ApiResponse<List<UserInfo>>> call, Throwable t) {
                Log.e(TAG, "getAllUsers NETWORK ERROR: " + t.getMessage());
                callback.onNetworkError(t.getMessage());
            }
        });
    }

    /**
     * Get friendship suggestions - FIXED with pagination
     */
    public void getFriendshipSuggestions(int page, int limit, FriendshipCallback<List<UserInfo>> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        Call<ApiResponse<List<UserInfo>>> call = apiService.getFriendshipSuggestions(authHeader, page, limit);
        call.enqueue(new retrofit2.Callback<ApiResponse<List<UserInfo>>>() {
            @Override
            public void onResponse(retrofit2.Call<ApiResponse<List<UserInfo>>> call, retrofit2.Response<ApiResponse<List<UserInfo>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<UserInfo>> apiResponse = response.body();
                    List<UserInfo> users = apiResponse.getResult();

                    Log.d(TAG, "getFriendshipSuggestions SUCCESS: " + apiResponse.getMessage());
                    callback.onSuccess(users, apiResponse.getPage(), apiResponse.getTotalPages());
                } else {
                    Log.e(TAG, "getFriendshipSuggestions ERROR: " + response.code());
                    callback.onError(response.code(), "Failed to load suggestions");
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ApiResponse<List<UserInfo>>> call, Throwable t) {
                Log.e(TAG, "getFriendshipSuggestions NETWORK ERROR: " + t.getMessage());
                callback.onNetworkError(t.getMessage());
            }
        });
    }

    /**
     * Search friends - FIXED with pagination
     */
    public void searchFriends(String query, int page, int limit, FriendshipCallback<List<Friend>> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        Call<ApiResponse<List<Friend>>> call = apiService.searchFriends(authHeader, query, page, limit);
        call.enqueue(new retrofit2.Callback<ApiResponse<List<Friend>>>() {
            @Override
            public void onResponse(retrofit2.Call<ApiResponse<List<Friend>>> call, retrofit2.Response<ApiResponse<List<Friend>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Friend>> apiResponse = response.body();
                    List<Friend> friends = apiResponse.getResult();

                    Log.d(TAG, "searchFriends SUCCESS: " + apiResponse.getMessage());
                    callback.onSuccess(friends, apiResponse.getPage(), apiResponse.getTotalPages());
                } else {
                    Log.e(TAG, "searchFriends ERROR: " + response.code());
                    callback.onError(response.code(), "Failed to search friends");
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ApiResponse<List<Friend>>> call, Throwable t) {
                Log.e(TAG, "searchFriends NETWORK ERROR: " + t.getMessage());
                callback.onNetworkError(t.getMessage());
            }
        });
    }

    /**
     * Search users - FIXED with pagination
     */
    public void searchUsers(String query, int page, int limit, FriendshipCallback<List<UserInfo>> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        Call<ApiResponse<List<UserInfo>>> call = apiService.searchUsers(authHeader, query, page, limit);
        call.enqueue(new retrofit2.Callback<ApiResponse<List<UserInfo>>>() {
            @Override
            public void onResponse(retrofit2.Call<ApiResponse<List<UserInfo>>> call, retrofit2.Response<ApiResponse<List<UserInfo>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<UserInfo>> apiResponse = response.body();
                    List<UserInfo> users = apiResponse.getResult();

                    Log.d(TAG, "searchUsers SUCCESS: " + apiResponse.getMessage());
                    callback.onSuccess(users, apiResponse.getPage(), apiResponse.getTotalPages());
                } else {
                    Log.e(TAG, "searchUsers ERROR: " + response.code());
                    callback.onError(response.code(), "Failed to search users");
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ApiResponse<List<UserInfo>>> call, Throwable t) {
                Log.e(TAG, "searchUsers NETWORK ERROR: " + t.getMessage());
                callback.onNetworkError(t.getMessage());
            }
        });
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
}