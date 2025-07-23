package com.example.chat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.chat.fragments.ChatsFragment;
import com.example.chat.fragments.GroupsFragment;
// import com.example.chat.fragments.FriendsActivity; // Removed - FriendsActivity is not a fragment
import com.example.chat.fragments.ProfileFragment;
import com.example.chat.network.NetworkManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private NetworkManager networkManager;
    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNavigation;
    private FragmentManager fragmentManager;

    // Fragments
    private ChatsFragment chatsFragment;
    private GroupsFragment groupsFragment;
    private ProfileFragment profileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_main);

            // Initialize NetworkManager with error handling
            try {
                networkManager = NetworkManager.getInstance(this);
            } catch (Exception e) {
                Log.e("MainActivity", "Failed to initialize NetworkManager", e);
                // If NetworkManager fails, still continue but handle login check carefully
            }

            // Kiểm tra đăng nhập với error handling
            try {
                if (networkManager != null && !networkManager.isLoggedIn()) {
                    navigateToLogin();
                    return;
                }
            } catch (Exception e) {
                Log.e("MainActivity", "Login check crashed", e);
                // If login check fails, assume not logged in and navigate to login
                navigateToLogin();
                return;
            }

            // Initialize UI components with error handling
            try {
                setupToolbar();
                initViews();
                setupBottomNavigation();

                // Hiển thị fragment mặc định
                if (savedInstanceState == null) {
                    showChatsFragment();
                }
            } catch (Exception e) {
                Log.e("MainActivity", "Failed to initialize UI components", e);
                // If UI initialization fails, navigate to login as fallback
                navigateToLogin();
                return;
            }

        } catch (Exception e) {
            Log.e("MainActivity", "Critical error in onCreate", e);
            // Critical error - try to navigate to login or finish activity
            try {
                navigateToLogin();
            } catch (Exception ex) {
                Log.e("MainActivity", "Failed to navigate to login, finishing activity", ex);
                finish();
            }
        }
    }

    private void setupToolbar() {
        try {
            toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
            } else {
                Log.e("MainActivity", "Toolbar not found in layout");
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Failed to setup toolbar", e);
        }
    }

    private void initViews() {
        try {
            bottomNavigation = findViewById(R.id.bottomNavigation);
            if (bottomNavigation == null) {
                Log.e("MainActivity", "BottomNavigation not found in layout");
                throw new RuntimeException("BottomNavigation not found");
            }

            fragmentManager = getSupportFragmentManager();
            if (fragmentManager == null) {
                Log.e("MainActivity", "FragmentManager is null");
                throw new RuntimeException("FragmentManager is null");
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Failed to initialize views", e);
            throw e; // Re-throw to be handled by onCreate
        }
    }

    private void setupBottomNavigation() {
        try {
            if (bottomNavigation == null) {
                Log.e("MainActivity", "BottomNavigation is null, cannot setup");
                return;
            }

            bottomNavigation.setOnItemSelectedListener(item -> {
                try {
                    int itemId = item.getItemId();

                    if (itemId == R.id.navigation_chats) {
                        showChatsFragment();
                        updateToolbarTitle("Chats");
                        return true;
                    } else if (itemId == R.id.navigation_friends) {
                        showFriendsActivity();
                        return true;
                    } else if (itemId == R.id.navigation_groups) {
                        showGroupsFragment();
                        updateToolbarTitle("Groups");
                        return true;
                    } else if (itemId == R.id.navigation_profile) {
                        showProfileFragment();
                        updateToolbarTitle("Profile");
                        return true;
                    }

                    return false;
                } catch (Exception e) {
                    Log.e("MainActivity", "Error in bottom navigation item selection", e);
                    return false;
                }
            });
        } catch (Exception e) {
            Log.e("MainActivity", "Failed to setup bottom navigation", e);
            throw e; // Re-throw to be handled by onCreate
        }
    }

    private void showChatsFragment() {
        try {
            if (chatsFragment == null) {
                chatsFragment = new ChatsFragment();
            }
            replaceFragment(chatsFragment);
            updateToolbarTitle("Chats");
        } catch (Exception e) {
            Log.e("MainActivity", "Failed to show ChatsFragment", e);
        }
    }

    private void showFriendsActivity() {
        try {
            // Navigate to existing FriendsActivity
            Intent intent = new Intent(this, FriendsActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } catch (Exception e) {
            Log.e("MainActivity", "Failed to show FriendsActivity", e);
        }
    }

    private void showGroupsFragment() {
        try {
            if (groupsFragment == null) {
                groupsFragment = new GroupsFragment();
            }
            replaceFragment(groupsFragment);
            updateToolbarTitle("Groups");
        } catch (Exception e) {
            Log.e("MainActivity", "Failed to show GroupsFragment", e);
        }
    }

    private void showProfileFragment() {
        try {
            if (profileFragment == null) {
                profileFragment = new ProfileFragment();
            }
            replaceFragment(profileFragment);
            updateToolbarTitle("Profile");
        } catch (Exception e) {
            Log.e("MainActivity", "Failed to show ProfileFragment", e);
        }
    }

    private void replaceFragment(Fragment fragment) {
        try {
            if (fragmentManager == null) {
                Log.e("MainActivity", "FragmentManager is null, cannot replace fragment");
                return;
            }

            if (fragment == null) {
                Log.e("MainActivity", "Fragment is null, cannot replace");
                return;
            }

            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.fragmentContainer, fragment);
            transaction.commitAllowingStateLoss(); // Use commitAllowingStateLoss to avoid IllegalStateException
        } catch (Exception e) {
            Log.e("MainActivity", "Failed to replace fragment", e);
        }
    }

    private void updateToolbarTitle(String title) {
        try {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(title);
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Failed to update toolbar title", e);
        }
    }

    private void navigateToLogin() {
        try {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        } catch (Exception e) {
            Log.e("MainActivity", "Failed to navigate to login", e);
            // If navigation fails, just finish the activity
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            // Reset bottom navigation selection when returning from FriendsActivity
            if (bottomNavigation != null && bottomNavigation.getSelectedItemId() == R.id.navigation_friends) {
                bottomNavigation.setSelectedItemId(R.id.navigation_chats);
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error in onResume", e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_logout) {
            handleLogout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void handleLogout() {
        try {
            if (networkManager != null) {
                networkManager.clearTokens();
            }
            navigateToLogin();
        } catch (Exception e) {
            Log.e("MainActivity", "Error during logout", e);
            navigateToLogin(); // Still navigate to login even if logout fails
        }
    }
}