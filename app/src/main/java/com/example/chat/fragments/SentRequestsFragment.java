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
import com.example.chat.R;
import com.example.chat.adapters.SentRequestsAdapter;
import com.example.chat.models.Friend;
import com.example.chat.models.FriendListResponse;
import com.example.chat.network.ApiCallback;
import com.example.chat.network.NetworkManager;
import com.example.chat.services.FriendshipService;
import com.example.chat.utils.PaginationHelper;
import java.util.ArrayList;
import java.util.List;

public class SentRequestsFragment extends Fragment {
    private static final String TAG = "SentRequestsFragment";

    // Views
    private RecyclerView recyclerViewRequests;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView textViewEmpty;
    private TextView textViewHint;

    // Data
    private List<Friend> sentRequestsList = new ArrayList<>();
    private SentRequestsAdapter requestsAdapter; // FIX: Sử dụng SentRequestsAdapter thay vì FriendRequestsAdapter
    private FriendshipService friendshipService;
    private PaginationHelper paginationHelper;

    // Callback
    private OnSentRequestActionListener sentRequestActionListener;

    public interface OnSentRequestActionListener {
        void onSentRequestCancelled();
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
        loadSentRequests();
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

        // FIX: Sử dụng SentRequestsAdapter với interface đúng
        requestsAdapter = new SentRequestsAdapter(sentRequestsList, new SentRequestsAdapter.OnSentRequestActionListener() {
            @Override
            public void onCancelRequest(Friend request) {
                cancelSentRequest(request);
            }

            @Override
            public void onViewProfile(Friend request) {
                // TODO: Implement view profile
                Toast.makeText(getContext(), "View profile coming soon", Toast.LENGTH_SHORT).show();
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
     * Load sent friend requests
     */
    private void loadSentRequests() {
        if (paginationHelper.isLoading()) return;

        showLoading(true);
        paginationHelper.setLoading(true);

        friendshipService.getSentFriendRequests(paginationHelper.getCurrentPage(), paginationHelper.getPageSize(),
                new FriendshipService.FriendshipCallback<FriendListResponse>() {
                    @Override
                    public void onSuccess(FriendListResponse response) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                showLoading(false);
                                swipeRefreshLayout.setRefreshing(false);

                                if (response.getFriends() != null) {
                                    if (paginationHelper.getCurrentPage() == 1) {
                                        sentRequestsList.clear();
                                    }
                                    sentRequestsList.addAll(response.getFriends());
                                    requestsAdapter.notifyDataSetChanged();

                                    paginationHelper.updatePagination(response.getTotalPages());

                                    updateEmptyState();

                                    Log.d(TAG, "Loaded " + response.getFriends().size() + " sent requests. Page " +
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
                                Log.e(TAG, "Error loading sent requests: " + message);
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
                                Log.e(TAG, "Network error: " + message);
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
        loadSentRequests();
    }

    /**
     * Refresh requests
     */
    private void refreshRequests() {
        paginationHelper.reset();
        loadSentRequests();
    }

    /**
     * Cancel sent friend request
     */
    private void cancelSentRequest(Friend request) {
        friendshipService.cancelFriendRequest(request.getId(), new ApiCallback<Object>() {
            @Override
            public void onSuccess(Object result, String message) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Friend request cancelled", Toast.LENGTH_SHORT).show();

                        // Remove request from list
                        sentRequestsList.remove(request);
                        requestsAdapter.notifyDataSetChanged();
                        updateEmptyState();

                        // Notify listener
                        if (sentRequestActionListener != null) {
                            sentRequestActionListener.onSentRequestCancelled();
                        }
                    });
                }
            }

            @Override
            public void onError(int statusCode, String message) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error cancelling request: " + message);
                    });
                }
            }

            @Override
            public void onNetworkError(String message) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Network error: " + message, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Network error: " + message);
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
        boolean isEmpty = sentRequestsList.isEmpty();
        textViewEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        textViewHint.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        recyclerViewRequests.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        if (isEmpty) {
            textViewEmpty.setText("No sent friend requests");
        }
    }

    /**
     * Set listener cho sent request actions
     */
    public void setOnSentRequestActionListener(OnSentRequestActionListener listener) {
        this.sentRequestActionListener = listener;
    }

    /**
     * Refresh data from outside
     */
    public void refreshData() {
        refreshRequests();
    }
}