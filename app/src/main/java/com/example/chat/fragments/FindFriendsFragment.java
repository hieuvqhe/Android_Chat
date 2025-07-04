package com.example.chat.fragments;

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

import com.example.chat.FriendsActivity;
import com.example.chat.R;
import com.example.chat.adapters.FindUsersAdapter;
import com.example.chat.models.Friend;
import com.example.chat.models.UserInfo;
import com.example.chat.network.ApiCallback;
import com.example.chat.network.NetworkManager;
import com.example.chat.services.FriendshipService;
import com.example.chat.utils.PaginationHelper;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;

public class FindFriendsFragment extends Fragment {
    private static final String TAG = "FindFriendsFragment";

    // Views
    private TextInputEditText editTextSearch;
    private RecyclerView recyclerViewUsers;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView textViewEmpty;
    private TextView textViewHint;

    // Data
    private List<UserInfo> usersList = new ArrayList<>();
    private FindUsersAdapter usersAdapter;
    private FriendshipService friendshipService;
    private PaginationHelper paginationHelper;

    // Search
    private String currentSearchQuery = "";
    private boolean isSearchMode = false;

    // Callback
    private OnFriendRequestSentListener friendRequestSentListener;

    public interface OnFriendRequestSentListener {
        void onFriendRequestSent();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_find_friends, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupSearch();
        initFriendshipService();
        loadUsers();
    }

    private void initViews(View view) {
        editTextSearch = view.findViewById(R.id.editTextSearch);
        recyclerViewUsers = view.findViewById(R.id.recyclerViewUsers);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        progressBar = view.findViewById(R.id.progressBar);
        textViewEmpty = view.findViewById(R.id.textViewEmpty);
        textViewHint = view.findViewById(R.id.textViewHint);
    }

    private void setupRecyclerView() {
        paginationHelper = new PaginationHelper(20);

        usersAdapter = new FindUsersAdapter(usersList, new FindUsersAdapter.OnUserActionListener() {
            @Override
            public void onAddFriend(UserInfo user) {
                sendFriendRequest(user);
            }

            @Override
            public void onViewProfile(UserInfo user) {
                // UPDATED: Implement view profile functionality
                if (getActivity() instanceof FriendsActivity) {
                    ((FriendsActivity) getActivity()).openUserProfile(user);
                }
            }
        });

        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewUsers.setAdapter(usersAdapter);

        // Setup pagination
        recyclerViewUsers.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && paginationHelper.canLoadMore()) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 3) {
                        loadMoreUsers();
                    }
                }
            }
        });

        // Setup swipe refresh
        swipeRefreshLayout.setOnRefreshListener(() -> {
            refreshUsers();
        });

        swipeRefreshLayout.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorAccent
        );
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
                    searchUsers(query);
                }
            }
        });
    }

    private void initFriendshipService() {
        NetworkManager networkManager = NetworkManager.getInstance(getContext());
        friendshipService = new FriendshipService(networkManager);
    }

    /**
     * Load tất cả users - FIXED: Sử dụng List<UserInfo> callback
     */
    private void loadUsers() {
        if (paginationHelper.isLoading()) return;

        showLoading(true);
        paginationHelper.setLoading(true);

        friendshipService.getAllUsers(paginationHelper.getCurrentPage(), paginationHelper.getPageSize(),
                new FriendshipService.FriendshipCallback<List<UserInfo>>() {
                    @Override
                    public void onSuccess(List<UserInfo> users, int page, int totalPages) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                showLoading(false);
                                swipeRefreshLayout.setRefreshing(false);

                                if (users != null) {
                                    if (paginationHelper.getCurrentPage() == 1) {
                                        usersList.clear();
                                    }
                                    usersList.addAll(users);
                                    usersAdapter.notifyDataSetChanged();

                                    paginationHelper.updatePagination(totalPages);

                                    updateEmptyState();

                                    Log.d(TAG, "✅ Loaded " + users.size() + " users. Page " + page + "/" + totalPages);
                                } else {
                                    Log.w(TAG, "Users list is null");
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

                                Log.e(TAG, "❌ Error loading users: " + statusCode + " - " + message);
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
     * Load more users cho pagination
     */
    private void loadMoreUsers() {
        if (isSearchMode) {
            searchUsers(currentSearchQuery, true);
        } else {
            paginationHelper.incrementPage();
            loadUsers();
        }
    }

    /**
     * Refresh users
     */
    private void refreshUsers() {
        paginationHelper.reset();
        if (isSearchMode && !currentSearchQuery.isEmpty()) {
            searchUsers(currentSearchQuery);
        } else {
            isSearchMode = false;
            currentSearchQuery = "";
            loadUsers();
        }
    }

    /**
     * Search users - FIXED: Sử dụng List<UserInfo> callback
     */
    private void searchUsers(String query) {
        searchUsers(query, false);
    }

    private void searchUsers(String query, boolean loadMore) {
        if (query.trim().length() < 2) {
            // Nếu query quá ngắn, load tất cả users
            isSearchMode = false;
            paginationHelper.reset();
            loadUsers();
            return;
        }

        isSearchMode = true;

        if (!loadMore) {
            paginationHelper.reset();
            showLoading(true);
        }

        paginationHelper.setLoading(true);

        int page = loadMore ? paginationHelper.getNextPage() : 1;

        friendshipService.searchUsers(query, page, paginationHelper.getPageSize(),
                new FriendshipService.FriendshipCallback<List<UserInfo>>() {
                    @Override
                    public void onSuccess(List<UserInfo> users, int page, int totalPages) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                showLoading(false);

                                if (users != null) {
                                    if (!loadMore) {
                                        usersList.clear();
                                    }
                                    usersList.addAll(users);
                                    usersAdapter.notifyDataSetChanged();

                                    paginationHelper.updatePagination(totalPages);
                                    if (loadMore) {
                                        paginationHelper.incrementPage();
                                    }

                                    updateEmptyState();

                                    Log.d(TAG, "Found " + users.size() + " users for query: " + query);
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

    /**
     * Gửi friend request
     */
    private void sendFriendRequest(UserInfo user) {
        friendshipService.addFriend(user.getId(), new ApiCallback<Friend>() {
            @Override
            public void onSuccess(Friend result, String message) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Friend request sent to " + user.getUsername(), Toast.LENGTH_SHORT).show();

                        // Remove user from list after sending request
                        usersList.remove(user);
                        usersAdapter.notifyDataSetChanged();
                        updateEmptyState();

                        // Notify listener
                        if (friendRequestSentListener != null) {
                            friendRequestSentListener.onFriendRequestSent();
                        }
                    });
                }
            }

            @Override
            public void onError(int statusCode, String message) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onNetworkError(String message) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Network error: " + message, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
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
        boolean isEmpty = usersList.isEmpty();
        textViewEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        textViewHint.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        recyclerViewUsers.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        if (isEmpty) {
            if (isSearchMode) {
                textViewEmpty.setText("No users found for \"" + currentSearchQuery + "\"");
            } else {
                textViewEmpty.setText("No users available");
            }
        }
    }

    /**
     * Set listener cho friend request sent
     */
    public void setOnFriendRequestSentListener(OnFriendRequestSentListener listener) {
        this.friendRequestSentListener = listener;
    }
}