package com.example.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chat.models.*;
import com.example.chat.network.ApiCallback;
import com.example.chat.network.NetworkManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText editTextEmail, editTextPassword;
    private TextInputLayout emailInputLayout, passwordInputLayout;
    private MaterialButton buttonLogin;
    private TextView textViewRegisterLink, textViewForgotPassword;
    private MaterialCardView loginCard;

    private NetworkManager networkManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        setupAnimations();
        setupClickListeners();

        networkManager = NetworkManager.getInstance(this);

        // Pre-fill email if passed from registration or verification
        String email = getIntent().getStringExtra("email");
        if (email != null) {
            editTextEmail.setText(email);
        }

        // Show success message if coming from successful registration
        boolean verified = getIntent().getBooleanExtra("verified", false);
        if (verified) {
            Toast.makeText(this, "Email verified successfully! You can now login.",
                    Toast.LENGTH_LONG).show();
        }

        // Check if user is already logged in
        if (networkManager.isLoggedIn()) {
            navigateToMain();
        }
    }

    private void initViews() {
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewRegisterLink = findViewById(R.id.textViewRegisterLink);
        textViewForgotPassword = findViewById(R.id.textViewForgotPassword);
        loginCard = findViewById(R.id.loginCard);
    }

    private void setupAnimations() {
        // Slide up animation for the card
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        loginCard.startAnimation(slideUp);

        // Fade in animation for elements
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        buttonLogin.startAnimation(fadeIn);
    }

    private void setupClickListeners() {
        // Login button click listener
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogin();
            }
        });

        // Register link click listener
        textViewRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Animation for click
                Animation scaleAnimation = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.scale_click);
                v.startAnimation(scaleAnimation);

                // Navigate to register
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        // Forgot password click listener
        textViewForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }

    private void handleLogin() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Reset error states
        emailInputLayout.setError(null);
        passwordInputLayout.setError(null);

        // Validate input
        if (email.isEmpty()) {
            emailInputLayout.setError("Email is required");
            emailInputLayout.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.setError("Please enter a valid email");
            emailInputLayout.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordInputLayout.setError("Password is required");
            passwordInputLayout.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordInputLayout.setError("Password must be at least 6 characters");
            passwordInputLayout.requestFocus();
            return;
        }

        // Disable button and show loading
        buttonLogin.setEnabled(false);
        buttonLogin.setText("LOGGING IN...");

        // Call login API
        performLogin(email, password);
    }

    private void performLogin(String email, String password) {
        LoginRequest request = new LoginRequest(email, password);

        networkManager.getApiService().login(request).enqueue(new ApiCallback<LoginResponseData>() {
            @Override
            public void onSuccess(LoginResponseData result, String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Reset button state
                        buttonLogin.setEnabled(true);
                        buttonLogin.setText("LOGIN");

                        // Save tokens
                        networkManager.saveTokens(result.getAccessToken(), result.getRefreshToken());

                        // Show success message
                        Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();

                        // Navigate to main activity
                        navigateToMain();
                    }
                });
            }

            @Override
            public void onError(int statusCode, String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Reset button state
                        buttonLogin.setEnabled(true);
                        buttonLogin.setText("LOGIN");

                        // Show error message
                        if (statusCode == 404) {
                            Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                        } else if (statusCode == 403) {
                            // User not verified
                            Toast.makeText(LoginActivity.this, "Please verify your email first", Toast.LENGTH_LONG).show();

                            // Navigate to email verification
                            Intent intent = new Intent(LoginActivity.this, EmailVerificationActivity.class);
                            intent.putExtra("email", email);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        } else {
                            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                        }

                        // Shake animation for error
                        Animation shake = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.shake);
                        loginCard.startAnimation(shake);
                    }
                });
            }

            @Override
            public void onNetworkError(String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Reset button state
                        buttonLogin.setEnabled(true);
                        buttonLogin.setText("LOGIN");

                        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();

                        // Shake animation for error
                        Animation shake = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.shake);
                        loginCard.startAnimation(shake);
                    }
                });
            }
        });
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}