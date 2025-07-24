package com.example.chat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chat.models.ApiResponse;
import com.example.chat.models.LoginRequest;
import com.example.chat.models.LoginResponseData;
import com.example.chat.models.UpdateProfileRequest;
import com.example.chat.models.User;
import com.example.chat.network.ApiCallback;
import com.example.chat.network.NetworkManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;

public class WelcomeUserActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView textWelcome;
    private EditText userNameInput;
    private MaterialButton chatNowButton;

    private NetworkManager networkManager;
    private User userProfile;

    private boolean isRegistered;
    private static final String TAG = "UserWelcome";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        try {
            setContentView(R.layout.activity_welcome_user);
            networkManager = NetworkManager.getInstance(this);
            isRegistered = getIntent().getBooleanExtra("isRegistered", false);

            initViews();
            setupToolbar();
            setupClickListeners();

            // Load user profile first, then setup UI based on result
            loadMyProfile();

        } catch (Exception e) {
            Toast.makeText(this, "App initialization failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "onCreate error: ", e);
            finish();
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbarRegister);
        textWelcome = findViewById(R.id.text_welcome);
        userNameInput = findViewById(R.id.phone_number_input);
        chatNowButton = findViewById(R.id.sendOtpButton);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupClickListeners() {
        chatNowButton.setOnClickListener(v -> {
            String newUsername = userNameInput.getText().toString().trim();
            if (newUsername.isEmpty()) {
                Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isRegistered) {
                // User is returning, just navigate to main
                navigateToMainActivity();
            } else {
                // New user, update profile first
                updateUsername(newUsername);
            }
        });
    }

    // Load current user's profile
    private void loadMyProfile() {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            showToast("Authentication required", true);
            finish();
            return;
        }

        networkManager.getApiService().getMyProfile(authHeader).enqueue(new ApiCallback<User>() {
            @Override
            public void onSuccess(User result, String message) {
                runOnUiThread(() -> {
                    userProfile = result;
                    Log.d(TAG, "✅ My profile loaded: " + result.getUsername());

                    // Save user ID
                    if (result.getId() != null) {
                        networkManager.saveUserId(result.getId());
                    }

                    setupUIBasedOnUserType();
                });
            }

            @Override
            public void onError(int statusCode, String message) {
                runOnUiThread(() -> {
                    Log.e(TAG, "❌ Error loading my profile: " + message);
                    showToast("Error loading profile: " + message, true);
                    if (statusCode == 401) {
                        finish();
                    }
                });
            }

            @Override
            public void onNetworkError(String message) {
                showToast("Network error: " + message, true);
            }
        });
    }

    private void setupUIBasedOnUserType() {
        if (userProfile == null) {
            Log.e(TAG, "userProfile is null in setupUIBasedOnUserType");
            return;
        }

        if (isRegistered) {
            // Returning user
            textWelcome.setText("Welcome back!");
            userNameInput.setText(userProfile.getUsername());
            userNameInput.setEnabled(false); // Don't allow editing for returning users
            chatNowButton.setText("Continue to Chat");
        } else {
            // New user
            textWelcome.setText("Welcome! Please set your username:");
            userNameInput.setText(""); // Let them enter new username
            userNameInput.setEnabled(true);
            chatNowButton.setText("Start Chatting");
        }
    }

    private void updateUsername(String newUsername) {
        if (userProfile == null) {
            Log.e(TAG, "userProfile is null in updateUsername");
            showToast("Profile not loaded yet", true);
            return;
        }

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setUsername(newUsername);
        request.setBio(userProfile.getBio() != null ? userProfile.getBio() : "");
        request.setLocation(userProfile.getLocation() != null ? userProfile.getLocation() : "");
        request.setWebsite(userProfile.getWebsite() != null ? userProfile.getWebsite() : "");
        request.setAvatar(userProfile.getAvatar());

        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            showToast("Authentication required", true);
            finish();
            return;
        }

        networkManager.getApiService().updateMyProfile(authHeader, request).enqueue(new ApiCallback<User>() {
            @Override
            public void onSuccess(User result, String message) {
                runOnUiThread(() -> {
                    userProfile = result; // Update local profile
                    Toast.makeText(WelcomeUserActivity.this, "Username updated successfully!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onSuccess: Username updated successfully");
                    navigateToMainActivity();
                });
            }

            @Override
            public void onError(int statusCode, String message) {
                showToast("Failed to update username: " + message, true);
                Log.e(TAG, "onError: Failed to update username: " + message);
            }

            @Override
            public void onNetworkError(String message) {
                showToast("Network error: " + message, true);
            }
        });
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showToast(String message, boolean isLong) {
        runOnUiThread(() ->
                Toast.makeText(this, message, isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show()
        );
    }
}