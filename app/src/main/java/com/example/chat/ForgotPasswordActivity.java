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
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputEditText editTextEmail;
    private TextInputLayout emailInputLayout;
    private MaterialButton buttonSendResetLink, buttonBackToLogin;
    private TextView textViewBackToLogin, textViewSuccessMessage;
    private MaterialCardView forgotPasswordCard, successCard;
    private MaterialToolbar toolbar;

    private NetworkManager networkManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        initViews();
        setupToolbar();
        setupAnimations();
        setupClickListeners();

        networkManager = NetworkManager.getInstance(this);
    }

    private void initViews() {
        editTextEmail = findViewById(R.id.editTextEmail);
        emailInputLayout = findViewById(R.id.emailInputLayout);
        buttonSendResetLink = findViewById(R.id.buttonSendResetLink);
        buttonBackToLogin = findViewById(R.id.buttonBackToLogin);
        textViewBackToLogin = findViewById(R.id.textViewBackToLogin);
        textViewSuccessMessage = findViewById(R.id.textViewSuccessMessage);
        forgotPasswordCard = findViewById(R.id.forgotPasswordCard);
        successCard = findViewById(R.id.successCard);
        toolbar = findViewById(R.id.toolbarForgotPassword);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void setupAnimations() {
        // Slide up animation for the card
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        forgotPasswordCard.startAnimation(slideUp);

        // Fade in animation for button
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        buttonSendResetLink.startAnimation(fadeIn);
    }

    private void setupClickListeners() {
        // Send reset link button click listener
        buttonSendResetLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSendResetLink();
            }
        });

        // Back to login link click listener
        textViewBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Animation for click
                Animation scaleAnimation = AnimationUtils.loadAnimation(ForgotPasswordActivity.this, R.anim.scale_click);
                v.startAnimation(scaleAnimation);

                // Navigate back to login
                navigateToLogin();
            }
        });

        // Back to login button click listener (from success card)
        buttonBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToLogin();
            }
        });
    }

    private void handleSendResetLink() {
        String email = editTextEmail.getText().toString().trim();

        // Reset error state
        emailInputLayout.setError(null);

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

        // Disable button and show loading
        buttonSendResetLink.setEnabled(false);
        buttonSendResetLink.setText("SENDING...");

        // Call forgot password API
        performForgotPassword(email);
    }

    private void performForgotPassword(String email) {
        ForgotPasswordRequest request = new ForgotPasswordRequest(email);

        networkManager.getApiService().forgotPassword(request).enqueue(new ApiCallback<ForgotPasswordResponseData>() {
            @Override
            public void onSuccess(ForgotPasswordResponseData result, String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Reset button state
                        buttonSendResetLink.setEnabled(true);
                        buttonSendResetLink.setText("SEND RESET LINK");

                        // Show success message
                        showSuccessMessage(email);

                        // Optional: Save forgot password token for later use
                        if (result != null && result.getForgotPasswordToken() != null) {
                            // You can save this token if you want to implement direct reset in the app
                            // For now, we'll rely on email link
                        }
                    }
                });
            }

            @Override
            public void onError(int statusCode, String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Reset button state
                        buttonSendResetLink.setEnabled(true);
                        buttonSendResetLink.setText("SEND RESET LINK");

                        // Handle specific error cases
                        if (statusCode == 404) {
                            emailInputLayout.setError("No account found with this email address");
                            emailInputLayout.requestFocus();
                        } else {
                            Toast.makeText(ForgotPasswordActivity.this, message, Toast.LENGTH_LONG).show();
                        }

                        // Shake animation for error
                        Animation shake = AnimationUtils.loadAnimation(ForgotPasswordActivity.this, R.anim.shake);
                        forgotPasswordCard.startAnimation(shake);
                    }
                });
            }

            @Override
            public void onNetworkError(String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Reset button state
                        buttonSendResetLink.setEnabled(true);
                        buttonSendResetLink.setText("SEND RESET LINK");

                        Toast.makeText(ForgotPasswordActivity.this, message, Toast.LENGTH_LONG).show();

                        // Shake animation for error
                        Animation shake = AnimationUtils.loadAnimation(ForgotPasswordActivity.this, R.anim.shake);
                        forgotPasswordCard.startAnimation(shake);
                    }
                });
            }
        });
    }

    private void showSuccessMessage(String email) {
        // Hide the forgot password card with fade out animation
        Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        forgotPasswordCard.startAnimation(fadeOut);

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                forgotPasswordCard.setVisibility(View.GONE);

                // Update success message with email
                textViewSuccessMessage.setText(
                        "We've sent a password reset link to " + email +
                                ". Please check your inbox and follow the instructions to reset your password."
                );

                // Show success card with fade in animation
                successCard.setVisibility(View.VISIBLE);
                Animation fadeIn = AnimationUtils.loadAnimation(ForgotPasswordActivity.this, R.anim.fade_in);
                successCard.startAnimation(fadeIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        // Show toast message
        Toast.makeText(this, "Reset link sent successfully!", Toast.LENGTH_SHORT).show();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}