package com.example.chat;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.example.chat.fragments.FindFriendsFragment;
import com.example.chat.fragments.FriendsListFragment;
import com.example.chat.models.FriendListResponse;
import com.example.chat.network.NetworkManager;
import com.example.chat.services.FriendshipService;
import com.google.android.material.appbar.MaterialToolbar;

public class FriendsActivity extends AppCompatActivity {
    private static final String TAG = "FriendsActivity";

    private MaterialToolbar toolbar;
    private ProgressBar progressBar;
    private FriendshipService friendshipService;

    private FindFriendsFragment findFriendsFragment;
    private FriendsListFragment friendsListFragment;

    private boolean hasFriends = false;
    private boolean isInFindMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        initViews();
        setupToolbar();
        initFriendshipService();
        checkUserFriends();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Friends");
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void initFriendshipService() {
        NetworkManager networkManager = NetworkManager.getInstance(this);
        friendshipService = new FriendshipService(networkManager);
    }

    /**
     * Kiểm tra xem user có bạn bè hay không
     */
    private void checkUserFriends() {
        showLoading(true);

        friendshipService.getAllFriends(1, 1, new FriendshipService.FriendshipCallback<FriendListResponse>() {
            @Override
            public void onSuccess(FriendListResponse response) {
                runOnUiThread(() -> {
                    showLoading(false);

                    if (response.getFriends() != null && !response.getFriends().isEmpty()) {
                        // Có bạn bè - hiển thị danh sách bạn bè
                        hasFriends = true;
                        showFriendsListFragment();
                    } else {
                        // Không có bạn bè - hiển thị màn hình tìm bạn
                        hasFriends = false;
                        showFindFriendsFragment();
                    }

                    updateToolbarTitle();
                });
            }

            @Override
            public void onError(int statusCode, String message) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(FriendsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                    // Mặc định hiển thị màn hình tìm bạn nếu có lỗi
                    showFindFriendsFragment();
                });
            }

            @Override
            public void onNetworkError(String message) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(FriendsActivity.this, "Network error: " + message, Toast.LENGTH_SHORT).show();
                    showFindFriendsFragment();
                });
            }
        });
    }

    /**
     * Hiển thị fragment tìm bạn
     */
    private void showFindFriendsFragment() {
        if (findFriendsFragment == null) {
            findFriendsFragment = new FindFriendsFragment();
            findFriendsFragment.setOnFriendRequestSentListener(() -> {
                // Callback khi gửi friend request thành công
                Toast.makeText(this, "Friend request sent! Check back later for updates.", Toast.LENGTH_LONG).show();
            });
        }

        isInFindMode = true;
        replaceFragment(findFriendsFragment);
        updateToolbarTitle();
    }

    /**
     * Hiển thị fragment danh sách bạn bè
     */
    private void showFriendsListFragment() {
        if (friendsListFragment == null) {
            friendsListFragment = new FriendsListFragment();
        }

        isInFindMode = false;
        replaceFragment(friendsListFragment);
        updateToolbarTitle();
    }

    /**
     * Thay thế fragment
     */
    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }

    /**
     * Cập nhật title của toolbar
     */
    private void updateToolbarTitle() {
        if (getSupportActionBar() != null) {
            if (isInFindMode) {
                getSupportActionBar().setTitle("Find Friends");
            } else {
                getSupportActionBar().setTitle("My Friends");
            }
        }

        // Cập nhật menu
        invalidateOptionsMenu();
    }

    /**
     * Hiển thị/ẩn loading
     */
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    /**
     * Refresh dữ liệu
     */
    public void refreshData() {
        checkUserFriends();
    }

    /**
     * Chuyển đổi giữa find friends và friends list
     */
    public void toggleMode() {
        if (isInFindMode) {
            if (hasFriends) {
                showFriendsListFragment();
            } else {
                Toast.makeText(this, "You don't have any friends yet. Keep finding!", Toast.LENGTH_SHORT).show();
            }
        } else {
            showFindFriendsFragment();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.friends_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem toggleItem = menu.findItem(R.id.action_toggle_mode);
        MenuItem refreshItem = menu.findItem(R.id.action_refresh);

        if (toggleItem != null) {
            if (isInFindMode) {
                toggleItem.setTitle("My Friends");
                toggleItem.setIcon(R.drawable.ic_people);
            } else {
                toggleItem.setTitle("Find Friends");
                toggleItem.setIcon(R.drawable.ic_person_add);
            }
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_toggle_mode) {
            toggleMode();
            return true;
        } else if (itemId == R.id.action_refresh) {
            refreshData();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}