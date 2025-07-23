package com.example.chat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.chat.adapters.GroupMembersAdapter;
import com.example.chat.models.Group;
import com.example.chat.models.GroupMember;
import com.example.chat.network.ApiCallback;
import com.example.chat.network.NetworkManager;
import com.example.chat.services.GroupService;
import com.example.chat.utils.PaginationHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class GroupActivity extends AppCompatActivity {
    private static final String TAG = "GroupActivity";

    // Intent extras
    public static final String EXTRA_GROUP_ID = "group_id";

    // Views
    private MaterialToolbar toolbar;
    private ImageView imageViewGroupAvatar;
    private TextView textViewGroupName;
    private TextView textViewGroupDescription;
    private TextView textViewMemberCount;
    private TextView textViewUserRole;
    private RecyclerView recyclerViewMembers;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView textViewEmpty;
    private FloatingActionButton fabAddMembers;
    private MaterialButton buttonLeaveGroup;

    // Data
    private String groupId;
    private Group currentGroup;
    private List<GroupMember> membersList = new ArrayList<>();
    private GroupMembersAdapter membersAdapter;
    private GroupService groupService;
    private PaginationHelper paginationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        getIntentData();
        initViews();
        setupToolbar();
        setupRecyclerView();
        initGroupService();
        loadGroupInfo();
        loadGroupMembers();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        groupId = intent.getStringExtra(EXTRA_GROUP_ID);

        if (groupId == null) {
            Toast.makeText(this, "Invalid group", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        imageViewGroupAvatar = findViewById(R.id.imageViewGroupAvatar);
        textViewGroupName = findViewById(R.id.textViewGroupName);
        textViewGroupDescription = findViewById(R.id.textViewGroupDescription);
        textViewMemberCount = findViewById(R.id.textViewMemberCount);
        textViewUserRole = findViewById(R.id.textViewUserRole);
        recyclerViewMembers = findViewById(R.id.recyclerViewMembers);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        progressBar = findViewById(R.id.progressBar);
        textViewEmpty = findViewById(R.id.textViewEmpty);
        fabAddMembers = findViewById(R.id.fabAddMembers);
        buttonLeaveGroup = findViewById(R.id.buttonLeaveGroup);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Group Info");
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        paginationHelper = new PaginationHelper(20);

        membersAdapter = new GroupMembersAdapter(membersList, new GroupMembersAdapter.OnMemberActionListener() {
            @Override
            public void onMemberClick(GroupMember member) {
                openMemberProfile(member);
            }

            @Override
            public void onMemberLongClick(GroupMember member) {
                showMemberOptions(member);
            }

            @Override
            public void onRemoveMember(GroupMember member) {
                confirmRemoveMember(member);
            }

            @Override
            public void onMakeAdmin(GroupMember member) {
                makeAdmin(member);
            }

            @Override
            public void onRemoveAdmin(GroupMember member) {
                removeAdmin(member);
            }
        });

        recyclerViewMembers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMembers.setAdapter(membersAdapter);

        // Setup pagination
        recyclerViewMembers.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && paginationHelper.canLoadMore()) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 3) {
                        loadMoreMembers();
                    }
                }
            }
        });

        // Setup swipe refresh
        swipeRefreshLayout.setOnRefreshListener(() -> {
            refreshGroup();
        });

        swipeRefreshLayout.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorAccent
        );

        // Setup FAB for adding members
        if (fabAddMembers != null) {
            fabAddMembers.setOnClickListener(v -> {
                // Temporarily allow all users for testing
                addMembers();
                // if (currentGroup != null && GroupService.hasAdminPermissions(currentGroup)) {
                //     addMembers();
                // } else {
                //     Toast.makeText(this, "Only admins can add members", Toast.LENGTH_SHORT).show();
                // }
            });
        }

        // Setup leave group button
        if (buttonLeaveGroup != null) {
            buttonLeaveGroup.setOnClickListener(v -> {
                confirmLeaveGroup();
            });
        }
    }

    private void initGroupService() {
        NetworkManager networkManager = NetworkManager.getInstance(this);
        groupService = new GroupService(networkManager);
    }

    private void loadGroupInfo() {
        groupService.getGroupInfo(groupId, new ApiCallback<Group>() {
            @Override
            public void onSuccess(Group result, String message) {
                runOnUiThread(() -> {
                    currentGroup = result;
                    updateGroupInfo(result);
                });
            }

            @Override
            public void onError(int statusCode, String message) {
                runOnUiThread(() -> {
                    Toast.makeText(GroupActivity.this, "Error loading group info: " + message, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error loading group info: " + message);
                });
            }

            @Override
            public void onNetworkError(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(GroupActivity.this, "Network error: " + message, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Network error: " + message);
                });
            }
        });
    }

    private void updateGroupInfo(Group group) {
        textViewGroupName.setText(group.getName());

        if (group.getDescription() != null && !group.getDescription().isEmpty()) {
            textViewGroupDescription.setText(group.getDescription());
            textViewGroupDescription.setVisibility(View.VISIBLE);
        } else {
            textViewGroupDescription.setVisibility(View.GONE);
        }

        textViewMemberCount.setText(group.getMemberCount() + " members");

        if (group.getUserRole() != null) {
            textViewUserRole.setText("Your role: " + group.getUserRole());
            textViewUserRole.setVisibility(View.VISIBLE);

            // Set role color
            if (group.isOwner()) {
                textViewUserRole.setTextColor(getColor(R.color.colorPrimary));
            } else if (group.isAdmin()) {
                textViewUserRole.setTextColor(getColor(R.color.success_color));
            } else {
                textViewUserRole.setTextColor(getColor(R.color.text_color_secondary));
            }
        } else {
            textViewUserRole.setVisibility(View.GONE);
        }

        // Load group avatar
        String avatarUrl = group.getDisplayAvatar();
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            RequestOptions requestOptions = new RequestOptions()
                    .placeholder(R.drawable.ic_people)
                    .error(R.drawable.ic_people)
                    .circleCrop();

            Glide.with(this)
                    .load(avatarUrl)
                    .apply(requestOptions)
                    .into(imageViewGroupAvatar);
        } else {
            imageViewGroupAvatar.setImageResource(R.drawable.ic_people);
        }

        // Update UI based on permissions
        updateUIBasedOnPermissions(group);
    }

    private void updateUIBasedOnPermissions(Group group) {
        boolean isAdmin = GroupService.hasAdminPermissions(group);
        boolean isOwner = GroupService.isGroupOwner(group);

        // Show/hide FAB based on admin permissions
        if (fabAddMembers != null) {
            // Temporarily show for all users for testing
            fabAddMembers.setVisibility(View.VISIBLE);
            // fabAddMembers.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        }

        // Show/hide leave button (owners cannot leave)
        if (buttonLeaveGroup != null) {
            buttonLeaveGroup.setVisibility(isOwner ? View.GONE : View.VISIBLE);
        }

        // Update toolbar menu
        invalidateOptionsMenu();
    }

    private void loadGroupMembers() {
        if (paginationHelper.isLoading()) return;

        showLoading(true);
        paginationHelper.setLoading(true);

        groupService.getGroupMembers(groupId, paginationHelper.getCurrentPage(), paginationHelper.getPageSize(),
                new GroupService.GroupCallback<List<GroupMember>>() {
                    @Override
                    public void onSuccess(List<GroupMember> members, int page, int totalPages) {
                        runOnUiThread(() -> {
                            showLoading(false);
                            swipeRefreshLayout.setRefreshing(false);

                            if (members != null) {
                                if (page == 1) {
                                    membersList.clear();
                                }
                                membersList.addAll(members);
                                membersAdapter.notifyDataSetChanged();

                                paginationHelper.updatePagination(totalPages);

                                updateEmptyState();

                                Log.d(TAG, "Loaded " + members.size() + " members. Page " + page + "/" + totalPages);
                            }
                        });
                    }

                    @Override
                    public void onError(int statusCode, String message) {
                        runOnUiThread(() -> {
                            showLoading(false);
                            swipeRefreshLayout.setRefreshing(false);
                            paginationHelper.setLoading(false);

                            Toast.makeText(GroupActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            updateEmptyState();
                            Log.e(TAG, "Error loading members: " + message);
                        });
                    }

                    @Override
                    public void onNetworkError(String message) {
                        runOnUiThread(() -> {
                            showLoading(false);
                            swipeRefreshLayout.setRefreshing(false);
                            paginationHelper.setLoading(false);

                            Toast.makeText(GroupActivity.this, "Network error: " + message, Toast.LENGTH_SHORT).show();
                            updateEmptyState();
                            Log.e(TAG, "Network error: " + message);
                        });
                    }
                });
    }

    private void loadMoreMembers() {
        paginationHelper.incrementPage();
        loadGroupMembers();
    }

    private void refreshGroup() {
        paginationHelper.reset();
        loadGroupInfo();
        loadGroupMembers();
    }

    private void showLoading(boolean show) {
        if (paginationHelper.getCurrentPage() == 1) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void updateEmptyState() {
        boolean isEmpty = membersList.isEmpty();
        textViewEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerViewMembers.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        if (isEmpty) {
            textViewEmpty.setText("No members found");
        }
    }

    private void openMemberProfile(GroupMember member) {
        if (member.getUser() != null) {
            Intent intent = new Intent(this, ViewProfileActivity.class);
            intent.putExtra(ViewProfileActivity.EXTRA_USERNAME, member.getUser().getUsername());
            intent.putExtra(ViewProfileActivity.EXTRA_USER_ID, member.getUserId());
            startActivity(intent);
        }
    }

    private void showMemberOptions(GroupMember member) {
        if (currentGroup == null || member == null) return;

        boolean canRemove = GroupService.canRemoveMember(currentGroup, member);
        boolean canMakeAdmin = GroupService.canMakeAdmin(currentGroup, member);
        boolean canRemoveAdmin = GroupService.canRemoveAdmin(currentGroup, member);

        List<String> options = new ArrayList<>();
        options.add("View Profile");

        // Temporarily show all options for testing
        options.add("Make Admin");
        options.add("Remove Admin");
        options.add("Remove from Group");

        // if (canMakeAdmin) {
        //     options.add("Make Admin");
        // }
        // if (canRemoveAdmin) {
        //     options.add("Remove Admin");
        // }
        // if (canRemove) {
        //     options.add("Remove from Group");
        // }

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle(member.getUserName());

        builder.setItems(options.toArray(new String[0]), (dialog, which) -> {
            String option = options.get(which);
            switch (option) {
                case "View Profile":
                    openMemberProfile(member);
                    break;
                case "Make Admin":
                    makeAdmin(member);
                    break;
                case "Remove Admin":
                    removeAdmin(member);
                    break;
                case "Remove from Group":
                    confirmRemoveMember(member);
                    break;
            }
        });

        builder.show();
    }

    private void addMembers() {
        // For now, show a simple dialog to add members by username
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Add Member");

        final com.google.android.material.textfield.TextInputEditText editText =
            new com.google.android.material.textfield.TextInputEditText(this);
        editText.setHint("Enter username");

        // Add padding
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        editText.setPadding(padding, padding, padding, padding);

        builder.setView(editText);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String username = editText.getText().toString().trim();
            if (!username.isEmpty()) {
                addMemberByUsername(username);
            }
        });

        builder.setNegativeButton("Cancel", null);

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();

        // Focus and show keyboard
        editText.requestFocus();
        dialog.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    private void addMemberByUsername(String username) {
        groupService.addMemberByUsername(groupId, username, new ApiCallback<Object>() {
            @Override
            public void onSuccess(Object result, String message) {
                runOnUiThread(() -> {
                    Toast.makeText(GroupActivity.this, "Member added successfully", Toast.LENGTH_SHORT).show();
                    // Reload members list
                    loadGroupMembers();
                });
            }

            @Override
            public void onError(int statusCode, String error) {
                runOnUiThread(() -> {
                    Toast.makeText(GroupActivity.this, "Failed to add member: " + error, Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onNetworkError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(GroupActivity.this, "Network error: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void makeAdmin(GroupMember member) {
        groupService.makeAdmin(groupId, member.getUserId(), new ApiCallback<Object>() {
            @Override
            public void onSuccess(Object result, String message) {
                runOnUiThread(() -> {
                    Toast.makeText(GroupActivity.this, member.getUserName() + " is now an admin", Toast.LENGTH_SHORT).show();
                    refreshGroup();
                });
            }

            @Override
            public void onError(int statusCode, String message) {
                runOnUiThread(() -> {
                    Toast.makeText(GroupActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onNetworkError(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(GroupActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void removeAdmin(GroupMember member) {
        groupService.removeAdmin(groupId, member.getUserId(), new ApiCallback<Object>() {
            @Override
            public void onSuccess(Object result, String message) {
                runOnUiThread(() -> {
                    Toast.makeText(GroupActivity.this, member.getUserName() + " is no longer an admin", Toast.LENGTH_SHORT).show();
                    refreshGroup();
                });
            }

            @Override
            public void onError(int statusCode, String message) {
                runOnUiThread(() -> {
                    Toast.makeText(GroupActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onNetworkError(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(GroupActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void confirmRemoveMember(GroupMember member) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Remove Member")
                .setMessage("Are you sure you want to remove " + member.getUserName() + " from this group?")
                .setPositiveButton("Remove", (dialog, which) -> removeMember(member))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void removeMember(GroupMember member) {
        groupService.removeMember(groupId, member.getUserId(), new ApiCallback<Object>() {
            @Override
            public void onSuccess(Object result, String message) {
                runOnUiThread(() -> {
                    Toast.makeText(GroupActivity.this, member.getUserName() + " removed from group", Toast.LENGTH_SHORT).show();
                    membersAdapter.removeMember(member);
                    updateEmptyState();
                });
            }

            @Override
            public void onError(int statusCode, String message) {
                runOnUiThread(() -> {
                    Toast.makeText(GroupActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onNetworkError(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(GroupActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void confirmLeaveGroup() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Leave Group")
                .setMessage("Are you sure you want to leave this group?")
                .setPositiveButton("Leave", (dialog, which) -> leaveGroup())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void leaveGroup() {
        groupService.leaveGroup(groupId, new ApiCallback<Object>() {
            @Override
            public void onSuccess(Object result, String message) {
                runOnUiThread(() -> {
                    Toast.makeText(GroupActivity.this, "Left group successfully", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onError(int statusCode, String message) {
                runOnUiThread(() -> {
                    Toast.makeText(GroupActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onNetworkError(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(GroupActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.group_menu, menu);
//        return true;
//    }

//    @Override
//    public boolean onPrepareOptionsMenu(Menu menu) {
//        if (currentGroup != null) {
//            boolean isAdmin = GroupService.hasAdminPermissions(currentGroup);
//            MenuItem editItem = menu.findItem(R.id.action_edit_group);
//            if (editItem != null) {
//                editItem.setVisible(isAdmin);
//            }
//        }
//        return super.onPrepareOptionsMenu(menu);
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int itemId = item.getItemId();
//
//        if (itemId == R.id.action_edit_group) {
//            editGroup();
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//
//    private void editGroup() {
//        Intent intent = new Intent(this, EditGroupActivity.class);
//        intent.putExtra(EditGroupActivity.EXTRA_GROUP_ID, groupId);
//        startActivity(intent);
//    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}