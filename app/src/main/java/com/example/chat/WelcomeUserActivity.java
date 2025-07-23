package com.example.chat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chat.models.ApiResponse;
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

    private String username = getIntent().getStringExtra("phone");
    private boolean isRegister = getIntent().getBooleanExtra("isRegister", false);
    private static final String TAG = "UserWelcome";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        try {
            setContentView(R.layout.activity_welcome_user);
            initViews();
            setupToolbar();
            setupClickListeners();
            if(isRegister){
                loadUserProfile();
                textWelcome.setText("Welcome come back!");
                userNameInput.setText(userProfile.getUsername());
            }
            networkManager = NetworkManager.getInstance(this);

        } catch (Exception e) {
            Toast.makeText(this, "App initialization failed", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setNewUserName(String newUserName) {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setUsername(newUserName);
        request.setBio(userProfile.getBio().isEmpty() ? null : userProfile.getBio());
        request.setLocation(userProfile.getLocation().isEmpty() ? null : userProfile.getLocation());
        request.setWebsite(userProfile.getWebsite().isEmpty() ? null : userProfile.getWebsite());
        // Keep current avatar for now
        request.setAvatar(userProfile.getAvatar());
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            Toast.makeText(this, "Authentication error", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        networkManager.getApiService().updateMyProfile(authHeader, request).enqueue(new ApiCallback<User>() {
            @Override
            public void onSuccess(User result, String message) {
                runOnUiThread(() -> {
                    Toast.makeText(WelcomeUserActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    finish(); // Return to profile fragment
                });
            }

            @Override
            public void onError(int statusCode, String message) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Error updating profile: " + message);
                    Toast.makeText(WelcomeUserActivity.this, "Failed to update profile: " + message, Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onNetworkError(String message) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Network error: " + message);
                    Toast.makeText(WelcomeUserActivity.this, "Network error: " + message, Toast.LENGTH_LONG).show();
                });
            }
        });

    }

    private void loadUserProfile() {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            Log.e(TAG, "No authorization token found");
            Toast.makeText(this, "Authentication error", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "Loading profile for username: " + getIntent().getStringExtra("phone"));

        networkManager.getApiService().getUserProfile(authHeader, username).enqueue(new ApiCallback<User>() {
            @Override
            public void onSuccess(User result, String message) {
                runOnUiThread(() -> {
                    userProfile = result;
                    Log.d(TAG, "✅ User profile loaded successfully: " + result.getUsername());
                });
            }

            @Override
            public void onError(int statusCode, String message) {
                runOnUiThread(() -> {
                    Log.e(TAG, "❌ Error loading user profile: " + statusCode + " - " + message);
                    Toast.makeText(WelcomeUserActivity.this, "Error loading profile: " + message, Toast.LENGTH_SHORT).show();

                    if (statusCode == 401) {
                        // Handle unauthorized error
                        finish();
                    } else if (statusCode == 404) {
                        Toast.makeText(WelcomeUserActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }

            @Override
            public void onNetworkError(String message) {
                runOnUiThread(() -> {
                    Log.e(TAG, "🌐 Network error: " + message);
                    Toast.makeText(WelcomeUserActivity.this, "Network error: " + message, Toast.LENGTH_SHORT).show();
                });
            }
        });
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
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupClickListeners() {
        if(chatNowButton != null){
            chatNowButton.setOnClickListener(v -> {
                setNewUserName(userNameInput.getText().toString());
                getUserProfile();
            });
        }
    }

    private void getUserProfile() {
        try {
            String authHeader = networkManager.getAuthorizationHeader();
            if (authHeader == null) {
                Log.e(TAG, "No authorization header available");
                navigateToMainActivity();
                return;
            }

            Call<ApiResponse<User>> call = networkManager.getApiService().getMyProfile(authHeader);
            call.enqueue(new ApiCallback<User>() {
                @Override
                public void onSuccess(User user, String message) {
                    Log.d(TAG, "User profile retrieved successfully");

                    runOnUiThread(() -> {
                        try {
                            // Save user ID
                            if (user != null && user.getId() != null) {
                                networkManager.saveUserId(user.getId());
                                Log.d(TAG, "User ID saved: " + user.getId());
                            }

                            Toast.makeText(WelcomeUserActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                            navigateToMainActivity();

                        } catch (Exception e) {
                            Log.e(TAG, "Error processing user profile", e);
                            // Still navigate to main even if user profile fails
                            navigateToMainActivity();
                        }
                    });
                }

                @Override
                public void onError(int statusCode, String error) {
                    Log.e(TAG, "Failed to get user profile: " + error);

                    runOnUiThread(() -> {
                        // Still navigate to main even if user profile fails
                        Toast.makeText(WelcomeUserActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                        navigateToMainActivity();
                    });
                }

                @Override
                public void onNetworkError(String error) {
                    Log.e(TAG, "Network error getting user profile: " + error);

                    runOnUiThread(() -> {
                        // Still navigate to main even if user profile fails
                        Toast.makeText(WelcomeUserActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                        navigateToMainActivity();
                    });
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error getting user profile", e);
            // Still navigate to main even if user profile fails
            navigateToMainActivity();
        }
    }

    private void navigateToMainActivity() {
        try {
            Intent intent = new Intent(WelcomeUserActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to MainActivity", e);
            Toast.makeText(this, "Navigation error", Toast.LENGTH_SHORT).show();
        }
    }
}