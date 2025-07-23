package com.example.chat;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.chat.adapters.SelectFriendsAdapter;
import com.example.chat.models.CreateGroupRequest;
import com.example.chat.models.Friend;
import com.example.chat.models.Group;
import com.example.chat.network.ApiCallback;
import com.example.chat.network.NetworkManager;
import com.example.chat.services.FriendshipService;
import com.example.chat.services.GroupService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;

public class CreateGroupActivity extends AppCompatActivity {
    private static final String TAG = "CreateGroupActivity";

    // Views
    private MaterialToolbar toolbar;
    private ImageView imageViewGroupAvatar;
    private TextInputEditText editTextGroupName;
    private TextInputEditText editTextGroupDescription;
    private TextInputEditText editTextSearch;
    private RecyclerView recyclerViewFriends;
    private TextView textViewSelectedCount;
    private MaterialButton buttonCreateGroup;
    private ProgressBar progressBar;
    private TextView textViewEmpty;

    // Data
    private List<Friend> friendsList = new ArrayList<>();
    private List<Friend> selectedFriends = new ArrayList<>();
    private SelectFriendsAdapter friendsAdapter;

    // Services
    private FriendshipService friendshipService;
    private GroupService groupService;
    private NetworkManager networkManager;

    // State
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupInputValidation();
        initServices();
        loadFriends();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        imageViewGroupAvatar = findViewById(R.id.imageViewGroupAvatar);
        editTextGroupName = findViewById(R.id.editTextGroupName);
        editTextGroupDescription = findViewById(R.id.editTextGroupDescription);
        editTextSearch = findViewById(R.id.editTextSearch);
        recyclerViewFriends = findViewById(R.id.recyclerViewFriends);
        textViewSelectedCount = findViewById(R.id.textViewSelectedCount);
        buttonCreateGroup = findViewById(R.id.buttonCreateGroup);
        progressBar = findViewById(R.id.progressBar);
        textViewEmpty = findViewById(R.id.textViewEmpty);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Create Group");
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        friendsAdapter = new SelectFriendsAdapter(friendsList, new SelectFriendsAdapter.OnFriendSelectionListener() {
            @Override
            public void onFriendSelected(Friend friend, boolean isSelected) {
                if (isSelected) {
                    if (!selectedFriends.contains(friend)) {
                        selectedFriends.add(friend);
                    }
                } else {
                    selectedFriends.remove(friend);
                }
                updateSelectedCount();
                updateCreateButton();
            }

            @Override
            public void onFriendClick(Friend friend) {
                // Toggle selection
                boolean isCurrentlySelected = selectedFriends.contains(friend);
                onFriendSelected(friend, !isCurrentlySelected);
                friendsAdapter.toggleSelection(friend);
            }
        });

        recyclerViewFriends.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewFriends.setAdapter(friendsAdapter);

        // Setup search functionality
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filterFriends(s.toString().trim());
            }
        });
    }

    private void setupInputValidation() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateCreateButton();
            }
        };

        editTextGroupName.addTextChangedListener(textWatcher);

        // Setup create button click
        buttonCreateGroup.setOnClickListener(v -> createGroup());

        // Setup avatar click (for future image selection)
        imageViewGroupAvatar.setOnClickListener(v -> {
            // TODO: Implement image selection
            Toast.makeText(this, "Avatar selection coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    private void initServices() {
        networkManager = NetworkManager.getInstance(this);
        friendshipService = new FriendshipService(networkManager);
        groupService = new GroupService(networkManager);
    }

    private void loadFriends() {
        showLoading(true);

        friendshipService.getAllFriends(1, 100, new FriendshipService.FriendshipCallback<List<Friend>>() {
            @Override
            public void onSuccess(List<Friend> friends, int page, int totalPages) {
                runOnUiThread(() -> {
                    showLoading(false);
                    if (friends != null) {
                        friendsList.clear();
                        friendsList.addAll(friends);
                        friendsAdapter.notifyDataSetChanged();
                        updateEmptyState();
                    }
                    Log.d(TAG, "Loaded " + (friends != null ? friends.size() : 0) + " friends");
                });
            }

            @Override
            public void onError(int statusCode, String message) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(CreateGroupActivity.this, "Error loading friends: " + message, Toast.LENGTH_SHORT).show();
                    updateEmptyState();
                });
            }

            @Override
            public void onNetworkError(String message) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(CreateGroupActivity.this, "Network error: " + message, Toast.LENGTH_SHORT).show();
                    updateEmptyState();
                });
            }
        });
    }

    private void filterFriends(String query) {
        friendsAdapter.filter(query);
        updateEmptyState();
    }

    private void updateSelectedCount() {
        int count = selectedFriends.size();
        if (count > 0) {
            textViewSelectedCount.setText(count + " friend" + (count > 1 ? "s" : "") + " selected");
            textViewSelectedCount.setVisibility(View.VISIBLE);
        } else {
            textViewSelectedCount.setVisibility(View.GONE);
        }
    }

    private void updateCreateButton() {
        String groupName = editTextGroupName.getText().toString().trim();
        boolean hasName = !groupName.isEmpty();
        boolean hasMembers = selectedFriends.size() > 0;

        buttonCreateGroup.setEnabled(hasName && hasMembers && !isLoading);
    }

    private void createGroup() {
        String groupName = editTextGroupName.getText().toString().trim();
        String groupDescription = editTextGroupDescription.getText().toString().trim();

        if (groupName.isEmpty()) {
            editTextGroupName.setError("Group name is required");
            return;
        }

        if (selectedFriends.isEmpty()) {
            Toast.makeText(this, "Please select at least one friend", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare member IDs
        List<String> memberIds = new ArrayList<>();
        for (Friend friend : selectedFriends) {
            memberIds.add(friend.getFriendId());
        }

        // Create request
        CreateGroupRequest request = new CreateGroupRequest(groupName, groupDescription, memberIds);

        // Show loading
        isLoading = true;
        updateCreateButton();
        progressBar.setVisibility(View.VISIBLE);

        groupService.createGroup(request, new ApiCallback<Group>() {
            @Override
            public void onSuccess(Group result, String message) {
                runOnUiThread(() -> {
                    isLoading = false;
                    progressBar.setVisibility(View.GONE);

                    Toast.makeText(CreateGroupActivity.this, "Group created successfully!", Toast.LENGTH_SHORT).show();

                    // Return to previous activity
                    setResult(RESULT_OK);
                    finish();
                });
            }

            @Override
            public void onError(int statusCode, String message) {
                runOnUiThread(() -> {
                    isLoading = false;
                    progressBar.setVisibility(View.GONE);
                    updateCreateButton();

                    Toast.makeText(CreateGroupActivity.this, "Error creating group: " + message, Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onNetworkError(String message) {
                runOnUiThread(() -> {
                    isLoading = false;
                    progressBar.setVisibility(View.GONE);
                    updateCreateButton();

                    Toast.makeText(CreateGroupActivity.this, "Network error: " + message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showLoading(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            recyclerViewFriends.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            recyclerViewFriends.setVisibility(View.VISIBLE);
        }
    }

    private void updateEmptyState() {
        boolean isEmpty = friendsAdapter.getItemCount() == 0;
        textViewEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);

        if (isEmpty) {
            String searchQuery = editTextSearch.getText().toString().trim();
            if (!searchQuery.isEmpty()) {
                textViewEmpty.setText("No friends found for \"" + searchQuery + "\"");
            } else if (friendsList.isEmpty()) {
                textViewEmpty.setText("No friends available.\nAdd some friends first to create a group.");
            } else {
                textViewEmpty.setText("No friends found");
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}