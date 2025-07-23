package com.example.chat.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.chat.ChatActivity;
import com.example.chat.FriendsActivity;
import com.example.chat.R;
import com.example.chat.adapters.FriendsListAdapter;
import com.example.chat.models.Conversation;
import com.example.chat.models.CreatePrivateConversationRequest;
import com.example.chat.models.Friend;
import com.example.chat.models.UserInfo;
import com.example.chat.network.ApiCallback;
import com.example.chat.network.NetworkManager;
import com.example.chat.services.FriendshipService;
import com.example.chat.utils.FriendUtils;
import com.example.chat.utils.PaginationHelper;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;

public class FriendsListFragment extends Fragment {
    private static final String TAG = "FriendsListFragment";

    // Views
    private TextInputEditText editTextSearch;
    private RecyclerView recyclerViewFriends;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView textViewEmpty;
    private TextView textViewHint;

    // Data
    private List<Friend> friendsList = new ArrayList<>();
    private FriendsListAdapter friendsAdapter;
    private FriendshipService friendshipService;
    private PaginationHelper paginationHelper;

    // Search
    private String currentSearchQuery = "";
    private boolean isSearchMode = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_friends_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupSearch();
        initFriendshipService();
        loadFriends();
    }

    private void initViews(View view) {
        editTextSearch = view.findViewById(R.id.editTextSearch);
        recyclerViewFriends = view.findViewById(R.id.recyclerViewFriends);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        progressBar = view.findViewById(R.id.progressBar);
        textViewEmpty = view.findViewById(R.id.textViewEmpty);
        textViewHint = view.findViewById(R.id.textViewHint);
    }

    private void setupRecyclerView() {
        paginationHelper = new PaginationHelper(20);

        friendsAdapter = new FriendsListAdapter(friendsList, new FriendsListAdapter.OnFriendActionListener() {
            @Override
            public void onUnfriend(Friend friend) {
                showUnfriendDialog(friend);
            }

            @Override
            public void onStartChat(Friend friend) {
                startChatWithFriend(friend);
            }

            @Override
            public void onViewProfile(Friend friend) {
                // UPDATED: Implement view profile functionality
                if (getActivity() instanceof FriendsActivity) {
                    ((FriendsActivity) getActivity()).openFriendProfile(friend);
                }
            }
        });

        recyclerViewFriends.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewFriends.setAdapter(friendsAdapter);

        // Setup pagination
        recyclerViewFriends.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && paginationHelper.canLoadMore()) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 3) {
                        loadMoreFriends();
                    }
                }
            }
        });

        // Setup swipe refresh
        swipeRefreshLayout.setOnRefreshListener(() -> {
            refreshFriends();
        });

        swipeRefreshLayout.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorAccent
        );
    }

    /**
     * Hiển thị dialog xác nhận unfriend
     */
    private void showUnfriendDialog(Friend friend) {
        // UPDATED: Create confirmation dialog for unfriend
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());

        UserInfo friendUser = FriendUtils.getFriendUser(friend);
        String friendName = friendUser != null ? FriendUtils.getUserDisplayName(friendUser) : "this user";

        builder.setTitle("Unfriend " + friendName + "?")
                .setMessage("Are you sure you want to remove " + friendName + " from your friends list?")
                .setPositiveButton("Unfriend", (dialog, which) -> {
                    unfriendUser(friend);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Unfriend user
     */
    private void unfriendUser(Friend friend) {
        // TODO: Implement unfriend API call
        Toast.makeText(getContext(), "Unfriend functionality will be implemented", Toast.LENGTH_SHORT).show();
    }

    /**
     * Bắt đầu chat với friend
     */
    private void startChatWithFriend(Friend friend) {
        try {
            UserInfo friendUser = FriendUtils.getFriendUser(friend);
            if (friendUser == null || friendUser.getId() == null) {
                Toast.makeText(getContext(), "Unable to start chat: Invalid friend data", Toast.LENGTH_SHORT).show();
                return;
            }

            String friendName = FriendUtils.getUserDisplayName(friendUser);
            String friendId = friendUser.getId();

            Log.d(TAG, "Starting chat with friend: " + friendName + " (ID: " + friendId + ")");

            // Show loading
            Toast.makeText(getContext(), "Creating conversation...", Toast.LENGTH_SHORT).show();

            // Create private conversation
            createPrivateConversation(friendId, friendName);

        } catch (Exception e) {
            Log.e(TAG, "Error starting chat with friend", e);
            Toast.makeText(getContext(), "Error starting chat", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Tạo private conversation với friend
     */
    private void createPrivateConversation(String friendId, String friendName) {
        try {
            NetworkManager networkManager = NetworkManager.getInstance(getContext());
            if (networkManager == null) {
                Toast.makeText(getContext(), "Network not available", Toast.LENGTH_SHORT).show();
                return;
            }

            String authHeader = networkManager.getAuthorizationHeader();
            if (authHeader == null) {
                Toast.makeText(getContext(), "Authentication required", Toast.LENGTH_SHORT).show();
                return;
            }

            CreatePrivateConversationRequest request = new CreatePrivateConversationRequest(friendId);

            networkManager.getApiService().createPrivateConversation(authHeader, request)
                    .enqueue(new ApiCallback<Conversation>() {
                        @Override
                        public void onSuccess(Conversation conversation, String message) {
                            Log.d(TAG, "Private conversation created successfully: " + conversation.getId());

                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    navigateToChatActivity(conversation, friendName, friendId);
                                });
                            }
                        }

                        @Override
                        public void onError(int statusCode, String error) {
                            Log.e(TAG, "Failed to create private conversation: " + error);

                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    Toast.makeText(getContext(), "Failed to create conversation: " + error, Toast.LENGTH_LONG).show();
                                });
                            }
                        }

                        @Override
                        public void onNetworkError(String error) {
                            Log.e(TAG, "Network error creating conversation: " + error);

                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    Toast.makeText(getContext(), "Network error: " + error, Toast.LENGTH_LONG).show();
                                });
                            }
                        }
                    });

        } catch (Exception e) {
            Log.e(TAG, "Error creating private conversation", e);
            Toast.makeText(getContext(), "Error creating conversation", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Navigate to ChatActivity
     */
    private void navigateToChatActivity(Conversation conversation, String friendName, String friendId) {
        try {
            Intent intent = new Intent(getContext(), ChatActivity.class);
            intent.putExtra(ChatActivity.EXTRA_CONVERSATION_ID, conversation.getId());
            intent.putExtra(ChatActivity.EXTRA_CONVERSATION_NAME, friendName);
            intent.putExtra(ChatActivity.EXTRA_CONVERSATION_TYPE, "private");
            intent.putExtra(ChatActivity.EXTRA_OTHER_USER_ID, friendId);

            startActivity(intent);

            Log.d(TAG, "Navigated to ChatActivity with conversation ID: " + conversation.getId());

        } catch (Exception e) {
            Log.e(TAG, "Error navigating to ChatActivity", e);
            Toast.makeText(getContext(), "Error opening chat", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupSearch() {
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (!query.equals(currentSearchQuery)) {
                    currentSearchQuery = query;
                    searchFriends(query);
                }
            }
        });
    }

    private void initFriendshipService() {
        NetworkManager networkManager = NetworkManager.getInstance(getContext());
        friendshipService = new FriendshipService(networkManager);
    }

    /**
     * Load tất cả friends - FIXED: Sử dụng List<Friend> callback
     */
    private void loadFriends() {
        if (paginationHelper.isLoading()) return;

        showLoading(true);
        paginationHelper.setLoading(true);

        friendshipService.getAllFriends(paginationHelper.getCurrentPage(), paginationHelper.getPageSize(),
                new FriendshipService.FriendshipCallback<List<Friend>>() {
                    @Override
                    public void onSuccess(List<Friend> friends, int page, int totalPages) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                showLoading(false);
                                swipeRefreshLayout.setRefreshing(false);

                                if (friends != null) {
                                    if (paginationHelper.getCurrentPage() == 1) {
                                        friendsList.clear();
                                    }
                                    friendsList.addAll(friends);
                                    friendsAdapter.notifyDataSetChanged();

                                    paginationHelper.updatePagination(totalPages);

                                    updateEmptyState();

                                    Log.d(TAG, "✅ Loaded " + friends.size() + " friends. Page " +
                                            paginationHelper.getCurrentPage() + "/" + totalPages);
                                } else {
                                    Log.w(TAG, "Friends list is null");
                                    updateEmptyState();
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(int statusCode, String message) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                showLoading(false);
                                swipeRefreshLayout.setRefreshing(false);
                                paginationHelper.setLoading(false);

                                Log.e(TAG, "❌ Error loading friends: " + statusCode + " - " + message);
                                Toast.makeText(getContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
                                updateEmptyState();
                            });
                        }
                    }

                    @Override
                    public void onNetworkError(String message) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                showLoading(false);
                                swipeRefreshLayout.setRefreshing(false);
                                paginationHelper.setLoading(false);

                                Log.e(TAG, "🌐 Network error: " + message);
                                Toast.makeText(getContext(), "Network error: " + message, Toast.LENGTH_SHORT).show();
                                updateEmptyState();
                            });
                        }
                    }
                });
    }

    /**
     * Load more friends cho pagination
     */
    private void loadMoreFriends() {
        if (isSearchMode) {
            searchFriends(currentSearchQuery, true);
        } else {
            paginationHelper.incrementPage();
            loadFriends();
        }
    }

    /**
     * Refresh friends
     */
    private void refreshFriends() {
        paginationHelper.reset();
        if (isSearchMode && !currentSearchQuery.isEmpty()) {
            searchFriends(currentSearchQuery);
        } else {
            isSearchMode = false;
            currentSearchQuery = "";
            loadFriends();
        }
    }

    /**
     * Search friends - FIXED: Sử dụng List<Friend> callback
     */
    private void searchFriends(String query) {
        searchFriends(query, false);
    }

    private void searchFriends(String query, boolean loadMore) {
        if (query.trim().length() < 2) {
            // Nếu query quá ngắn, load tất cả friends
            isSearchMode = false;
            paginationHelper.reset();
            loadFriends();
            return;
        }

        isSearchMode = true;

        if (!loadMore) {
            paginationHelper.reset();
            showLoading(true);
        }

        paginationHelper.setLoading(true);

        int page = loadMore ? paginationHelper.getNextPage() : 1;

        friendshipService.searchFriends(query, page, paginationHelper.getPageSize(),
                new FriendshipService.FriendshipCallback<List<Friend>>() {
                    @Override
                    public void onSuccess(List<Friend> friends, int page, int totalPages) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                showLoading(false);

                                if (friends != null) {
                                    if (!loadMore) {
                                        friendsList.clear();
                                    }
                                    friendsList.addAll(friends);
                                    friendsAdapter.notifyDataSetChanged();

                                    paginationHelper.updatePagination(totalPages);
                                    if (loadMore) {
                                        paginationHelper.incrementPage();
                                    }

                                    updateEmptyState();

                                    Log.d(TAG, "Found " + friends.size() + " friends for query: " + query);
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(int statusCode, String message) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                showLoading(false);
                                paginationHelper.setLoading(false);
                                Toast.makeText(getContext(), "Search error: " + message, Toast.LENGTH_SHORT).show();
                                updateEmptyState();
                            });
                        }
                    }

                    @Override
                    public void onNetworkError(String message) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                showLoading(false);
                                paginationHelper.setLoading(false);
                                Toast.makeText(getContext(), "Network error: " + message, Toast.LENGTH_SHORT).show();
                                updateEmptyState();
                            });
                        }
                    }
                });
    }

    private void viewFriendProfile(Friend friend) {
        // TODO: Navigate to profile activity
        Toast.makeText(getContext(), "View profile: " + friend.getFriendUser().getUsername(), Toast.LENGTH_SHORT).show();
    }

    /**
     * Hiển thị loading
     */
    private void showLoading(boolean show) {
        if (paginationHelper.getCurrentPage() == 1) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Cập nhật empty state
     */
    private void updateEmptyState() {
        boolean isEmpty = friendsList.isEmpty();
        textViewEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        textViewHint.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        recyclerViewFriends.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        if (isEmpty) {
            if (isSearchMode) {
                textViewEmpty.setText("No friends found for \"" + currentSearchQuery + "\"");
            } else {
                textViewEmpty.setText("You don't have any friends yet.\nStart by finding and adding friends!");
            }
        }
    }

    /**
     * Refresh data từ bên ngoài
     */
    public void refreshData() {
        refreshFriends();
    }
}