package com.example.chat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.example.chat.models.*;
import com.example.chat.network.ApiCallback;
import com.example.chat.network.NetworkManager;
import retrofit2.Call;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private TextInputEditText editTextEmail, editTextPassword;
    private MaterialButton buttonLogin, buttonRegisterLink, buttonForgotPassword;
    private NetworkManager networkManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            Log.d(TAG, "Starting onCreate");
            setContentView(R.layout.activity_login);
            Log.d(TAG, "Layout set successfully");
            
            initViews();
            Log.d(TAG, "Views initialized successfully");
            
            setupClickListeners();
            Log.d(TAG, "Click listeners setup successfully");

            // Initialize NetworkManager
            try {
                networkManager = NetworkManager.getInstance(this);
                Log.d(TAG, "NetworkManager initialized successfully");
            } catch (Exception e) {
                Log.e(TAG, "Failed to initialize NetworkManager", e);
                Toast.makeText(this, "Network initialization failed", Toast.LENGTH_SHORT).show();
            }

            Log.d(TAG, "onCreate completed successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Critical error in onCreate", e);
            Toast.makeText(this, "App initialization failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews() {
        try {
            Log.d(TAG, "Initializing views...");
            
            editTextEmail = findViewById(R.id.editTextEmail);
            Log.d(TAG, "editTextEmail: " + (editTextEmail != null ? "found" : "NULL"));
            
            editTextPassword = findViewById(R.id.editTextPassword);
            Log.d(TAG, "editTextPassword: " + (editTextPassword != null ? "found" : "NULL"));
            
            buttonLogin = findViewById(R.id.buttonLogin);
            Log.d(TAG, "buttonLogin: " + (buttonLogin != null ? "found" : "NULL"));
            
            buttonRegisterLink = findViewById(R.id.buttonRegisterLink);
            Log.d(TAG, "buttonRegisterLink: " + (buttonRegisterLink != null ? "found" : "NULL"));
            
            buttonForgotPassword = findViewById(R.id.buttonForgotPassword);
            Log.d(TAG, "buttonForgotPassword: " + (buttonForgotPassword != null ? "found" : "NULL"));
            
            // Check for critical views
            if (editTextEmail == null) {
                throw new RuntimeException("editTextEmail not found in layout");
            }
            if (editTextPassword == null) {
                throw new RuntimeException("editTextPassword not found in layout");
            }
            if (buttonLogin == null) {
                throw new RuntimeException("buttonLogin not found in layout");
            }
            
            Log.d(TAG, "All critical views found successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize views", e);
            throw e;
        }
    }

    private void setupClickListeners() {
        try {
            Log.d(TAG, "Setting up click listeners...");
            
            if (buttonLogin != null) {
                buttonLogin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "Login button clicked");
                        handleLogin();
                    }
                });
                Log.d(TAG, "Login button listener set");
            }

            if (buttonRegisterLink != null) {
                buttonRegisterLink.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "Register link clicked");
                        try {
                            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                            startActivity(intent);
                        } catch (Exception e) {
                            Log.e(TAG, "Error navigating to register", e);
                            Toast.makeText(LoginActivity.this, "Navigation error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                Log.d(TAG, "Register link listener set");
            }

            if (buttonForgotPassword != null) {
                buttonForgotPassword.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "Forgot password clicked");
                        try {
                            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                            startActivity(intent);
                        } catch (Exception e) {
                            Log.e(TAG, "Error navigating to forgot password", e);
                            Toast.makeText(LoginActivity.this, "Navigation error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                Log.d(TAG, "Forgot password listener set");
            }
            
            Log.d(TAG, "All click listeners setup successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to setup click listeners", e);
            throw e;
        }
    }

    private void handleLogin() {
        try {
            Log.d(TAG, "Handling login...");

            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            Log.d(TAG, "Email: " + email);
            Log.d(TAG, "Password length: " + password.length());

            // Validate input
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.isEmpty()) {
                Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
                return;
            }

            if (networkManager == null) {
                Toast.makeText(this, "Network not available", Toast.LENGTH_SHORT).show();
                return;
            }

            // Disable login button to prevent multiple requests
            if (buttonLogin != null) {
                buttonLogin.setEnabled(false);
                buttonLogin.setText("Logging in...");
            }

            // Create login request
            LoginRequest loginRequest = new LoginRequest(email, password);

            // Make API call
            Call<ApiResponse<LoginResponseData>> call = networkManager.getApiService().login(loginRequest);
            call.enqueue(new ApiCallback<LoginResponseData>() {
                @Override
                public void onSuccess(LoginResponseData data, String message) {
                    Log.d(TAG, "Login successful: " + message);

                    runOnUiThread(() -> {
                        try {
                            // Save tokens
                            if (data != null && data.getAccessToken() != null && data.getRefreshToken() != null) {
                                networkManager.saveTokens(data.getAccessToken(), data.getRefreshToken());

                                // Get user profile to save user ID
                                getUserProfile();
                            } else {
                                onError(400, "Invalid login response");
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing login response", e);
                            onError(500, "Login processing failed");
                        }
                    });
                }

                @Override
                public void onError(int statusCode, String error) {
                    Log.e(TAG, "Login failed: " + error + " (Status: " + statusCode + ")");

                    runOnUiThread(() -> {
                        // Re-enable login button
                        if (buttonLogin != null) {
                            buttonLogin.setEnabled(true);
                            buttonLogin.setText("Login");
                        }

                        Toast.makeText(LoginActivity.this, "Login failed: " + error, Toast.LENGTH_LONG).show();
                    });
                }

                @Override
                public void onNetworkError(String error) {
                    Log.e(TAG, "Login network error: " + error);

                    runOnUiThread(() -> {
                        // Re-enable login button
                        if (buttonLogin != null) {
                            buttonLogin.setEnabled(true);
                            buttonLogin.setText("Login");
                        }

                        Toast.makeText(LoginActivity.this, "Network error: " + error, Toast.LENGTH_LONG).show();
                    });
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error in handleLogin", e);

            // Re-enable login button
            if (buttonLogin != null) {
                buttonLogin.setEnabled(true);
                buttonLogin.setText("Login");
            }

            Toast.makeText(this, "Login error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

                            Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                        navigateToMainActivity();
                    });
                }

                @Override
                public void onNetworkError(String error) {
                    Log.e(TAG, "Network error getting user profile: " + error);

                    runOnUiThread(() -> {
                        // Still navigate to main even if user profile fails
                        Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
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
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to MainActivity", e);
            Toast.makeText(this, "Navigation error", Toast.LENGTH_SHORT).show();
        }
    }
}
