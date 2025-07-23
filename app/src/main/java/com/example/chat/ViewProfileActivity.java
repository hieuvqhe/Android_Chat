package com.example.chat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.chat.models.Friend;
import com.example.chat.models.User;
import com.example.chat.network.ApiCallback;
import com.example.chat.network.NetworkManager;
import com.example.chat.services.FriendshipService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class ViewProfileActivity extends AppCompatActivity {
    private static final String TAG = "ViewProfileActivity";
    
    // Intent extras
    public static final String EXTRA_USERNAME = "username";
    public static final String EXTRA_USER_ID = "user_id";
    
    // Views
    private MaterialToolbar toolbar;
    private ProgressBar progressBar;
    private MaterialCardView profileCard;
    private ImageView imageViewAvatar;
    private TextView textViewUsername;
    private TextView textViewDisplayName;
    private TextView textViewEmail;
    private TextView textViewBio;
    private TextView textViewLocation;
    private TextView textViewWebsite;
    private TextView textViewVerified;
    private MaterialButton buttonAddFriend;
    private MaterialButton buttonMessage;
    
    // Data
    private String username;
    private String userId;
    private User userProfile;
    
    // Services
    private NetworkManager networkManager;
    private FriendshipService friendshipService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);
        
        try {
            // Get intent data
            Intent intent = getIntent();
            username = intent.getStringExtra(EXTRA_USERNAME);
            userId = intent.getStringExtra(EXTRA_USER_ID);
            
            if (username == null && userId == null) {
                Toast.makeText(this, "Invalid user data", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            
            initViews();
            setupToolbar();
            setupClickListeners();
            
            // Initialize services
            networkManager = NetworkManager.getInstance(this);
            friendshipService = new FriendshipService(networkManager);
            
            // Load user profile
            loadUserProfile();
            
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progressBar);
        profileCard = findViewById(R.id.profileCard);
        imageViewAvatar = findViewById(R.id.imageViewAvatar);
        textViewUsername = findViewById(R.id.textViewUsername);
        textViewDisplayName = findViewById(R.id.textViewDisplayName);
        textViewEmail = findViewById(R.id.textViewEmail);
        textViewBio = findViewById(R.id.textViewBio);
        textViewLocation = findViewById(R.id.textViewLocation);
        textViewWebsite = findViewById(R.id.textViewWebsite);
        textViewVerified = findViewById(R.id.textViewVerified);
        buttonAddFriend = findViewById(R.id.buttonAddFriend);
        buttonMessage = findViewById(R.id.buttonMessage);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Profile");
        }
    }
    
    private void setupClickListeners() {
        buttonAddFriend.setOnClickListener(v -> handleAddFriend());
        buttonMessage.setOnClickListener(v -> handleMessage());
    }
    
    private void loadUserProfile() {
        showLoading(true);
        
        if (username != null) {
            // Use getUserProfile API with username
            String authHeader = networkManager.getAuthorizationHeader();
            if (authHeader == null) {
                showError("Authentication required");
                return;
            }
            
            Log.d(TAG, "Loading profile for username: " + username);
            
            networkManager.getApiService().getUserProfile(authHeader, username)
                .enqueue(new ApiCallback<User>() {
                    @Override
                    public void onSuccess(User user, String message) {
                        Log.d(TAG, "Profile loaded successfully: " + user.getUsername());
                        
                        runOnUiThread(() -> {
                            userProfile = user;
                            userId = user.getId(); // Set userId from response
                            displayUserProfile(user);
                            showLoading(false);
                        });
                    }
                    
                    @Override
                    public void onError(int statusCode, String error) {
                        Log.e(TAG, "Failed to load profile: " + error);
                        
                        runOnUiThread(() -> {
                            showError("Failed to load profile: " + error);
                            showLoading(false);
                        });
                    }
                    
                    @Override
                    public void onNetworkError(String error) {
                        Log.e(TAG, "Network error: " + error);
                        
                        runOnUiThread(() -> {
                            showError("Network error: " + error);
                            showLoading(false);
                        });
                    }
                });
        } else {
            showError("Username not provided");
        }
    }
    
    private void displayUserProfile(User user) {
        try {
            // Set basic info
            textViewUsername.setText("@" + user.getUsername());
            
            if (user.getName() != null && !user.getName().isEmpty()) {
                textViewDisplayName.setText(user.getName());
                textViewDisplayName.setVisibility(View.VISIBLE);
            } else {
                textViewDisplayName.setVisibility(View.GONE);
            }
            
            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                textViewEmail.setText(user.getEmail());
                textViewEmail.setVisibility(View.VISIBLE);
            } else {
                textViewEmail.setVisibility(View.GONE);
            }
            
            if (user.getBio() != null && !user.getBio().isEmpty()) {
                textViewBio.setText(user.getBio());
                textViewBio.setVisibility(View.VISIBLE);
            } else {
                textViewBio.setVisibility(View.GONE);
            }
            
            if (user.getLocation() != null && !user.getLocation().isEmpty()) {
                textViewLocation.setText(user.getLocation());
                textViewLocation.setVisibility(View.VISIBLE);
            } else {
                textViewLocation.setVisibility(View.GONE);
            }
            
            if (user.getWebsite() != null && !user.getWebsite().isEmpty()) {
                textViewWebsite.setText(user.getWebsite());
                textViewWebsite.setVisibility(View.VISIBLE);
            } else {
                textViewWebsite.setVisibility(View.GONE);
            }
            
            // Set verified status
            if (user.getVerify() != null && user.getVerify() == 1) {
                textViewVerified.setVisibility(View.VISIBLE);
            } else {
                textViewVerified.setVisibility(View.GONE);
            }
            
            // Load avatar
            if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                Glide.with(this)
                    .load(user.getAvatar())
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .into(imageViewAvatar);
            } else {
                imageViewAvatar.setImageResource(R.drawable.default_avatar);
            }
            
            profileCard.setVisibility(View.VISIBLE);
            
        } catch (Exception e) {
            Log.e(TAG, "Error displaying profile", e);
            showError("Error displaying profile");
        }
    }
    
    private void handleAddFriend() {
        if (userId == null) {
            Toast.makeText(this, "User ID not available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        buttonAddFriend.setEnabled(false);
        buttonAddFriend.setText("Adding...");
        
        friendshipService.addFriend(userId, new ApiCallback<Friend>() {
            @Override
            public void onSuccess(Friend result, String message) {
                runOnUiThread(() -> {
                    Toast.makeText(ViewProfileActivity.this, "Friend request sent!", Toast.LENGTH_SHORT).show();
                    buttonAddFriend.setText("Request Sent");
                    buttonAddFriend.setEnabled(false);
                });
            }
            
            @Override
            public void onError(int statusCode, String error) {
                runOnUiThread(() -> {
                    Toast.makeText(ViewProfileActivity.this, "Failed to send friend request: " + error, Toast.LENGTH_SHORT).show();
                    buttonAddFriend.setText("Add Friend");
                    buttonAddFriend.setEnabled(true);
                });
            }
            
            @Override
            public void onNetworkError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(ViewProfileActivity.this, "Network error: " + error, Toast.LENGTH_SHORT).show();
                    buttonAddFriend.setText("Add Friend");
                    buttonAddFriend.setEnabled(true);
                });
            }
        });
    }
    
    private void handleMessage() {
        if (userId == null) {
            Toast.makeText(this, "User ID not available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Navigate to chat (create private conversation)
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_OTHER_USER_ID, userId);
        intent.putExtra(ChatActivity.EXTRA_CONVERSATION_NAME, userProfile != null ? userProfile.getUsername() : "Chat");
        intent.putExtra(ChatActivity.EXTRA_CONVERSATION_TYPE, "private");
        startActivity(intent);
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        profileCard.setVisibility(show ? View.GONE : View.VISIBLE);
    }
    
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
