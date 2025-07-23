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

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chat.models.ApiResponse;
import com.example.chat.models.LoginRequest;
import com.example.chat.models.LoginResponseData;
import com.example.chat.models.RegisterRequest;
import com.example.chat.models.RegisterResponseData;
import com.example.chat.models.User;
import com.example.chat.network.ApiCallback;
import com.example.chat.network.NetworkManager;
import com.example.chat.utils.AndroidUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

    String phoneNumber;

    private TextView resendOtpText;
    ProgressBar loginProcess;

    String verificationCode;
    PhoneAuthProvider.ForceResendingToken token;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private NetworkManager networkManager;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        try {
            setContentView(R.layout.activity_phone_login_otp);
            initViews();
            setupToolbar();
            setupClickListeners();
            sendOtp(configPhoneNumber(phoneNumber), false);

            networkManager = NetworkManager.getInstance(this);
        } catch (Exception e) {
            Toast.makeText(this, "App initialization failed", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private String configPhoneNumber(String phoneNumber) {
        phoneNumber = getIntent().getStringExtra("phoneNumber");
        String newPhoneNum = "+84" + phoneNumber.substring(1);
        return newPhoneNum;
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
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupClickListeners() {
        if (buttonLoginLink != null) {
            buttonLoginLink.setOnClickListener(v -> {
                try {
                    Animation scaleAnimation = AnimationUtils.loadAnimation(PhoneLoginOtpActivity.this, R.anim.scale_click);
                    v.startAnimation(scaleAnimation);

                    Intent intent = new Intent(PhoneLoginOtpActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    finish();
                } catch (Exception e) {
                    android.util.Log.e("RegisterActivity", "Error navigating to login", e);
                }
            });
        }

        nextButton.setOnClickListener(v -> {
            String enteredOTP = otpCodeInput.getText().toString();
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCode, enteredOTP);
            signIn(credential);
        });

        startOtpCountdown(resendOtpText);
    }

    void sendOtp(String phoneNumber, boolean isResend) {
        PhoneAuthOptions.Builder builder = PhoneAuthOptions.newBuilder(mAuth);
        builder.setPhoneNumber(phoneNumber);
        builder.setTimeout(60L, TimeUnit.SECONDS);
        builder.setActivity(PhoneLoginOtpActivity.this);
        builder.setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signIn(phoneAuthCredential);
                setInProgress(false);
            }

            @SuppressLint("UnsafeIntentLaunch")
            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                AndroidUtil.showToast(PhoneLoginOtpActivity.this, "OTP verification failed");
                setInProgress(false);
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                verificationCode = s;
                token = forceResendingToken;
                AndroidUtil.showToast(PhoneLoginOtpActivity.this, "OTP sent successfully");
                setInProgress(false);
            }
        });

        if (isResend) {
            PhoneAuthProvider.verifyPhoneNumber(builder.setForceResendingToken(token).build());
        } else {
            PhoneAuthProvider.verifyPhoneNumber(builder.build());
        }
    }

    void signIn(PhoneAuthCredential phoneAuthCredential) {
        // login and go to next activity
        mAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    performRegistration(configPhoneToMail(phoneNumber), phoneNumber, phoneNumber, phoneNumber, "31/10/2003");
                    AndroidUtil.showToast(getApplicationContext(), "Verified OTP successful! Redirecting...");
                } else {
                    AndroidUtil.showToast(getApplicationContext(), "OTP wrong! Please try again");
                }
            }
        });
    }

    private String configPhoneToMail(String phoneNumber) {
        String newPhoneMail = phoneNumber.substring(1) + "@gmail.com";
        return newPhoneMail;
    }

    private void performRegistration(String email, String username, String password,
                                     String confirmPassword, String dateOfBirth) {
        RegisterRequest request = new RegisterRequest(
                email,
                username,
                password,
                confirmPassword,
                dateOfBirth
        );

        // Log the full request for debugging
        Log.d("RegisterActivity", "Making register API call...");

        networkManager.getApiService().register(request).enqueue(new ApiCallback<RegisterResponseData>() {
            @Override
            public void onSuccess(RegisterResponseData result, String message) {
                Intent intent = new Intent(PhoneLoginOtpActivity.this, WelcomeUserActivity.class);
                intent.putExtra("username", phoneNumber);
                intent.putExtra("isRegister", false);
                startActivity(intent);
            }

            @Override
            public void onError(int statusCode, String message) {
                runOnUiThread(() -> {
                    Log.e("RegisterActivity", "Register error - Status: " + statusCode + ", Message: " + message);

                    // Handle specific error cases
                    if (message.toLowerCase().contains("email is already exists") ||
                            message.toLowerCase().contains("email already exists") ||
                            message.toLowerCase().contains("email")) {
                        Intent intent = new Intent(PhoneLoginOtpActivity.this, WelcomeUserActivity.class);
                        intent.putExtra("username", phoneNumber);
                        intent.putExtra("isRegister", true);
                        startActivity(intent);
                    } else {
                        Toast.makeText(PhoneLoginOtpActivity.this, "Registration failed: " + message, Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onNetworkError(String message) {
                runOnUiThread(() -> {
                    Log.e("RegisterActivity", "Network error: " + message);
                    Toast.makeText(PhoneLoginOtpActivity.this, "Network error: " + message, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    void setInProgress(boolean inProgress) {
        runOnUiThread(() -> {
            if (inProgress) {
                loginProcess.setVisibility(View.VISIBLE);
                nextButton.setVisibility(View.INVISIBLE);
            } else {
                loginProcess.setVisibility(View.INVISIBLE);
                nextButton.setVisibility(View.VISIBLE);
            }
        });
    }

    private void startOtpCountdown(TextView resendOtpText) {
        resendOtpText.setClickable(false);

        new CountDownTimer(30000, 1000) {
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                resendOtpText.setText("Resend OTP in " + seconds + "s");
            }

            public void onFinish() {
                resendOtpText.setText("Resend OTP");
                resendOtpText.setClickable(true);
                resendOtpText.setOnClickListener(v -> {
                    sendOtp(configPhoneNumber(phoneNumber), true);
                    startOtpCountdown(resendOtpText);
                });
            }
        }.start();
    }

}