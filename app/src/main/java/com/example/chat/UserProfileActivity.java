package com.example.chat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.chat.models.AddFriendRequest;
import com.example.chat.models.Friend;
import com.example.chat.models.User;
import com.example.chat.network.ApiCallback;
import com.example.chat.network.NetworkManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class UserProfileActivity extends AppCompatActivity {
    private static final String TAG = "UserProfileActivity";

    // Intent Extra Keys - Support both possible keys for compatibility
    public static final String EXTRA_USERNAME = "extra_username";
    public static final String EXTRA_USERNAME_ALT = "username";
    public static final String EXTRA_USER_ID = "user_id";
    // Views
    private MaterialToolbar toolbar;
    private ImageView imageViewAvatar;
    private TextView textViewUsername;
    private TextView textViewEmail;
    private TextView textViewVerifyStatus;
    private TextView textViewBio;
    private TextView textViewLocation;
    private TextView textViewWebsite;
    private MaterialCardView cardBio;
    private MaterialCardView cardLocation;
    private MaterialCardView cardWebsite;
    private MaterialButton buttonSendMessage;
    private MaterialButton buttonAddFriend;

    // Data
    private String username;
    private User userProfile;
    private NetworkManager networkManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Get username from intent - try both possible keys
        username = getIntent().getStringExtra(EXTRA_USERNAME);
        if (username == null) {
            username = getIntent().getStringExtra(EXTRA_USERNAME_ALT);
        }

        Log.d(TAG, "onCreate: Received username = " + username);

        if (username == null || username.trim().isEmpty()) {
            Log.e(TAG, "No username provided in intent");
            Toast.makeText(this, "Error: No username provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupClickListeners();
        initServices();
        loadUserProfile();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        imageViewAvatar = findViewById(R.id.imageViewAvatar);
        textViewUsername = findViewById(R.id.textViewUsername);
        textViewEmail = findViewById(R.id.textViewEmail);
        textViewVerifyStatus = findViewById(R.id.textViewVerifyStatus);
        textViewBio = findViewById(R.id.textViewBio);
        textViewLocation = findViewById(R.id.textViewLocation);
        textViewWebsite = findViewById(R.id.textViewWebsite);
        cardBio = findViewById(R.id.cardBio);
        cardLocation = findViewById(R.id.cardLocation);
        cardWebsite = findViewById(R.id.cardWebsite);
        buttonSendMessage = findViewById(R.id.buttonSendMessage);
        buttonAddFriend = findViewById(R.id.buttonAddFriend);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(username != null ? username : "Profile");
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupClickListeners() {
        buttonSendMessage.setOnClickListener(v -> {
            // TODO: Implement send message functionality
            Toast.makeText(this, "Message feature coming soon!", Toast.LENGTH_SHORT).show();
        });

        buttonAddFriend.setOnClickListener(v -> {
            sendFriendRequest();
        });

        cardWebsite.setOnClickListener(v -> {
            if (userProfile != null && userProfile.getWebsite() != null && !userProfile.getWebsite().trim().isEmpty()) {
                openWebsite(userProfile.getWebsite());
            }
        });
    }

    private void initServices() {
        networkManager = NetworkManager.getInstance(this);
    }

    private void loadUserProfile() {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            Log.e(TAG, "No authorization token found");
            Toast.makeText(this, "Authentication error", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "Loading profile for username: " + username);

        networkManager.getApiService().getUserProfile(authHeader, username).enqueue(new ApiCallback<User>() {
            @Override
            public void onSuccess(User result, String message) {
                runOnUiThread(() -> {
                    userProfile = result;
                    displayUserProfile(result);
                    Log.d(TAG, "✅ User profile loaded successfully: " + result.getUsername());
                });
            }

            @Override
            public void onError(int statusCode, String message) {
                runOnUiThread(() -> {
                    Log.e(TAG, "❌ Error loading user profile: " + statusCode + " - " + message);
                    Toast.makeText(UserProfileActivity.this, "Error loading profile: " + message, Toast.LENGTH_SHORT).show();

                    if (statusCode == 401) {
                        // Handle unauthorized error
                        finish();
                    } else if (statusCode == 404) {
                        Toast.makeText(UserProfileActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }

            @Override
            public void onNetworkError(String message) {
                runOnUiThread(() -> {
                    Log.e(TAG, "🌐 Network error: " + message);
                    Toast.makeText(UserProfileActivity.this, "Network error: " + message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void displayUserProfile(User user) {
        if (user == null) return;

        // Set toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(user.getUsername() != null ? user.getUsername() : "Profile");
        }

        // Set username
        if (user.getUsername() != null && !user.getUsername().trim().isEmpty()) {
            textViewUsername.setText(user.getUsername());
        } else {
            textViewUsername.setText("No username");
        }

        // Set email
        if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
            textViewEmail.setText(user.getEmail());
        } else {
            textViewEmail.setText("No email");
        }

        // Set verification status - FIXED: Handle nullable Integer by checking for null
        if (textViewVerifyStatus != null) {
            try {
                // The 'verify' field can be null if the API doesn't send it for other users.
                if (user.getVerify() != null && user.getVerify() == 1) {
                    textViewVerifyStatus.setText("✓ Verified");
                    textViewVerifyStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                } else {
                    // Treat null or any other value as "Not verified"
                    textViewVerifyStatus.setText("Not verified");
                    textViewVerifyStatus.setTextColor(getResources().getColor(android.R.color.darker_gray));
                }
                textViewVerifyStatus.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                Log.w(TAG, "Error setting verification status: " + e.getMessage());
                textViewVerifyStatus.setVisibility(View.GONE);
            }
        }

        // Set bio
        if (user.getBio() != null && !user.getBio().trim().isEmpty()) {
            textViewBio.setText(user.getBio());
            cardBio.setVisibility(View.VISIBLE);
        } else {
            cardBio.setVisibility(View.GONE);
        }

        // Set location
        if (user.getLocation() != null && !user.getLocation().trim().isEmpty()) {
            textViewLocation.setText(user.getLocation());
            cardLocation.setVisibility(View.VISIBLE);
        } else {
            cardLocation.setVisibility(View.GONE);
        }

        // Set website
        if (user.getWebsite() != null && !user.getWebsite().trim().isEmpty()) {
            textViewWebsite.setText(user.getWebsite());
            cardWebsite.setVisibility(View.VISIBLE);
        } else {
            cardWebsite.setVisibility(View.GONE);
        }

        // Load avatar
        if (user.getAvatar() != null && !user.getAvatar().trim().isEmpty()) {
            Glide.with(this)
                    .load(user.getAvatar())
                    .apply(new RequestOptions().circleCrop())
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .into(imageViewAvatar);
        } else {
            Glide.with(this)
                    .load(R.drawable.default_avatar)
                    .apply(new RequestOptions().circleCrop())
                    .into(imageViewAvatar);
        }
    }

    private void sendFriendRequest() {
        if (userProfile == null) {
            Toast.makeText(this, "User profile not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            Toast.makeText(this, "Authentication error", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button to prevent double clicks
        buttonAddFriend.setEnabled(false);
        buttonAddFriend.setText("Sending...");

        AddFriendRequest request = new AddFriendRequest(userProfile.getId());

        networkManager.getApiService().addFriend(authHeader, request).enqueue(new ApiCallback<Friend>() {
            @Override
            public void onSuccess(Friend result, String message) {
                runOnUiThread(() -> {
                    Toast.makeText(UserProfileActivity.this, "Friend request sent to " + userProfile.getUsername(), Toast.LENGTH_SHORT).show();

                    // Update button state
                    buttonAddFriend.setText("Request Sent");
                    buttonAddFriend.setEnabled(false);

                    Log.d(TAG, "✅ Friend request sent successfully");
                });
            }

            @Override
            public void onError(int statusCode, String message) {
                runOnUiThread(() -> {
                    Log.e(TAG, "❌ Error sending friend request: " + statusCode + " - " + message);
                    Toast.makeText(UserProfileActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();

                    // Re-enable button
                    buttonAddFriend.setEnabled(true);
                    buttonAddFriend.setText("Add Friend");
                });
            }

            @Override
            public void onNetworkError(String message) {
                runOnUiThread(() -> {
                    Log.e(TAG, "🌐 Network error: " + message);
                    Toast.makeText(UserProfileActivity.this, "Network error: " + message, Toast.LENGTH_SHORT).show();

                    // Re-enable button
                    buttonAddFriend.setEnabled(true);
                    buttonAddFriend.setText("Add Friend");
                });
            }
        });
    }

    private void openWebsite(String website) {
        try {
            String url = website;
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url;
            }

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening website: " + e.getMessage());
            Toast.makeText(this, "Cannot open website", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
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