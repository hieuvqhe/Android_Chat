package com.example.chat.services;

import android.util.Log;
import com.example.chat.api.ApiService;
import com.example.chat.models.*;
import com.example.chat.network.ApiCallback;
import com.example.chat.network.NetworkManager;
import retrofit2.Call;
import java.util.Arrays;
import java.util.List;

public class ConversationService {
    private static final String TAG = "ConversationService";
    private ApiService apiService;
    private NetworkManager networkManager;

    public ConversationService(NetworkManager networkManager) {
        this.networkManager = networkManager;
        this.apiService = networkManager.getApiService();
    }

    // Callback interface
    public interface ConversationCallback<T> {
        void onSuccess(T response, int page, int totalPages);
        void onError(int statusCode, String message);
        void onNetworkError(String message);
    }

    /**
     * Get all conversations with pagination
     */
    public void getAllConversations(int page, int limit, ConversationCallback<List<Conversation>> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        Log.d(TAG, "Getting all conversations - Page: " + page + ", Limit: " + limit);
        Call<ApiResponse<List<Conversation>>> call = apiService.getAllConversations(authHeader, page, limit);

        call.enqueue(new retrofit2.Callback<ApiResponse<List<Conversation>>>() {
            @Override
            public void onResponse(retrofit2.Call<ApiResponse<List<Conversation>>> call, retrofit2.Response<ApiResponse<List<Conversation>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Conversation>> apiResponse = response.body();
                    List<Conversation> conversations = apiResponse.getResult();

                    Log.d(TAG, "getAllConversations SUCCESS: " + apiResponse.getMessage());
                    Log.d(TAG, "Conversations count: " + (conversations != null ? conversations.size() : "NULL"));
                    Log.d(TAG, "Pagination - Page: " + apiResponse.getPage() + ", Total Pages: " + apiResponse.getTotalPages());

                    callback.onSuccess(conversations, apiResponse.getPage(), apiResponse.getTotalPages());
                } else {
                    Log.e(TAG, "getAllConversations ERROR: " + response.code());
                    callback.onError(response.code(), "Failed to load conversations");
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ApiResponse<List<Conversation>>> call, Throwable t) {
                Log.e(TAG, "getAllConversations NETWORK ERROR: " + t.getMessage());
                callback.onNetworkError(t.getMessage());
            }
        });
    }

    /**
     * Create private conversation
     */
    public void createPrivateConversation(String friendId, ApiCallback<Conversation> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        CreatePrivateConversationRequest request = new CreatePrivateConversationRequest(friendId);
        Call<ApiResponse<Conversation>> call = apiService.createPrivateConversation(authHeader, request);
        call.enqueue(callback);
    }

    /**
     * Get conversation details
     */
    public void getConversationDetails(String conversationId, ApiCallback<Conversation> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        Call<ApiResponse<Conversation>> call = apiService.getConversationDetails(authHeader, conversationId);
        call.enqueue(callback);
    }

    /**
     * Search conversations
     */
    public void searchConversations(String searchTerm, int page, int limit, ConversationCallback<List<Conversation>> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        Call<ApiResponse<List<Conversation>>> call = apiService.searchConversations(authHeader, searchTerm, page, limit);
        call.enqueue(new retrofit2.Callback<ApiResponse<List<Conversation>>>() {
            @Override
            public void onResponse(retrofit2.Call<ApiResponse<List<Conversation>>> call, retrofit2.Response<ApiResponse<List<Conversation>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Conversation>> apiResponse = response.body();
                    List<Conversation> conversations = apiResponse.getResult();

                    Log.d(TAG, "searchConversations SUCCESS: " + apiResponse.getMessage());
                    callback.onSuccess(conversations, apiResponse.getPage(), apiResponse.getTotalPages());
                } else {
                    Log.e(TAG, "searchConversations ERROR: " + response.code());
                    callback.onError(response.code(), "Failed to search conversations");
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ApiResponse<List<Conversation>>> call, Throwable t) {
                Log.e(TAG, "searchConversations NETWORK ERROR: " + t.getMessage());
                callback.onNetworkError(t.getMessage());
            }
        });
    }

    /**
     * Delete conversation
     */
    public void deleteConversation(String conversationId, ApiCallback<Object> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        Call<ApiResponse<Object>> call = apiService.deleteConversation(authHeader, conversationId);
        call.enqueue(callback);
    }

    /**
     * Mute conversation
     */
    public void muteConversation(String conversationId, ApiCallback<Object> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        MuteConversationRequest request = new MuteConversationRequest();
        Call<ApiResponse<Object>> call = apiService.muteConversation(authHeader, conversationId, request);
        call.enqueue(callback);
    }

    /**
     * Unmute conversation
     */
    public void unmuteConversation(String conversationId, ApiCallback<Object> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        Call<ApiResponse<Object>> call = apiService.unmuteConversation(authHeader, conversationId);
        call.enqueue(callback);
    }

    /**
     * Pin conversation
     */
    public void pinConversation(String conversationId, ApiCallback<Object> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        Call<ApiResponse<Object>> call = apiService.pinConversation(authHeader, conversationId);
        call.enqueue(callback);
    }

    /**
     * Unpin conversation
     */
    public void unpinConversation(String conversationId, ApiCallback<Object> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        Call<ApiResponse<Object>> call = apiService.unpinConversation(authHeader, conversationId);
        call.enqueue(callback);
    }

    /**
     * Get private conversation with a friend
     */
    public void getPrivateConversation(String friendId, ApiCallback<Conversation> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        // Create conversation if it doesn't exist
        createPrivateConversation(friendId, new ApiCallback<Conversation>() {
            @Override
            public void onSuccess(Conversation result, String message) {
                callback.onSuccess(result, message);
            }

            @Override
            public void onError(int statusCode, String message) {
                // If conversation already exists, try to get it
                if (statusCode == 409) { // Conflict - conversation exists
                    getConversationWithFriend(friendId, callback);
                } else {
                    callback.onError(statusCode, message);
                }
            }

            @Override
            public void onNetworkError(String message) {
                callback.onNetworkError(message);
            }
        });
    }

    /**
     * Get existing conversation with friend
     */
    private void getConversationWithFriend(String friendId, ApiCallback<Conversation> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        // Get conversation by receiver ID
        Call<ApiResponse<List<Conversation>>> call = apiService.getConversation(
                authHeader,
                Arrays.asList(friendId),
                "private",
                1,
                1
        );

        call.enqueue(new retrofit2.Callback<ApiResponse<List<Conversation>>>() {
            @Override
            public void onResponse(retrofit2.Call<ApiResponse<List<Conversation>>> call, retrofit2.Response<ApiResponse<List<Conversation>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Conversation>> apiResponse = response.body();
                    List<Conversation> conversations = apiResponse.getResult();

                    if (conversations != null && !conversations.isEmpty()) {
                        callback.onSuccess(conversations.get(0), apiResponse.getMessage());
                    } else {
                        callback.onError(404, "Conversation not found");
                    }
                } else {
                    callback.onError(response.code(), "Failed to get conversation");
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ApiResponse<List<Conversation>>> call, Throwable t) {
                callback.onNetworkError(t.getMessage());
            }
        });
    }
}