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
import com.example.chat.R;
import com.example.chat.adapters.FriendsListAdapter;
import com.example.chat.models.Friend;
import com.example.chat.models.FriendListResponse;
import com.example.chat.network.ApiCallback;
import com.example.chat.network.NetworkManager;
import com.example.chat.services.FriendshipService;
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
                viewFriendProfile(friend);
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
     * Load tất cả friends
     */
    private void loadFriends() {
        if (paginationHelper.isLoading()) return;

        showLoading(true);
        paginationHelper.setLoading(true);

        friendshipService.getAllFriends(paginationHelper.getCurrentPage(), paginationHelper.getPageSize(),
                new FriendshipService.FriendshipCallback<FriendListResponse>() {
                    @Override
                    public void onSuccess(FriendListResponse response) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                showLoading(false);
                                swipeRefreshLayout.setRefreshing(false);

                                if (response.getFriends() != null) {
                                    if (paginationHelper.getCurrentPage() == 1) {
                                        friendsList.clear();
                                    }
                                    friendsList.addAll(response.getFriends());
                                    friendsAdapter.notifyDataSetChanged();

                                    paginationHelper.updatePagination(response.getTotalPages());

                                    updateEmptyState();

                                    Log.d(TAG, "Loaded " + response.getFriends().size() + " friends. Page " +
                                            paginationHelper.getCurrentPage() + "/" + response.getTotalPages());
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
     * Search friends
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
                new FriendshipService.FriendshipCallback<FriendListResponse>() {
                    @Override
                    public void onSuccess(FriendListResponse response) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                showLoading(false);

                                if (response.getFriends() != null) {
                                    if (!loadMore) {
                                        friendsList.clear();
                                    }
                                    friendsList.addAll(response.getFriends());
                                    friendsAdapter.notifyDataSetChanged();

                                    paginationHelper.updatePagination(response.getTotalPages());
                                    if (loadMore) {
                                        paginationHelper.incrementPage();
                                    }

                                    updateEmptyState();

                                    Log.d(TAG, "Found " + response.getFriends().size() + " friends for query: " + query);
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
     * Hiển thị dialog xác nhận unfriend
     */
    private void showUnfriendDialog(Friend friend) {
        // TODO: Implement unfriend confirmation dialog
        Toast.makeText(getContext(), "Unfriend feature will be implemented", Toast.LENGTH_SHORT).show();
    }

    /**
     * Bắt đầu chat với friend
     */
    private void startChatWithFriend(Friend friend) {
        // TODO: Navigate to chat activity
        Toast.makeText(getContext(), "Starting chat with " + friend.getFriendInfo().getUsername(), Toast.LENGTH_SHORT).show();
    }

    /**
     * Xem profile của friend
     */
    private void viewFriendProfile(Friend friend) {
        // TODO: Navigate to profile activity
        Toast.makeText(getContext(), "View profile: " + friend.getFriendInfo().getUsername(), Toast.LENGTH_SHORT).show();
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