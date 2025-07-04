package com.example.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.example.chat.fragments.FindFriendsFragment;
import com.example.chat.fragments.FriendRequestsFragment;
import com.example.chat.fragments.FriendsListFragment;
import com.example.chat.fragments.SentRequestsFragment;
import com.example.chat.models.Friend;
import com.example.chat.models.UserInfo;
import com.example.chat.network.NetworkManager;
import com.example.chat.utils.FriendUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class FriendsActivity extends AppCompatActivity {
    private static final String TAG = "FriendsActivity";

    private MaterialToolbar toolbar;
    private ProgressBar progressBar;
    private BottomNavigationView bottomNavigationFriends;

    // Fragments
    private FindFriendsFragment findFriendsFragment;
    private FriendsListFragment friendsListFragment;
    private FriendRequestsFragment friendRequestsFragment;
    private SentRequestsFragment sentRequestsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        initViews();
        setupToolbar();
        setupBottomNavigation();

        // Show default fragment
        if (savedInstanceState == null) {
            showFindFriendsFragment();
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progressBar);
        bottomNavigationFriends = findViewById(R.id.bottomNavigationFriends);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Friends");
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupBottomNavigation() {
        bottomNavigationFriends.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_find_friends) {
                showFindFriendsFragment();
                updateToolbarTitle("Find Friends");
                return true;
            } else if (itemId == R.id.navigation_my_friends) {
                showFriendsListFragment();
                updateToolbarTitle("My Friends");
                return true;
            } else if (itemId == R.id.navigation_friend_requests) {
                showFriendRequestsFragment();
                updateToolbarTitle("Friend Requests");
                return true;
            } else if (itemId == R.id.navigation_sent_requests) {
                showSentRequestsFragment();
                updateToolbarTitle("Sent Requests");
                return true;
            }

            return false;
        });
    }

    /**
     * Show Find Friends Fragment
     */
    private void showFindFriendsFragment() {
        if (findFriendsFragment == null) {
            findFriendsFragment = new FindFriendsFragment();
            findFriendsFragment.setOnFriendRequestSentListener(() -> {
                // Callback when friend request sent successfully
                // Refresh sent requests if that tab is active
                if (sentRequestsFragment != null) {
                    sentRequestsFragment.refreshData();
                }
            });
        }
        replaceFragment(findFriendsFragment);
    }

    /**
     * Show Friends List Fragment
     */
    private void showFriendsListFragment() {
        if (friendsListFragment == null) {
            friendsListFragment = new FriendsListFragment();
        }
        replaceFragment(friendsListFragment);
    }

    /**
     * Show Friend Requests Fragment
     */
    private void showFriendRequestsFragment() {
        if (friendRequestsFragment == null) {
            friendRequestsFragment = new FriendRequestsFragment();
            friendRequestsFragment.setOnRequestActionListener(() -> {
                // Callback when request is accepted/rejected
                // Refresh friends list if that tab is active
                if (friendsListFragment != null) {
                    friendsListFragment.refreshData();
                }
            });
        }
        replaceFragment(friendRequestsFragment);
    }

    /**
     * Show Sent Requests Fragment
     */
    private void showSentRequestsFragment() {
        if (sentRequestsFragment == null) {
            sentRequestsFragment = new SentRequestsFragment();
            sentRequestsFragment.setOnSentRequestActionListener(() -> {
                // Callback when sent request is cancelled
                // Could refresh other tabs if needed
            });
        }
        replaceFragment(sentRequestsFragment);
    }

    /**
     * Replace fragment
     */
    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }

    /**
     * Update toolbar title
     */
    private void updateToolbarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    /**
     * Show/hide loading
     */
    public void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    // ================== PROFILE NAVIGATION METHODS ==================

    /**
     * Open user profile by username
     */
    public void openUserProfile(String username) {
        if (username != null && !username.trim().isEmpty()) {
            Intent intent = new Intent(this, UserProfileActivity.class);
            intent.putExtra(UserProfileActivity.EXTRA_USERNAME, username);
            startActivity(intent);
        }
    }

    /**
     * Open user profile from UserInfo object
     */
    public void openUserProfile(UserInfo userInfo) {
        if (userInfo != null && userInfo.getUsername() != null) {
            openUserProfile(userInfo.getUsername());
        }
    }

    /**
     * Open friend profile from Friend object
     */
    public void openFriendProfile(Friend friend) {
        if (friend != null) {
            UserInfo friendUser = FriendUtils.getFriendUser(friend);
            if (friendUser != null && friendUser.getUsername() != null) {
                openUserProfile(friendUser.getUsername());
            }
        }
    }

    /**
     * Refresh all fragments
     */
    public void refreshAllFragments() {
        if (findFriendsFragment != null) {
            // Find friends fragment auto-loads
        }
        if (friendsListFragment != null) {
            friendsListFragment.refreshData();
        }
        if (friendRequestsFragment != null) {
            friendRequestsFragment.refreshData();
        }
        if (sentRequestsFragment != null) {
            sentRequestsFragment.refreshData();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh current fragment when returning to activity
        refreshCurrentFragment();
    }

    /**
     * Refresh current active fragment
     */
    private void refreshCurrentFragment() {
        int selectedItemId = bottomNavigationFriends.getSelectedItemId();

        if (selectedItemId == R.id.navigation_my_friends && friendsListFragment != null) {
            friendsListFragment.refreshData();
        } else if (selectedItemId == R.id.navigation_friend_requests && friendRequestsFragment != null) {
            friendRequestsFragment.refreshData();
        } else if (selectedItemId == R.id.navigation_sent_requests && sentRequestsFragment != null) {
            sentRequestsFragment.refreshData();
        }
    }
}