package com.example.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

public class PhoneLoginActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private MaterialButton buttonLoginLink, sendOtpButton;

    private EditText phoneNumInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);


        try {
            setContentView(R.layout.activity_phone_login);
            initViews();
            setupToolbar();
            setupClickListeners();

        } catch (Exception e) {
            Toast.makeText(this, "App initialization failed", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbarRegister);
        buttonLoginLink = findViewById(R.id.buttonLoginLink);
        phoneNumInput = findViewById(R.id.phone_number_input);
        sendOtpButton = findViewById(R.id.sendOtpButton);
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
                    Animation scaleAnimation = AnimationUtils.loadAnimation(PhoneLoginActivity.this, R.anim.scale_click);
                    v.startAnimation(scaleAnimation);

                    Intent intent = new Intent(PhoneLoginActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    finish();
                } catch (Exception e) {
                    android.util.Log.e("RegisterActivity", "Error navigating to login", e);
                }
            });
        }

        if (sendOtpButton != null) {
            sendOtpButton.setOnClickListener(v -> {
                try {

                    String phoneNumber = phoneNumInput.getText().toString().trim();

                    String vietnamPhonePattern = "^(03|05|07|08|09)\\d{8}$";

                    if (!phoneNumber.matches(vietnamPhonePattern)) {
                        phoneNumInput.setError("Phone number not valid");
                        return;
                    }

                    Intent intent = new Intent(PhoneLoginActivity.this, PhoneLoginOtpActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("phoneNumber", phoneNumInput.getText().toString());
                    startActivity(intent);
                } catch (Exception e) {
                    android.util.Log.e("RegisterActivity", "Error navigating to login", e);
                }
            });
        }

    }

}