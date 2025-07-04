package com.example.chat.fragments;

import android.os.Bundle;
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
import com.example.chat.adapters.FriendRequestsAdapter;
import com.example.chat.models.Friend;
import com.example.chat.network.ApiCallback;
import com.example.chat.network.NetworkManager;
import com.example.chat.services.FriendshipService;
import com.example.chat.utils.PaginationHelper;
import java.util.ArrayList;
import java.util.List;

public class FriendRequestsFragment extends Fragment {
    private static final String TAG = "FriendRequestsFragment";

    // Views
    private RecyclerView recyclerViewRequests;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView textViewEmpty;
    private TextView textViewHint;

    // Data
    private List<Friend> requestsList = new ArrayList<>();
    private FriendRequestsAdapter requestsAdapter;
    private FriendshipService friendshipService;
    private PaginationHelper paginationHelper;

    // Callback
    private OnRequestActionListener requestActionListener;

    public interface OnRequestActionListener {
        void onRequestHandled();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_friend_requests, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        initFriendshipService();
        loadFriendRequests();
    }

    private void initViews(View view) {
        recyclerViewRequests = view.findViewById(R.id.recyclerViewRequests);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        progressBar = view.findViewById(R.id.progressBar);
        textViewEmpty = view.findViewById(R.id.textViewEmpty);
        textViewHint = view.findViewById(R.id.textViewHint);
    }

    private void setupRecyclerView() {
        paginationHelper = new PaginationHelper(20);

        requestsAdapter = new FriendRequestsAdapter(requestsList, new FriendRequestsAdapter.OnRequestActionListener() {
            @Override
            public void onAcceptRequest(Friend request) {
                acceptFriendRequest(request);
            }

            @Override
            public void onRejectRequest(Friend request) {
                rejectFriendRequest(request);
            }

            @Override
            public void onViewProfile(Friend request) {
                // UPDATED: Implement view profile functionality
                if (getActivity() instanceof FriendsActivity) {
                    ((FriendsActivity) getActivity()).openFriendProfile(request);
                }
            }
        });

        recyclerViewRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewRequests.setAdapter(requestsAdapter);

        // Setup pagination
        recyclerViewRequests.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && paginationHelper.canLoadMore()) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 3) {
                        loadMoreRequests();
                    }
                }
            }
        });

        // Setup swipe refresh
        swipeRefreshLayout.setOnRefreshListener(() -> {
            refreshRequests();
        });

        swipeRefreshLayout.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorAccent
        );
    }

    private void initFriendshipService() {
        NetworkManager networkManager = NetworkManager.getInstance(getContext());
        friendshipService = new FriendshipService(networkManager);
    }

    /**
     * Load friend requests
     */
    private void loadFriendRequests() {
        if (paginationHelper.isLoading()) return;

        showLoading(true);
        paginationHelper.setLoading(true);

        // FIXED: Updated to match new FriendshipCallback signature with 3 parameters
        friendshipService.getFriendRequests(paginationHelper.getCurrentPage(), paginationHelper.getPageSize(),
                new FriendshipService.FriendshipCallback<List<Friend>>() {
                    @Override
                    public void onSuccess(List<Friend> friends, int page, int totalPages) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                showLoading(false);
                                swipeRefreshLayout.setRefreshing(false);
                                paginationHelper.setLoading(false);

                                if (friends != null) {
                                    if (paginationHelper.getCurrentPage() == 1) {
                                        requestsList.clear();
                                    }
                                    requestsList.addAll(friends);
                                    requestsAdapter.notifyDataSetChanged();

                                    paginationHelper.updatePagination(totalPages);

                                    updateEmptyState();

                                    Log.d(TAG, "Loaded " + friends.size() + " friend requests. Page " +
                                            page + "/" + totalPages);
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
     * Load more requests
     */
    private void loadMoreRequests() {
        paginationHelper.incrementPage();
        loadFriendRequests();
    }

    /**
     * Refresh requests
     */
    private void refreshRequests() {
        paginationHelper.reset();
        loadFriendRequests();
    }

    /**
     * Accept friend request
     */
    private void acceptFriendRequest(Friend request) {
        friendshipService.acceptFriendRequest(request.getId(), new ApiCallback<Friend>() {
            @Override
            public void onSuccess(Friend result, String message) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Friend request accepted!", Toast.LENGTH_SHORT).show();

                        // Remove request from list
                        requestsList.remove(request);
                        requestsAdapter.notifyDataSetChanged();
                        updateEmptyState();

                        // Notify listener
                        if (requestActionListener != null) {
                            requestActionListener.onRequestHandled();
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
     * Reject friend request
     */
    private void rejectFriendRequest(Friend request) {
        friendshipService.rejectFriendRequest(request.getId(), new ApiCallback<Friend>() {
            @Override
            public void onSuccess(Friend result, String message) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Friend request rejected", Toast.LENGTH_SHORT).show();

                        // Remove request from list
                        requestsList.remove(request);
                        requestsAdapter.notifyDataSetChanged();
                        updateEmptyState();

                        // Notify listener
                        if (requestActionListener != null) {
                            requestActionListener.onRequestHandled();
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
     * Show loading
     */
    private void showLoading(boolean show) {
        if (paginationHelper.getCurrentPage() == 1) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Update empty state
     */
    private void updateEmptyState() {
        boolean isEmpty = requestsList.isEmpty();
        textViewEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        textViewHint.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        recyclerViewRequests.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        if (isEmpty) {
            textViewEmpty.setText("No friend requests received");
        }
    }

    /**
     * Set listener for request actions
     */
    public void setOnRequestActionListener(OnRequestActionListener listener) {
        this.requestActionListener = listener;
    }

    /**
     * Refresh data from outside
     */
    public void refreshData() {
        refreshRequests();
    }
}