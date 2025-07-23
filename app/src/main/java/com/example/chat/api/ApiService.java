package com.example.chat.api;

import com.example.chat.models.*;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import java.util.List;

public interface ApiService {

    // ==================== USER AUTHENTICATION ENDPOINTS ====================

    // Login endpoint
    @POST("api/v1/users/login")
    Call<ApiResponse<LoginResponseData>> login(@Body LoginRequest request);

    // Register endpoint
    @POST("api/v1/users/register")
    Call<ApiResponse<RegisterResponseData>> register(@Body RegisterRequest request);

    // Forgot password endpoint
    @POST("api/v1/users/forgot-password")
    Call<ApiResponse<ForgotPasswordResponseData>> forgotPassword(@Body ForgotPasswordRequest request);

    // Email verification endpoint
    @POST("api/v1/users/verify-email")
    Call<ApiResponse<Object>> verifyEmail(@Body EmailVerificationRequest request);

    // Reset password endpoint
    @POST("api/v1/users/reset-password")
    Call<ApiResponse<Object>> resetPassword(@Body ResetPasswordRequest request);

    // Resend verification email endpoint
    @POST("api/v1/users/resend-verify-email")
    Call<ApiResponse<String>> resendVerificationEmail(@Header("Authorization") String accessToken);

    // Verify forgot password token endpoint
    @POST("api/v1/users/verify-forgot-password")
    Call<ApiResponse<User>> verifyForgotPasswordToken(@Body Object request);

    // Logout endpoint
    @POST("api/v1/users/logout")
    Call<ApiResponse<Object>> logout(@Body LogoutRequest request);

    // Get my profile endpoint
    @GET("api/v1/users/me")
    Call<ApiResponse<User>> getMyProfile(@Header("Authorization") String accessToken);

    // Update my profile endpoint
    @PUT("api/v1/users/me")
    Call<ApiResponse<User>> updateMyProfile(
            @Header("Authorization") String accessToken,
            @Body UpdateProfileRequest request
    );

    // Get user profile by username
    @GET("api/v1/users/{username}")
    Call<ApiResponse<User>> getUserProfile(
            @Header("Authorization") String accessToken,
            @Path("username") String username
    );

    // ==================== FRIEND MANAGEMENT ENDPOINTS ====================

    // Add friend endpoint
    @POST("api/v1/friend-requests/add")
    Call<ApiResponse<Friend>> addFriend(
            @Header("Authorization") String accessToken,
            @Body AddFriendRequest request
    );

    // Unfriend endpoint
    @DELETE("api/v1/friend-requests/unfriend/{friendship_id}")
    Call<ApiResponse<Object>> unfriend(
            @Header("Authorization") String accessToken,
            @Path("friendship_id") String friendshipId
    );

    // Get friendship suggestions
    @GET("api/v1/friend-requests/friendship-suggestions")
    Call<ApiResponse<List<UserInfo>>> getFriendshipSuggestions(
            @Header("Authorization") String accessToken,
            @Query("page") int page,
            @Query("limit") int limit
    );

    // Get all friends
    @GET("api/v1/friend-requests/all-friends")
    Call<ApiResponse<List<Friend>>> getAllFriends(
            @Header("Authorization") String accessToken,
            @Query("page") int page,
            @Query("limit") int limit
    );

    // Get friend requests
    @GET("api/v1/friend-requests/get-requests-accept")
    Call<ApiResponse<List<Friend>>> getFriendRequests(
            @Header("Authorization") String accessToken,
            @Query("page") int page,
            @Query("limit") int limit
    );

    // Accept friend request
    @POST("api/v1/friend-requests/accept/{friendship_id}")
    Call<ApiResponse<Friend>> acceptFriendRequest(
            @Header("Authorization") String accessToken,
            @Path("friendship_id") String friendshipId
    );

    // Reject friend request
    @DELETE("api/v1/friend-requests/reject/{friendship_id}")
    Call<ApiResponse<Friend>> rejectFriendRequest(
            @Header("Authorization") String accessToken,
            @Path("friendship_id") String friendshipId
    );

    // Search friends
    @GET("api/v1/friend-requests/search-friends")
    Call<ApiResponse<List<Friend>>> searchFriends(
            @Header("Authorization") String accessToken,
            @Query("search") String searchQuery,
            @Query("page") int page,
            @Query("limit") int limit
    );

    // Cancel friend request
    @DELETE("api/v1/friend-requests/cancel/{cancel_request_id}")
    Call<ApiResponse<Object>> cancelFriendRequest(
            @Header("Authorization") String accessToken,
            @Path("cancel_request_id") String cancelRequestId
    );

    // Get all users
    @GET("api/v1/friend-requests/all-users")
    Call<ApiResponse<List<UserInfo>>> getAllUsers(
            @Header("Authorization") String accessToken,
            @Query("page") int page,
            @Query("limit") int limit
    );

    // Search users
    @GET("api/v1/friend-requests/search-users")
    Call<ApiResponse<List<UserInfo>>> searchUsers(
            @Header("Authorization") String accessToken,
            @Query("search") String searchQuery,
            @Query("page") int page,
            @Query("limit") int limit
    );

    // Get sent friend requests
    @GET("api/v1/friend-requests/get-requests-sent")
    Call<ApiResponse<List<Friend>>> getSentFriendRequests(
            @Header("Authorization") String accessToken,
            @Query("page") int page,
            @Query("limit") int limit
    );

    // ==================== CONVERSATION ENDPOINTS ====================

    // Get all conversations
    @GET("api/v1/conversations/get_all_conversations")
    Call<ApiResponse<List<Conversation>>> getAllConversations(
            @Header("Authorization") String accessToken,
            @Query("page") int page,
            @Query("limit") int limit
    );

    // Get conversation with specific users
    @GET("api/v1/conversations/receivers")
    Call<ApiResponse<List<Conversation>>> getConversation(
            @Header("Authorization") String accessToken,
            @Query("receiver_id") List<String> receiverIds,
            @Query("type") String type,
            @Query("page") int page,
            @Query("limit") int limit
    );

    // Create private conversation
    @POST("api/v1/conversations/create-private")
    Call<ApiResponse<Conversation>> createPrivateConversation(
            @Header("Authorization") String accessToken,
            @Body CreatePrivateConversationRequest request
    );

    // Get conversation details
    @GET("api/v1/conversations/{conversation_id}")
    Call<ApiResponse<Conversation>> getConversationDetails(
            @Header("Authorization") String accessToken,
            @Path("conversation_id") String conversationId
    );

    // Delete conversation
    @DELETE("api/v1/conversations/{conversation_id}")
    Call<ApiResponse<Object>> deleteConversation(
            @Header("Authorization") String accessToken,
            @Path("conversation_id") String conversationId
    );

    // Mute conversation
    @POST("api/v1/conversations/{conversation_id}/mute")
    Call<ApiResponse<Object>> muteConversation(
            @Header("Authorization") String accessToken,
            @Path("conversation_id") String conversationId,
            @Body MuteConversationRequest request
    );

    // Unmute conversation
    @POST("api/v1/conversations/{conversation_id}/unmute")
    Call<ApiResponse<Object>> unmuteConversation(
            @Header("Authorization") String accessToken,
            @Path("conversation_id") String conversationId
    );

    // Pin conversation
    @POST("api/v1/conversations/{conversation_id}/pin")
    Call<ApiResponse<Object>> pinConversation(
            @Header("Authorization") String accessToken,
            @Path("conversation_id") String conversationId
    );

    // Unpin conversation
    @POST("api/v1/conversations/{conversation_id}/unpin")
    Call<ApiResponse<Object>> unpinConversation(
            @Header("Authorization") String accessToken,
            @Path("conversation_id") String conversationId
    );

    // Search conversations
    @GET("api/v1/conversations/search")
    Call<ApiResponse<List<Conversation>>> searchConversations(
            @Header("Authorization") String accessToken,
            @Query("search_term") String searchTerm,
            @Query("page") int page,
            @Query("limit") int limit
    );

    // ==================== MESSAGE ENDPOINTS ====================

    // Send message
    @POST("api/v1/messages/send")
    Call<ApiResponse<Message>> sendMessage(
            @Header("Authorization") String accessToken,
            @Body SendMessageRequest request
    );

    // Get messages
    @GET("api/v1/messages")
    Call<ApiResponse<MessagesResponse>> getMessages(
            @Header("Authorization") String accessToken,
            @Query("conversation_id") String conversationId,
            @Query("page") int page,
            @Query("limit") int limit,
            @Query("before_message_id") String beforeMessageId
    );

    // Edit message
    @PUT("api/v1/messages/{message_id}")
    Call<ApiResponse<Message>> editMessage(
            @Header("Authorization") String accessToken,
            @Path("message_id") String messageId,
            @Body EditMessageRequest request
    );

    // Delete message
    @DELETE("api/v1/messages/{message_id}")
    Call<ApiResponse<Object>> deleteMessage(
            @Header("Authorization") String accessToken,
            @Path("message_id") String messageId
    );

    // Mark messages as read
    @POST("api/v1/messages/mark-read")
    Call<ApiResponse<Object>> markMessagesAsRead(
            @Header("Authorization") String accessToken,
            @Body MarkMessageReadRequest request
    );

    // Search messages
    @GET("api/v1/messages/search")
    Call<ApiResponse<List<Message>>> searchMessages(
            @Header("Authorization") String accessToken,
            @Query("conversation_id") String conversationId,
            @Query("search_term") String searchTerm,
            @Query("page") int page,
            @Query("limit") int limit
    );

    // Pin message
    @POST("api/v1/messages/pin")
    Call<ApiResponse<Object>> pinMessage(
            @Header("Authorization") String accessToken,
            @Body Object request // {message_id: string}
    );

    // Unpin message
    @POST("api/v1/messages/unpin")
    Call<ApiResponse<Object>> unpinMessage(
            @Header("Authorization") String accessToken,
            @Body Object request // {message_id: string}
    );

    // Get pinned messages
    @GET("api/v1/messages/pinned")
    Call<ApiResponse<List<Message>>> getPinnedMessages(
            @Header("Authorization") String accessToken,
            @Query("conversation_id") String conversationId
    );

    // Add reaction to message
    @POST("api/v1/messages/reaction")
    Call<ApiResponse<Object>> addMessageReaction(
            @Header("Authorization") String accessToken,
            @Body Object request // {message_id: string, reaction_type: number}
    );

    // Remove reaction from message
    @DELETE("api/v1/messages/reaction/{message_id}")
    Call<ApiResponse<Object>> removeMessageReaction(
            @Header("Authorization") String accessToken,
            @Path("message_id") String messageId
    );

    // Get message reactions
    @GET("api/v1/messages/{message_id}/reactions")
    Call<ApiResponse<Object>> getMessageReactions(
            @Header("Authorization") String accessToken,
            @Path("message_id") String messageId
    );

    // Reply to message
    @POST("api/v1/messages/reply")
    Call<ApiResponse<Message>> replyToMessage(
            @Header("Authorization") String accessToken,
            @Body SendMessageRequest request
    );

    // ==================== GROUP ENDPOINTS ====================

    // Create group
    @POST("api/v1/groups/create")
    Call<ApiResponse<Group>> createGroup(
            @Header("Authorization") String accessToken,
            @Body CreateGroupRequest request
    );

    // Add members to group
    @POST("api/v1/groups/{group_id}/add-member")
    Call<ApiResponse<Object>> addGroupMembers(
            @Header("Authorization") String accessToken,
            @Path("group_id") String groupId,
            @Body AddGroupMembersRequest request
    );

    // Remove member from group
    @DELETE("api/v1/groups/{group_id}/remove-member/{member_id}")
    Call<ApiResponse<Object>> removeGroupMember(
            @Header("Authorization") String accessToken,
            @Path("group_id") String groupId,
            @Path("member_id") String memberId
    );

    // Leave group
    @POST("api/v1/groups/{group_id}/leave")
    Call<ApiResponse<Object>> leaveGroup(
            @Header("Authorization") String accessToken,
            @Path("group_id") String groupId
    );

    // Update group
    @PUT("api/v1/groups/{group_id}")
    Call<ApiResponse<Group>> updateGroup(
            @Header("Authorization") String accessToken,
            @Path("group_id") String groupId,
            @Body UpdateGroupRequest request
    );

    // Get group info
    @GET("api/v1/groups/{group_id}")
    Call<ApiResponse<Group>> getGroupInfo(
            @Header("Authorization") String accessToken,
            @Path("group_id") String groupId
    );

    // Get group members
    @GET("api/v1/groups/{group_id}/members")
    Call<ApiResponse<List<GroupMember>>> getGroupMembers(
            @Header("Authorization") String accessToken,
            @Path("group_id") String groupId,
            @Query("page") int page,
            @Query("limit") int limit
    );

    // Get user's groups
    @GET("api/v1/groups/user")
    Call<ApiResponse<List<Group>>> getUserGroups(
            @Header("Authorization") String accessToken,
            @Query("page") int page,
            @Query("limit") int limit
    );

    // Make member admin
    @POST("api/v1/groups/{group_id}/make-admin/{member_id}")
    Call<ApiResponse<Object>> makeGroupAdmin(
            @Header("Authorization") String accessToken,
            @Path("group_id") String groupId,
            @Path("member_id") String memberId
    );

    // Remove admin role
    @DELETE("api/v1/groups/{group_id}/remove-admin/{member_id}")
    Call<ApiResponse<Object>> removeGroupAdmin(
            @Header("Authorization") String accessToken,
            @Path("group_id") String groupId,
            @Path("member_id") String memberId
    );

    // ==================== NOTIFICATION ENDPOINTS ====================

    // Get notifications
    @GET("api/v1/notifications")
    Call<ApiResponse<List<Object>>> getNotifications(
            @Header("Authorization") String accessToken,
            @Query("page") int page,
            @Query("limit") int limit,
            @Query("status") String status
    );

    // Mark notification as read
    @PUT("api/v1/notifications/{notification_id}/read")
    Call<ApiResponse<Object>> markNotificationRead(
            @Header("Authorization") String accessToken,
            @Path("notification_id") String notificationId
    );

    // Mark all notifications as read
    @PUT("api/v1/notifications/mark-all-read")
    Call<ApiResponse<Object>> markAllNotificationsRead(
            @Header("Authorization") String accessToken
    );

    // Delete notification
    @DELETE("api/v1/notifications/{notification_id}")
    Call<ApiResponse<Object>> deleteNotification(
            @Header("Authorization") String accessToken,
            @Path("notification_id") String notificationId
    );

    // Get unread notifications count
    @GET("api/v1/notifications/unread-count")
    Call<ApiResponse<Object>> getUnreadNotificationsCount(
            @Header("Authorization") String accessToken
    );

    // Clear all notifications
    @DELETE("api/v1/notifications/clear-all")
    Call<ApiResponse<Object>> clearAllNotifications(
            @Header("Authorization") String accessToken
    );
}