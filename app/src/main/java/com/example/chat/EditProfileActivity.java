package com.example.chat;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.chat.models.UpdateProfileRequest;
import com.example.chat.models.User;
import com.example.chat.network.ApiCallback;
import com.example.chat.network.NetworkManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;

public class EditProfileActivity extends AppCompatActivity {
    private static final String TAG = "EditProfileActivity";

    // Views
    private MaterialToolbar toolbar;
    private ShapeableImageView imageViewAvatar;
    private TextInputEditText editTextUsername;
    private TextInputEditText editTextBio;
    private TextInputEditText editTextLocation;
    private TextInputEditText editTextWebsite;
    private MaterialButton buttonSave;
    private MaterialButton buttonCancel;

    // Services
    private NetworkManager networkManager;

    // Data
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initViews();
        initServices();
        setupToolbar();

        // SỬA LỖI: Lấy dữ liệu người dùng trực tiếp từ Intent được truyền sang.
        // Không gọi lại API một cách không cần thiết.
        if (getIntent().hasExtra("CURRENT_USER")) {
            currentUser = (User) getIntent().getSerializableExtra("CURRENT_USER");
        }

        // Nếu vì lý do nào đó không nhận được dữ liệu, thông báo lỗi và đóng Activity.
        if (currentUser == null) {
            Toast.makeText(this, "Failed to load user data.", Toast.LENGTH_SHORT).show();
            finish();
            return; // Dừng thực thi để tránh crash
        }

        // Sau khi đã có dữ liệu người dùng, tiến hành cài đặt các listener và hiển thị dữ liệu.
        setupClickListeners();
        loadUserData();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        imageViewAvatar = findViewById(R.id.imageViewAvatar);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextBio = findViewById(R.id.editTextBio);
        editTextLocation = findViewById(R.id.editTextLocation);
        editTextWebsite = findViewById(R.id.editTextWebsite);
        buttonSave = findViewById(R.id.buttonSave);
        buttonCancel = findViewById(R.id.buttonCancel);
    }

    private void initServices() {
        networkManager = NetworkManager.getInstance(this);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Edit Profile");
        }
    }

    private void setupClickListeners() {
        buttonSave.setOnClickListener(v -> saveProfile());
        buttonCancel.setOnClickListener(v -> finish());

        imageViewAvatar.setOnClickListener(v -> {
            // TODO: Implement image selection
            Toast.makeText(this, "Image selection coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadUserData() {
        // Phương thức này giờ đây an toàn để gọi vì chúng ta đã đảm bảo currentUser không null.
        if (currentUser == null) {
            Toast.makeText(this, "No user data found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set current values
        if (currentUser.getUsername() != null) {
            editTextUsername.setText(currentUser.getUsername());
        }

        if (currentUser.getBio() != null) {
            editTextBio.setText(currentUser.getBio());
        }

        if (currentUser.getLocation() != null) {
            editTextLocation.setText(currentUser.getLocation());
        }

        if (currentUser.getWebsite() != null) {
            editTextWebsite.setText(currentUser.getWebsite());
        }

        // Load avatar
        if (currentUser.getAvatar() != null && !currentUser.getAvatar().trim().isEmpty()) {
            Glide.with(this)
                    .load(currentUser.getAvatar())
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

    private void saveProfile() {
        // Get input values
        String username = editTextUsername.getText() != null ? editTextUsername.getText().toString().trim() : "";
        String bio = editTextBio.getText() != null ? editTextBio.getText().toString().trim() : "";
        String location = editTextLocation.getText() != null ? editTextLocation.getText().toString().trim() : "";
        String website = editTextWebsite.getText() != null ? editTextWebsite.getText().toString().trim() : "";

        // Basic validation
        if (username.isEmpty()) {
            editTextUsername.setError("Username is required");
            editTextUsername.requestFocus();
            return;
        }

        if (username.length() < 5 || username.length() > 60) {
            editTextUsername.setError("Username must be between 5-60 characters");
            editTextUsername.requestFocus();
            return;
        }

        if (!website.isEmpty() && !isValidUrl(website)) {
            editTextWebsite.setError("Please enter a valid URL");
            editTextWebsite.requestFocus();
            return;
        }

        // Disable save button to prevent multiple requests
        buttonSave.setEnabled(false);
        buttonSave.setText("Saving...");

        // Create update request
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setUsername(username);
        request.setBio(bio.isEmpty() ? null : bio);
        request.setLocation(location.isEmpty() ? null : location);
        request.setWebsite(website.isEmpty() ? null : website);
        // Keep current avatar for now
        request.setAvatar(currentUser.getAvatar());

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
                    Toast.makeText(EditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    finish(); // Return to profile fragment
                });
            }

            @Override
            public void onError(int statusCode, String message) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Error updating profile: " + message);
                    Toast.makeText(EditProfileActivity.this, "Failed to update profile: " + message, Toast.LENGTH_LONG).show();

                    // Re-enable save button
                    buttonSave.setEnabled(true);
                    buttonSave.setText("Save Changes");
                });
            }

            @Override
            public void onNetworkError(String message) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Network error: " + message);
                    Toast.makeText(EditProfileActivity.this, "Network error: " + message, Toast.LENGTH_LONG).show();

                    // Re-enable save button
                    buttonSave.setEnabled(true);
                    buttonSave.setText("Save Changes");
                });
            }
        });
    }

    private boolean isValidUrl(String url) {
        try {
            // A more robust check for a valid URL start
            return url.toLowerCase().startsWith("http://") || url.toLowerCase().startsWith("https://");
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
