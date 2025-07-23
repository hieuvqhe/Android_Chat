package com.example.chat.services;

import android.util.Log;
import com.example.chat.api.ApiService;
import com.example.chat.models.*;
import com.example.chat.network.ApiCallback;
import com.example.chat.network.NetworkManager;
import retrofit2.Call;
import java.util.ArrayList;
import java.util.List;

public class GroupService {
    private static final String TAG = "GroupService";
    private ApiService apiService;
    private NetworkManager networkManager;

    public GroupService(NetworkManager networkManager) {
        this.networkManager = networkManager;
        this.apiService = networkManager.getApiService();
    }

    // Callback interface for paginated results
    public interface GroupCallback<T> {
        void onSuccess(T response, int page, int totalPages);
        void onError(int statusCode, String message);
        void onNetworkError(String message);
    }

    /**
     * Create group with CreateGroupRequest - NEW METHOD
     */
    public void createGroup(CreateGroupRequest request, ApiCallback<Group> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        Log.d(TAG, "Creating group: " + request.getName() + " with " +
                (request.getMemberIds() != null ? request.getMemberIds().size() : 0) + " members");

        Call<ApiResponse<Group>> call = apiService.createGroup(authHeader, request);
        call.enqueue(callback);
    }

    /**
     * Create group
     */
    public void createGroup(String name, String description, List<String> memberIds, String avatar, ApiCallback<Group> callback) {
        CreateGroupRequest request = new CreateGroupRequest(name, description, memberIds, avatar);
        createGroup(request, callback);
    }

    /**
     * Create group without avatar
     */
    public void createGroup(String name, String description, List<String> memberIds, ApiCallback<Group> callback) {
        createGroup(name, description, memberIds, null, callback);
    }

    /**
     * Get group info
     */
    public void getGroupInfo(String groupId, ApiCallback<Group> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        Call<ApiResponse<Group>> call = apiService.getGroupInfo(authHeader, groupId);
        call.enqueue(callback);
    }

    /**
     * Update group
     */
    public void updateGroup(String groupId, String name, String description, String avatar, ApiCallback<Group> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        UpdateGroupRequest request = new UpdateGroupRequest(name, description, avatar);
        Call<ApiResponse<Group>> call = apiService.updateGroup(authHeader, groupId, request);
        call.enqueue(callback);
    }

    /**
     * Get group members with pagination
     */
    public void getGroupMembers(String groupId, int page, int limit, GroupCallback<List<GroupMember>> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        Log.d(TAG, "Getting group members - Group: " + groupId + ", Page: " + page + ", Limit: " + limit);
        Call<ApiResponse<List<GroupMember>>> call = apiService.getGroupMembers(authHeader, groupId, page, limit);

        call.enqueue(new retrofit2.Callback<ApiResponse<List<GroupMember>>>() {
            @Override
            public void onResponse(retrofit2.Call<ApiResponse<List<GroupMember>>> call, retrofit2.Response<ApiResponse<List<GroupMember>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<GroupMember>> apiResponse = response.body();
                    List<GroupMember> members = apiResponse.getResult();

                    Log.d(TAG, "getGroupMembers SUCCESS: " + apiResponse.getMessage());
                    Log.d(TAG, "Members count: " + (members != null ? members.size() : "NULL"));

                    callback.onSuccess(members, apiResponse.getPage(), apiResponse.getTotalPages());
                } else {
                    Log.e(TAG, "getGroupMembers ERROR: " + response.code());
                    callback.onError(response.code(), "Failed to load group members");
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ApiResponse<List<GroupMember>>> call, Throwable t) {
                Log.e(TAG, "getGroupMembers NETWORK ERROR: " + t.getMessage());
                callback.onNetworkError(t.getMessage());
            }
        });
    }

    /**
     * Get user's groups with pagination
     */
    public void getUserGroups(int page, int limit, GroupCallback<List<Group>> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        Log.d(TAG, "Getting user groups - Page: " + page + ", Limit: " + limit);
        Call<ApiResponse<List<Group>>> call = apiService.getUserGroups(authHeader, page, limit);

        call.enqueue(new retrofit2.Callback<ApiResponse<List<Group>>>() {
            @Override
            public void onResponse(retrofit2.Call<ApiResponse<List<Group>>> call, retrofit2.Response<ApiResponse<List<Group>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Group>> apiResponse = response.body();
                    List<Group> groups = apiResponse.getResult();

                    Log.d(TAG, "getUserGroups SUCCESS: " + apiResponse.getMessage());
                    Log.d(TAG, "Groups count: " + (groups != null ? groups.size() : "NULL"));

                    callback.onSuccess(groups, apiResponse.getPage(), apiResponse.getTotalPages());
                } else {
                    Log.e(TAG, "getUserGroups ERROR: " + response.code());
                    callback.onError(response.code(), "Failed to load user groups");
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ApiResponse<List<Group>>> call, Throwable t) {
                Log.e(TAG, "getUserGroups NETWORK ERROR: " + t.getMessage());
                callback.onNetworkError(t.getMessage());
            }
        });
    }

    /**
     * Add members to group
     */
    public void addMembers(String groupId, List<String> memberIds, ApiCallback<Object> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        AddGroupMembersRequest request = new AddGroupMembersRequest(memberIds);
        Log.d(TAG, "Adding " + memberIds.size() + " members to group: " + groupId);

        Call<ApiResponse<Object>> call = apiService.addGroupMembers(authHeader, groupId, request);
        call.enqueue(callback);
    }

    /**
     * Add member by username
     */
    public void addMemberByUsername(String groupId, String username, ApiCallback<Object> callback) {
        // First search for user by username, then add by ID
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        // Search for user first
        apiService.searchUsers(authHeader, username, 1, 1).enqueue(new ApiCallback<List<UserInfo>>() {
            @Override
            public void onSuccess(List<UserInfo> result, String message) {
                if (result != null && !result.isEmpty()) {
                    UserInfo user = result.get(0);
                    if (user.getUsername().equals(username)) {
                        // Found exact match, add to group
                        List<String> memberIds = new ArrayList<>();
                        memberIds.add(user.getId());
                        addMembers(groupId, memberIds, callback);
                    } else {
                        callback.onError(404, "User not found");
                    }
                } else {
                    callback.onError(404, "User not found");
                }
            }

            @Override
            public void onError(int statusCode, String error) {
                callback.onError(statusCode, error);
            }

            @Override
            public void onNetworkError(String error) {
                callback.onNetworkError(error);
            }
        });
    }

    /**
     * Remove member from group
     */
    public void removeMember(String groupId, String memberId, ApiCallback<Object> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        Log.d(TAG, "Removing member " + memberId + " from group: " + groupId);

        Call<ApiResponse<Object>> call = apiService.removeGroupMember(authHeader, groupId, memberId);
        call.enqueue(callback);
    }

    /**
     * Leave group
     */
    public void leaveGroup(String groupId, ApiCallback<Object> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        Log.d(TAG, "Leaving group: " + groupId);
        Call<ApiResponse<Object>> call = apiService.leaveGroup(authHeader, groupId);
        call.enqueue(callback);
    }

    /**
     * Make member admin
     */
    public void makeAdmin(String groupId, String memberId, ApiCallback<Object> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        Log.d(TAG, "Making member " + memberId + " admin of group: " + groupId);
        Call<ApiResponse<Object>> call = apiService.makeGroupAdmin(authHeader, groupId, memberId);
        call.enqueue(callback);
    }

    /**
     * Remove admin role
     */
    public void removeAdmin(String groupId, String memberId, ApiCallback<Object> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }

        Log.d(TAG, "Removing admin role from member " + memberId + " in group: " + groupId);
        Call<ApiResponse<Object>> call = apiService.removeGroupAdmin(authHeader, groupId, memberId);
        call.enqueue(callback);
    }

    /**
     * Helper method to check if user has admin permissions
     */
    public static boolean hasAdminPermissions(Group group) {
        return group != null && (group.isOwner() || group.isAdmin());
    }

    /**
     * Helper method to check if user is group owner
     */
    public static boolean isGroupOwner(Group group) {
        return group != null && group.isOwner();
    }

    /**
     * Helper method to check if user can remove member
     */
    public static boolean canRemoveMember(Group group, GroupMember member) {
        if (group == null || member == null) return false;

        // Only owners and admins can remove members
        if (!hasAdminPermissions(group)) return false;

        // Owners can remove anyone except themselves
        if (group.isOwner()) {
            return !member.isOwner();
        }

        // Admins can only remove regular members
        return member.isMember();
    }

    /**
     * Helper method to check if user can make someone admin
     */
    public static boolean canMakeAdmin(Group group, GroupMember member) {
        if (group == null || member == null) return false;

        // Only owners can make admins
        return group.isOwner() && member.isMember();
    }

    /**
     * Helper method to check if user can remove admin role
     */
    public static boolean canRemoveAdmin(Group group, GroupMember member) {
        if (group == null || member == null) return false;

        // Only owners can remove admin role
        return group.isOwner() && member.isAdmin() && !member.isOwner();
    }
}