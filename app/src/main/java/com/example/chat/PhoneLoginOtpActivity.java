package com.example.chat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chat.models.ApiResponse;
import com.example.chat.models.LoginRequest;
import com.example.chat.models.LoginResponseData;
import com.example.chat.models.RegisterRequest;
import com.example.chat.models.RegisterResponseData;
import com.example.chat.network.ApiCallback;
import com.example.chat.network.NetworkManager;
import com.example.chat.utils.AndroidUtil;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import retrofit2.Call;

public class PhoneLoginOtpActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private MaterialButton buttonLoginLink, nextButton;
    private EditText otpCodeInput;
    private TextView resendOtpText;
    private ProgressBar loginProcess;

    private String phoneNumber;
    private String verificationCode;
    private PhoneAuthProvider.ForceResendingToken token;
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private NetworkManager networkManager;

    private static final String TAG = "PhoneOTPLogin";

    private boolean isRegistered = false;
    private boolean isProcessingAuth = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login_otp);

        phoneNumber = getIntent().getStringExtra("phoneNumber");
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            AndroidUtil.showToast(this, "Invalid phone number");
            finish();
            return;
        }

        networkManager = NetworkManager.getInstance(this);

        initViews();
        setupToolbar();
        setupClickListeners();

        sendOtp(formatPhoneNumber(phoneNumber), false);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbarRegister);
        buttonLoginLink = findViewById(R.id.buttonLoginLink);
        nextButton = findViewById(R.id.sendOtpButton);
        otpCodeInput = findViewById(R.id.phone_number_input);
        resendOtpText = findViewById(R.id.resend_otp_text);
        loginProcess = findViewById(R.id.login_process_otp);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupClickListeners() {
        buttonLoginLink.setOnClickListener(v -> {
            if (isProcessingAuth) return; // Prevent multiple clicks during processing

            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_click));
            startActivity(new Intent(this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finish();
        });

        nextButton.setOnClickListener(v -> {
            if (isProcessingAuth) return;

            String enteredOTP = otpCodeInput.getText().toString().trim();
            if (enteredOTP.isEmpty()) {
                AndroidUtil.showToast(this, "Please enter OTP code");
                return;
            }

            if (verificationCode == null) {
                AndroidUtil.showToast(this, "Verification code not received. Please try again.");
                return;
            }

            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCode, enteredOTP);
            signIn(credential);
        });

        startOtpCountdown();
    }

    private String formatPhoneNumber(String phoneNumber) {
        return "+84" + phoneNumber.substring(1);
    }

    private void sendOtp(String formattedPhone, boolean isResend) {
        setInProgress(true);
        PhoneAuthOptions.Builder builder = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(formattedPhone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        Log.d(TAG, "Auto verification completed");
                        signIn(credential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Log.e(TAG, "OTP verification failed: " + e.getMessage());
                        AndroidUtil.showToast(PhoneLoginOtpActivity.this, "OTP verification failed: " + e.getMessage());
                        setInProgress(false);
                    }

                    @Override
                    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken resendingToken) {
                        Log.d(TAG, "OTP code sent successfully");
                        verificationCode = s;
                        token = resendingToken;
                        AndroidUtil.showToast(PhoneLoginOtpActivity.this, "OTP sent successfully");
                        setInProgress(false);
                    }
                });

        if (isResend && token != null) {
            builder.setForceResendingToken(token);
        }

        PhoneAuthProvider.verifyPhoneNumber(builder.build());
    }

    private void signIn(PhoneAuthCredential credential) {
        if (isProcessingAuth) return;

        isProcessingAuth = true;
        setInProgress(true);

        mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Firebase authentication successful");
                AndroidUtil.showToast(this, "OTP verified successfully!");

                // Try login first to check if user exists
                attemptLogin();

            } else {
                Log.e(TAG, "Firebase authentication failed: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                AndroidUtil.showToast(this, "Invalid OTP! Please try again");
                isProcessingAuth = false;
                setInProgress(false);
            }
        });
    }

    private String convertPhoneToEmail(String phone) {
        return "xamPle" + phone.substring(1) + "@gmail.com";
    }

    private void attemptLogin() {
        String email = convertPhoneToEmail(phoneNumber);
        String password = "Sondaigia123@";

        LoginRequest loginRequest = new LoginRequest(email, password);

        networkManager.getApiService().login(loginRequest).enqueue(new ApiCallback<LoginResponseData>() {
            @Override
            public void onSuccess(LoginResponseData data, String message) {
                Log.d(TAG, "Login successful - user exists: " + message);

                runOnUiThread(() -> {
                    if (data != null && data.getAccessToken() != null && data.getRefreshToken() != null) {
                        networkManager.saveTokens(data.getAccessToken(), data.getRefreshToken());
                        isRegistered = true; // User already exists
                        navigateToWelcome();
                    } else {
                        AndroidUtil.showToast(PhoneLoginOtpActivity.this, "Invalid login response");
                        isProcessingAuth = false;
                        setInProgress(false);
                    }
                });
            }

            @Override
            public void onError(int statusCode, String error) {
                Log.d(TAG, "Login failed - user doesn't exist, attempting registration: " + error);

                // User doesn't exist, try to register
                if (statusCode == 401 || statusCode == 404) {
                    performRegistration();
                } else {
                    runOnUiThread(() -> {
                        AndroidUtil.showToast(PhoneLoginOtpActivity.this, "Login failed: " + error);
                        isProcessingAuth = false;
                        setInProgress(false);
                    });
                }
            }

            @Override
            public void onNetworkError(String error) {
                Log.e(TAG, "Login network error: " + error);
                runOnUiThread(() -> {
                    AndroidUtil.showToast(PhoneLoginOtpActivity.this, "Network error: " + error);
                    isProcessingAuth = false;
                    setInProgress(false);
                });
            }
        });
    }

    private void performRegistration() {
        String email = convertPhoneToEmail(phoneNumber);
        RegisterRequest request = new RegisterRequest(
                email,
                email,
                "Sondaigia123@",
                "Sondaigia123@",
                "2003-10-31"
        );

        networkManager.getApiService().register(request).enqueue(new ApiCallback<RegisterResponseData>() {
            @Override
            public void onSuccess(RegisterResponseData result, String message) {
                Log.d(TAG, "Registration successful: " + message);

                // After successful registration, login
                runOnUiThread(() -> {
                    isRegistered = false; // New user
                    performLoginAfterRegistration();
                });
            }

            @Override
            public void onError(int statusCode, String message) {
                Log.e(TAG, "Registration failed: " + message + " (Status: " + statusCode + ")");

                runOnUiThread(() -> {
                    if (statusCode == 409) {
                        // User already exists, try login again
                        isRegistered = true;
                        performLoginAfterRegistration();
                    } else {
                        AndroidUtil.showToast(PhoneLoginOtpActivity.this, "Registration failed: " + message);
                        isProcessingAuth = false;
                        setInProgress(false);
                    }
                });
            }

            @Override
            public void onNetworkError(String message) {
                Log.e(TAG, "Registration network error: " + message);
                runOnUiThread(() -> {
                    AndroidUtil.showToast(PhoneLoginOtpActivity.this, "Network error: " + message);
                    isProcessingAuth = false;
                    setInProgress(false);
                });
            }
        });
    }

    private void performLoginAfterRegistration() {
        String email = convertPhoneToEmail(phoneNumber);
        String password = "Sondaigia123@";

        LoginRequest loginRequest = new LoginRequest(email, password);

        networkManager.getApiService().login(loginRequest).enqueue(new ApiCallback<LoginResponseData>() {
            @Override
            public void onSuccess(LoginResponseData data, String message) {
                Log.d(TAG, "Login after registration successful: " + message);

                runOnUiThread(() -> {
                    if (data != null && data.getAccessToken() != null && data.getRefreshToken() != null) {
                        networkManager.saveTokens(data.getAccessToken(), data.getRefreshToken());
                        navigateToWelcome();
                    } else {
                        AndroidUtil.showToast(PhoneLoginOtpActivity.this, "Invalid login response");
                        isProcessingAuth = false;
                        setInProgress(false);
                    }
                });
            }

            @Override
            public void onError(int statusCode, String error) {
                Log.e(TAG, "Login after registration failed: " + error + " (Status: " + statusCode + ")");
                runOnUiThread(() -> {
                    AndroidUtil.showToast(PhoneLoginOtpActivity.this, "Login failed: " + error);
                    isProcessingAuth = false;
                    setInProgress(false);
                });
            }

            @Override
            public void onNetworkError(String error) {
                Log.e(TAG, "Login after registration network error: " + error);
                runOnUiThread(() -> {
                    AndroidUtil.showToast(PhoneLoginOtpActivity.this, "Network error: " + error);
                    isProcessingAuth = false;
                    setInProgress(false);
                });
            }
        });
    }

    private void navigateToWelcome() {
        Intent intent = new Intent(PhoneLoginOtpActivity.this, WelcomeUserActivity.class);
        intent.putExtra("isRegistered", isRegistered);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void startOtpCountdown() {
        resendOtpText.setClickable(false);

        new CountDownTimer(30000, 1000) {
            public void onTick(long millisUntilFinished) {
                resendOtpText.setText("Resend OTP in " + (millisUntilFinished / 1000) + "s");
            }

            public void onFinish() {
                resendOtpText.setText("Resend OTP");
                resendOtpText.setClickable(true);
                resendOtpText.setOnClickListener(v -> {
                    if (!isProcessingAuth) {
                        sendOtp(formatPhoneNumber(phoneNumber), true);
                        startOtpCountdown();
                    }
                });
            }
        }.start();
    }

    private void setInProgress(boolean inProgress) {
        runOnUiThread(() -> {
            loginProcess.setVisibility(inProgress ? View.VISIBLE : View.INVISIBLE);
            nextButton.setVisibility(inProgress ? View.INVISIBLE : View.VISIBLE);
            nextButton.setEnabled(!inProgress);
            buttonLoginLink.setEnabled(!inProgress);
        });
    }
}