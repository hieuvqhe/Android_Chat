package com.example.chat;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.chat.fragments.ChatsFragment;
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
    private ProfileFragment profileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        networkManager = NetworkManager.getInstance(this);

        // Kiểm tra đăng nhập
        if (!networkManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }

        setupToolbar();
        initViews();
        setupBottomNavigation();

        // Hiển thị fragment mặc định
        if (savedInstanceState == null) {
            showChatsFragment();
        }
    }

    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void initViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation);
        fragmentManager = getSupportFragmentManager();
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_chats) {
                showChatsFragment();
                updateToolbarTitle("Chats");
                return true;
            } else if (itemId == R.id.navigation_friends) {
                showFriendsActivity();
                return true;
            } else if (itemId == R.id.navigation_profile) {
                showProfileFragment();
                updateToolbarTitle("Profile");
                return true;
            }

            return false;
        });
    }

    private void showChatsFragment() {
        if (chatsFragment == null) {
            chatsFragment = new ChatsFragment();
        }
        replaceFragment(chatsFragment);
        updateToolbarTitle("Chats");
    }

    private void showFriendsActivity() {
        // Navigate to existing FriendsActivity
        Intent intent = new Intent(this, FriendsActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void showProfileFragment() {
        if (profileFragment == null) {
            profileFragment = new ProfileFragment();
        }
        replaceFragment(profileFragment);
        updateToolbarTitle("Profile");
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }

    private void updateToolbarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reset bottom navigation selection when returning from FriendsActivity
        if (bottomNavigation.getSelectedItemId() == R.id.navigation_friends) {
            bottomNavigation.setSelectedItemId(R.id.navigation_chats);
        }
    }
}