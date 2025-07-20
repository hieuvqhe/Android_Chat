package com.example.chat.services;

import android.util.Log;
import com.example.chat.api.ApiService;
import com.example.chat.models.*;
import com.example.chat.network.ApiCallback;
import com.example.chat.network.NetworkManager;
import retrofit2.Call;
import java.util.List;

public class MessageService {
    private static final String TAG = "MessageService";
    private ApiService apiService;
    private NetworkManager networkManager;

    public MessageService(NetworkManager networkManager) {
        this.networkManager = networkManager;
        this.apiService = networkManager.getApiService();
    }

    // Callback interface for paginated results
    public interface MessageCallback<T> {
        void onSuccess(T response, boolean hasMore);
        void onError(int statusCode, String message);
        void onNetworkError(String message);
    }

    /**
     * Send text message
     */
    public void sendMessage(String conversationId, String content, ApiCallback<Message> callback) {
        SendMessageRequest request = new SendMessageRequest(conversationId, content);
        sendMessage(request, callback);
    }

    /**
     * Send message with media
     */
    public void sendMessage(SendMessageRequest request, ApiCallback<Message> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        Log.d(TAG, "Sending message to conversation: " + request.getConversationId());
        Call<ApiResponse<Message>> call = apiService.sendMessage(authHeader, request);
        call.enqueue(callback);
    }

    /**
     * Get messages for conversation with pagination
     */
    public void getMessages(String conversationId, int page, int limit, MessageCallback<List<Message>> callback) {
        getMessages(conversationId, page, limit, null, callback);
    }

    /**
     * Get messages for conversation with pagination and before message ID
     */
    public void getMessages(String conversationId, int page, int limit, String beforeMessageId, MessageCallback<List<Message>> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        Log.d(TAG, "Getting messages - Conversation: " + conversationId + ", Page: " + page + ", Limit: " + limit);
        Call<ApiResponse<List<Message>>> call = apiService.getMessages(authHeader, conversationId, page, limit, beforeMessageId);

        call.enqueue(new retrofit2.Callback<ApiResponse<List<Message>>>() {
            @Override
            public void onResponse(retrofit2.Call<ApiResponse<List<Message>>> call, retrofit2.Response<ApiResponse<List<Message>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Message>> apiResponse = response.body();
                    List<Message> messages = apiResponse.getResult();

                    Log.d(TAG, "getMessages SUCCESS: " + apiResponse.getMessage());
                    Log.d(TAG, "Messages count: " + (messages != null ? messages.size() : "NULL"));

                    // Calculate if there are more messages
                    boolean hasMore = false;
                    if (apiResponse.getTotalPages() > 0) {
                        hasMore = apiResponse.getPage() < apiResponse.getTotalPages();
                    } else {
                        // If no pagination info, assume no more if we got less than requested
                        hasMore = messages != null && messages.size() >= limit;
                    }

                    callback.onSuccess(messages, hasMore);
                } else {
                    Log.e(TAG, "getMessages ERROR: " + response.code());
                    callback.onError(response.code(), "Failed to load messages");
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ApiResponse<List<Message>>> call, Throwable t) {
                Log.e(TAG, "getMessages NETWORK ERROR: " + t.getMessage());
                callback.onNetworkError(t.getMessage());
            }
        });
    }

    /**
     * Edit message
     */
    public void editMessage(String messageId, String newContent, ApiCallback<Message> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        EditMessageRequest request = new EditMessageRequest(newContent);
        Call<ApiResponse<Message>> call = apiService.editMessage(authHeader, messageId, request);
        call.enqueue(callback);
    }

    /**
     * Delete message
     */
    public void deleteMessage(String messageId, ApiCallback<Object> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        Call<ApiResponse<Object>> call = apiService.deleteMessage(authHeader, messageId);
        call.enqueue(callback);
    }

    /**
     * Mark messages as read
     */
    public void markMessagesAsRead(String conversationId, ApiCallback<Object> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        MarkMessageReadRequest request = new MarkMessageReadRequest(conversationId);
        Call<ApiResponse<Object>> call = apiService.markMessagesAsRead(authHeader, request);
        call.enqueue(callback);
    }

    /**
     * Search messages in conversation
     */
    public void searchMessages(String conversationId, String searchTerm, int page, int limit, MessageCallback<List<Message>> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        Call<ApiResponse<List<Message>>> call = apiService.searchMessages(authHeader, conversationId, searchTerm, page, limit);

        call.enqueue(new retrofit2.Callback<ApiResponse<List<Message>>>() {
            @Override
            public void onResponse(retrofit2.Call<ApiResponse<List<Message>>> call, retrofit2.Response<ApiResponse<List<Message>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Message>> apiResponse = response.body();
                    List<Message> messages = apiResponse.getResult();

                    Log.d(TAG, "searchMessages SUCCESS: " + apiResponse.getMessage());

                    boolean hasMore = false;
                    if (apiResponse.getTotalPages() > 0) {
                        hasMore = apiResponse.getPage() < apiResponse.getTotalPages();
                    } else {
                        hasMore = messages != null && messages.size() >= limit;
                    }

                    callback.onSuccess(messages, hasMore);
                } else {
                    Log.e(TAG, "searchMessages ERROR: " + response.code());
                    callback.onError(response.code(), "Failed to search messages");
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ApiResponse<List<Message>>> call, Throwable t) {
                Log.e(TAG, "searchMessages NETWORK ERROR: " + t.getMessage());
                callback.onNetworkError(t.getMessage());
            }
        });
    }

    /**
     * Reply to message
     */
    public void replyToMessage(String conversationId, String content, String replyToMessageId, ApiCallback<Message> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        SendMessageRequest request = new SendMessageRequest(conversationId, content);
        request.setReplyTo(replyToMessageId);

        Call<ApiResponse<Message>> call = apiService.replyToMessage(authHeader, request);
        call.enqueue(callback);
    }

    /**
     * Pin message
     */
    public void pinMessage(String messageId, ApiCallback<Object> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        // Create request body with message_id
        PinMessageRequest request = new PinMessageRequest(messageId);
        Call<ApiResponse<Object>> call = apiService.pinMessage(authHeader, request);
        call.enqueue(callback);
    }

    /**
     * Unpin message
     */
    public void unpinMessage(String messageId, ApiCallback<Object> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        PinMessageRequest request = new PinMessageRequest(messageId);
        Call<ApiResponse<Object>> call = apiService.unpinMessage(authHeader, request);
        call.enqueue(callback);
    }

    /**
     * Get pinned messages
     */
    public void getPinnedMessages(String conversationId, ApiCallback<List<Message>> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        Call<ApiResponse<List<Message>>> call = apiService.getPinnedMessages(authHeader, conversationId);
        call.enqueue(callback);
    }

    /**
     * Add reaction to message
     */
    public void addReaction(String messageId, int reactionType, ApiCallback<Object> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        MessageReactionRequest request = new MessageReactionRequest(messageId, reactionType);
        Call<ApiResponse<Object>> call = apiService.addMessageReaction(authHeader, request);
        call.enqueue(callback);
    }

    /**
     * Remove reaction from message
     */
    public void removeReaction(String messageId, ApiCallback<Object> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        Call<ApiResponse<Object>> call = apiService.removeMessageReaction(authHeader, messageId);
        call.enqueue(callback);
    }

    /**
     * Get message reactions
     */
    public void getMessageReactions(String messageId, ApiCallback<Object> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        Call<ApiResponse<Object>> call = apiService.getMessageReactions(authHeader, messageId);
        call.enqueue(callback);
    }

    // Helper request classes
    private static class PinMessageRequest {
        private String message_id;

        public PinMessageRequest(String messageId) {
            this.message_id = messageId;
        }
    }

    private static class MessageReactionRequest {
        private String message_id;
        private int reaction_type;

        public MessageReactionRequest(String messageId, int reactionType) {
            this.message_id = messageId;
            this.reaction_type = reactionType;
        }
    }
}