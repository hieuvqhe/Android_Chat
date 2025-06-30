package com.example.chat.api;

import com.example.chat.models.*;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

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
    Call<UserListResponse> getFriendshipSuggestions(
            @Header("Authorization") String accessToken,
            @Query("page") int page,
            @Query("limit") int limit
    );

    // Get all friends
    @GET("api/v1/friend-requests/all-friends")
    Call<FriendListResponse> getAllFriends(
            @Header("Authorization") String accessToken,
            @Query("page") int page,
            @Query("limit") int limit
    );

    // Get friend requests (pending requests to accept)
    @GET("api/v1/friend-requests/get-requests-accept")
    Call<FriendListResponse> getFriendRequests(
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
    Call<FriendListResponse> searchFriends(
            @Header("Authorization") String accessToken,
            @Query("search") String searchQuery,
            @Query("page") int page,
            @Query("limit") int limit
    );

    // Cancel friend request (sent by current user)
    @DELETE("api/v1/friend-requests/cancel/{cancel_request_id}")
    Call<ApiResponse<Object>> cancelFriendRequest(
            @Header("Authorization") String accessToken,
            @Path("cancel_request_id") String cancelRequestId
    );

    // Get all users in system
    @GET("api/v1/friend-requests/all-users")
    Call<UserListResponse> getAllUsers(
            @Header("Authorization") String accessToken,
            @Query("page") int page,
            @Query("limit") int limit
    );

    // Search users in system
    @GET("api/v1/friend-requests/search-users")
    Call<UserListResponse> searchUsers(
            @Header("Authorization") String accessToken,
            @Query("search") String searchQuery,
            @Query("page") int page,
            @Query("limit") int limit
    );
}