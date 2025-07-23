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
import com.example.chat.CreateGroupActivity;
import com.example.chat.GroupActivity;
import com.example.chat.R;
import com.example.chat.adapters.GroupsAdapter;
import com.example.chat.models.Group;
import com.example.chat.network.NetworkManager;
import com.example.chat.services.GroupService;
import com.example.chat.utils.PaginationHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;

public class GroupsFragment extends Fragment {
    private static final String TAG = "GroupsFragment";

    // Views
    private RecyclerView recyclerViewGroups;
    private GroupsAdapter groupsAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView textViewEmpty;
    private View layoutEmpty;
    private TextInputEditText editTextSearch;
    private FloatingActionButton fabCreateGroup;

    // Data
    private List<Group> groupsList = new ArrayList<>();
    private List<Group> filteredGroupsList = new ArrayList<>();

    // Services
    private GroupService groupService;
    private NetworkManager networkManager;

    // Pagination
    private PaginationHelper paginationHelper;
    private boolean isLoading = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);
        
        initViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        setupSearch();
        setupFab();
        
        // Initialize services
        networkManager = NetworkManager.getInstance(getContext());
        groupService = new GroupService(networkManager);
        paginationHelper = new PaginationHelper(20); // Page size = 20
        
        // Load initial data
        loadGroups(true);
        
        return view;
    }

    private void initViews(View view) {
        recyclerViewGroups = view.findViewById(R.id.recyclerViewGroups);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        progressBar = view.findViewById(R.id.progressBar);
        textViewEmpty = view.findViewById(R.id.textViewEmpty);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        editTextSearch = view.findViewById(R.id.editTextSearch);
        fabCreateGroup = view.findViewById(R.id.fabCreateGroup);

        // Setup create first group button
        View buttonCreateFirstGroup = view.findViewById(R.id.buttonCreateFirstGroup);
        if (buttonCreateFirstGroup != null) {
            buttonCreateFirstGroup.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), CreateGroupActivity.class);
                startActivity(intent);
            });
        }
    }

    private void setupRecyclerView() {
        groupsAdapter = new GroupsAdapter(filteredGroupsList, new GroupsAdapter.OnGroupActionListener() {
            @Override
            public void onGroupClick(Group group) {
                openGroupChat(group);
            }

            @Override
            public void onGroupLongClick(Group group) {
                openGroupDetails(group);
            }

            @Override
            public void onGroupInfo(Group group) {
                openGroupDetails(group);
            }

            @Override
            public void onLeaveGroup(Group group) {
                // TODO: Implement leave group functionality
                Toast.makeText(getContext(), "Leave group: " + group.getName(), Toast.LENGTH_SHORT).show();
            }
        });

        recyclerViewGroups.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewGroups.setAdapter(groupsAdapter);

        // Add scroll listener for pagination
        recyclerViewGroups.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && !isLoading && paginationHelper.canLoadMore()) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 5) {
                        loadGroups(false);
                    }
                }
            }
        });
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            refreshGroups();
        });
    }

    private void setupSearch() {
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterGroups(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFab() {
        fabCreateGroup.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CreateGroupActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Load groups from server
     */
    private void loadGroups(boolean isRefresh) {
        if (isLoading) return;

        isLoading = true;

        if (isRefresh) {
            paginationHelper.reset();
            if (!swipeRefreshLayout.isRefreshing()) {
                progressBar.setVisibility(View.VISIBLE);
            }
        }

        int page = isRefresh ? 1 : paginationHelper.getNextPage();
        paginationHelper.setLoading(true);
        Log.d(TAG, "Loading groups - Page: " + page);

        groupService.getUserGroups(page, 20, new GroupService.GroupCallback<List<Group>>() {
            @Override
            public void onSuccess(List<Group> groups, int currentPage, int totalPages) {
                Log.d(TAG, "✅ Loaded " + groups.size() + " groups. Page " + currentPage + "/" + totalPages);

                // Update pagination helper with server response
                paginationHelper.setTotalPages(totalPages);
                paginationHelper.incrementPage();

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        handleGroupsLoaded(groups, isRefresh);
                    });
                }
            }

            @Override
            public void onError(int statusCode, String error) {
                Log.e(TAG, "❌ Failed to load groups: " + error);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        handleLoadError(error, isRefresh);
                    });
                }
            }

            @Override
            public void onNetworkError(String error) {
                Log.e(TAG, "🌐 Network error loading groups: " + error);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        handleLoadError("Network error: " + error, isRefresh);
                    });
                }
            }
        });
    }

    private void handleGroupsLoaded(List<Group> groups, boolean isRefresh) {
        isLoading = false;
        progressBar.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);

        if (isRefresh) {
            groupsList.clear();
        }

        groupsList.addAll(groups);
        paginationHelper.setLoading(false);

        // Apply current search filter
        String currentSearch = editTextSearch.getText().toString().trim();
        if (currentSearch.isEmpty()) {
            filteredGroupsList.clear();
            filteredGroupsList.addAll(groupsList);
        } else {
            filterGroups(currentSearch);
        }

        groupsAdapter.notifyDataSetChanged();
        updateEmptyState();

        Log.d(TAG, "Total groups loaded: " + groupsList.size());
    }

    private void handleLoadError(String error, boolean isRefresh) {
        isLoading = false;
        paginationHelper.setLoading(false);
        progressBar.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);

        if (isRefresh && groupsList.isEmpty()) {
            updateEmptyState();
        }

        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
    }

    private void refreshGroups() {
        Log.d(TAG, "Refreshing groups...");
        loadGroups(true);
    }

    private void filterGroups(String query) {
        filteredGroupsList.clear();

        if (query.isEmpty()) {
            filteredGroupsList.addAll(groupsList);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            for (Group group : groupsList) {
                if (matchesSearchQuery(group, lowerQuery)) {
                    filteredGroupsList.add(group);
                }
            }
        }

        groupsAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private boolean matchesSearchQuery(Group group, String query) {
        if (group.getName() != null && group.getName().toLowerCase().contains(query)) {
            return true;
        }
        if (group.getDescription() != null && group.getDescription().toLowerCase().contains(query)) {
            return true;
        }
        return false;
    }

    private void updateEmptyState() {
        boolean isEmpty = filteredGroupsList.isEmpty();
        layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerViewGroups.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        if (isEmpty) {
            String searchQuery = editTextSearch.getText().toString().trim();
            if (searchQuery.isEmpty()) {
                textViewEmpty.setText(R.string.no_groups_yet);
            } else {
                textViewEmpty.setText("No groups found for \"" + searchQuery + "\"");
            }
        }
    }

    /**
     * Open group chat
     */
    private void openGroupChat(Group group) {
        try {
            Intent intent = new Intent(getContext(), ChatActivity.class);
            intent.putExtra(ChatActivity.EXTRA_CONVERSATION_ID, group.getConversationId());
            intent.putExtra(ChatActivity.EXTRA_CONVERSATION_NAME, group.getName());
            intent.putExtra(ChatActivity.EXTRA_CONVERSATION_TYPE, "group");
            intent.putExtra("group_id", group.getId());

            startActivity(intent);

            Log.d(TAG, "Opened group chat: " + group.getName());

        } catch (Exception e) {
            Log.e(TAG, "Error opening group chat", e);
            Toast.makeText(getContext(), "Error opening group chat", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Open group details/settings
     */
    private void openGroupDetails(Group group) {
        try {
            Intent intent = new Intent(getContext(), GroupActivity.class);
            intent.putExtra("group_id", group.getId());
            intent.putExtra("group_name", group.getName());

            startActivity(intent);

            Log.d(TAG, "Opened group details: " + group.getName());

        } catch (Exception e) {
            Log.e(TAG, "Error opening group details", e);
            Toast.makeText(getContext(), "Error opening group details", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh groups when returning to fragment
        refreshGroups();
    }
}
