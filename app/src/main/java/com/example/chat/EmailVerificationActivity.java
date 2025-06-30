package com.example.chat;

import android.content.Intent;
import android.net.Uri;
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

public class EmailVerificationActivity extends AppCompatActivity {

    private TextView textViewEmailMessage, textViewInstruction;
    private MaterialButton buttonVerifyEmail, buttonResendEmail, buttonBackToLogin;
    private MaterialCardView verificationCard;
    private MaterialToolbar toolbar;

    private NetworkManager networkManager;
    private String email;
    private String emailVerifyToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);

        initViews();
        setupToolbar();
        setupAnimations();
        setupClickListeners();

        networkManager = NetworkManager.getInstance(this);

        // Get email from intent
        email = getIntent().getStringExtra("email");
        emailVerifyToken = getIntent().getStringExtra("email_verify_token");

        // Save email verify token to NetworkManager
        if (emailVerifyToken != null) {
            networkManager.saveEmailVerifyToken(emailVerifyToken);
        }

        setupEmailMessage();

        // Check if activity was opened from email link
        handleEmailVerificationLink();
    }

    private void initViews() {
        textViewEmailMessage = findViewById(R.id.textViewEmailMessage);
        textViewInstruction = findViewById(R.id.textViewInstruction);
        buttonVerifyEmail = findViewById(R.id.buttonVerifyEmail);
        buttonResendEmail = findViewById(R.id.buttonResendEmail);
        buttonBackToLogin = findViewById(R.id.buttonBackToLogin);
        verificationCard = findViewById(R.id.verificationCard);
        toolbar = findViewById(R.id.toolbarEmailVerification);
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
                navigateToLogin();
            }
        });
    }

    private void setupAnimations() {
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        verificationCard.startAnimation(slideUp);

        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        buttonVerifyEmail.startAnimation(fadeIn);
    }

    private void setupClickListeners() {
        buttonVerifyEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyEmail();
            }
        });

        buttonResendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resendVerificationEmail();
            }
        });

        buttonBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToLogin();
            }
        });
    }

    private void setupEmailMessage() {
        if (email != null) {
            textViewEmailMessage.setText("We've sent a verification link to " + email +
                    ". Please check your inbox and click the link to verify your account.");
        }
    }

    private void handleEmailVerificationLink() {
        // Check if the activity was opened from a verification link
        Intent intent = getIntent();
        Uri data = intent.getData();

        if (data != null) {
            // Extract token from the URL
            String token = data.getQueryParameter("token");
            if (token != null) {
                emailVerifyToken = token;
                networkManager.saveEmailVerifyToken(token);
                // Auto verify email
                verifyEmail();
            }
        }
    }

    private void verifyEmail() {
        String token = networkManager.getEmailVerifyToken();

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "No verification token found. Please try resending the email.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button and show loading
        buttonVerifyEmail.setEnabled(false);
        buttonVerifyEmail.setText("VERIFYING...");

        EmailVerificationRequest request = new EmailVerificationRequest(token);

        networkManager.getApiService().verifyEmail(request).enqueue(new ApiCallback<Object>() {
            @Override
            public void onSuccess(Object result, String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        buttonVerifyEmail.setEnabled(true);
                        buttonVerifyEmail.setText("VERIFY EMAIL");

                        Toast.makeText(EmailVerificationActivity.this,
                                "Email verified successfully!", Toast.LENGTH_SHORT).show();

                        // Clear the email verify token
                        networkManager.saveEmailVerifyToken(null);

                        // Navigate to login
                        Intent intent = new Intent(EmailVerificationActivity.this, LoginActivity.class);
                        intent.putExtra("email", email);
                        intent.putExtra("verified", true);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        finish();
                    }
                });
            }

            @Override
            public void onError(int statusCode, String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        buttonVerifyEmail.setEnabled(true);
                        buttonVerifyEmail.setText("VERIFY EMAIL");

                        if (statusCode == 204) {
                            Toast.makeText(EmailVerificationActivity.this,
                                    "Email already verified!", Toast.LENGTH_SHORT).show();
                            navigateToLogin();
                        } else {
                            Toast.makeText(EmailVerificationActivity.this,
                                    message, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }

            @Override
            public void onNetworkError(String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        buttonVerifyEmail.setEnabled(true);
                        buttonVerifyEmail.setText("VERIFY EMAIL");
                        Toast.makeText(EmailVerificationActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void resendVerificationEmail() {
        String accessToken = networkManager.getAuthorizationHeader();

        if (accessToken == null) {
            Toast.makeText(this, "Please register again to resend verification email.",
                    Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return;
        }

        // Disable button and show loading
        buttonResendEmail.setEnabled(false);
        buttonResendEmail.setText("SENDING...");

        networkManager.getApiService().resendVerificationEmail(accessToken)
                .enqueue(new ApiCallback<String>() {
                    @Override
                    public void onSuccess(String result, String message) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                buttonResendEmail.setEnabled(true);
                                buttonResendEmail.setText("RESEND EMAIL");

                                if (result != null) {
                                    networkManager.saveEmailVerifyToken(result);
                                }

                                Toast.makeText(EmailVerificationActivity.this,
                                        "Verification email sent successfully!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onError(int statusCode, String message) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                buttonResendEmail.setEnabled(true);
                                buttonResendEmail.setText("RESEND EMAIL");

                                if (statusCode == 204) {
                                    Toast.makeText(EmailVerificationActivity.this,
                                            "Email already verified!", Toast.LENGTH_SHORT).show();
                                    navigateToLogin();
                                } else {
                                    Toast.makeText(EmailVerificationActivity.this,
                                            message, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }

                    @Override
                    public void onNetworkError(String message) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                buttonResendEmail.setEnabled(true);
                                buttonResendEmail.setText("RESEND EMAIL");
                                Toast.makeText(EmailVerificationActivity.this, message, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(EmailVerificationActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }
}