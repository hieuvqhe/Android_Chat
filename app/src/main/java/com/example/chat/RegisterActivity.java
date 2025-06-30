package com.example.chat;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chat.models.*;
import com.example.chat.network.ApiCallback;
import com.example.chat.network.NetworkManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText editTextUsername, editTextEmail,
            editTextPassword, editTextConfirmPassword, editTextDateOfBirth;
    private TextInputLayout usernameInputLayout, emailInputLayout,
            passwordInputLayout, confirmPasswordInputLayout, dateOfBirthInputLayout;
    private MaterialButton buttonRegister;
    private TextView textViewLoginLink, textViewTerms;
    private MaterialCheckBox checkBoxTerms;
    private MaterialCardView registerCard;
    private MaterialToolbar toolbar;

    private NetworkManager networkManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        setupToolbar();
        setupAnimations();
        setupClickListeners();

        networkManager = NetworkManager.getInstance(this);
    }

    private void initViews() {
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        editTextDateOfBirth = findViewById(R.id.editTextDateOfBirth);

        usernameInputLayout = findViewById(R.id.usernameInputLayout);
        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        confirmPasswordInputLayout = findViewById(R.id.confirmPasswordInputLayout);
        dateOfBirthInputLayout = findViewById(R.id.dateOfBirthInputLayout);

        buttonRegister = findViewById(R.id.buttonRegister);
        textViewLoginLink = findViewById(R.id.textViewLoginLink);
        textViewTerms = findViewById(R.id.textViewTerms);
        checkBoxTerms = findViewById(R.id.checkBoxTerms);
        registerCard = findViewById(R.id.registerCard);
        toolbar = findViewById(R.id.toolbarRegister);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupAnimations() {
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        registerCard.startAnimation(slideUp);

        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        buttonRegister.startAnimation(fadeIn);
    }

    private void setupClickListeners() {
        buttonRegister.setOnClickListener(v -> handleRegister());

        textViewLoginLink.setOnClickListener(v -> {
            Animation scaleAnimation = AnimationUtils.loadAnimation(RegisterActivity.this, R.anim.scale_click);
            v.startAnimation(scaleAnimation);

            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finish();
        });

        textViewTerms.setOnClickListener(v -> {
            Animation scaleAnimation = AnimationUtils.loadAnimation(RegisterActivity.this, R.anim.scale_click);
            v.startAnimation(scaleAnimation);
            showTermsAndConditions();
        });

        editTextDateOfBirth.setOnClickListener(v -> showDatePickerDialog());
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, monthOfYear, dayOfMonth) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year1, monthOfYear, dayOfMonth);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String formattedDate = dateFormat.format(selectedDate.getTime());
            editTextDateOfBirth.setText(formattedDate);
            dateOfBirthInputLayout.setError(null);
        }, year, month, day);

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void handleRegister() {
        String username = editTextUsername.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String dateOfBirth = editTextDateOfBirth.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        // Reset all error states
        usernameInputLayout.setError(null);
        emailInputLayout.setError(null);
        dateOfBirthInputLayout.setError(null);
        passwordInputLayout.setError(null);
        confirmPasswordInputLayout.setError(null);

        // Validate username
        if (username.isEmpty()) {
            usernameInputLayout.setError("Username is required");
            usernameInputLayout.requestFocus();
            return;
        }

        if (username.length() < 3) {
            usernameInputLayout.setError("Username must be at least 3 characters");
            usernameInputLayout.requestFocus();
            return;
        }

        if (!isValidUsername(username)) {
            usernameInputLayout.setError("Username can only contain letters, numbers and underscore");
            usernameInputLayout.requestFocus();
            return;
        }

        // Validate email
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

        // Validate Date of Birth
        if (dateOfBirth.isEmpty()) {
            dateOfBirthInputLayout.setError("Date of birth is required");
            dateOfBirthInputLayout.requestFocus();
            return;
        }

        // Validate password
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

        if (!isValidPassword(password)) {
            passwordInputLayout.setError("Password must contain at least 1 letter, 1 number and 1 special character");
            passwordInputLayout.requestFocus();
            return;
        }

        // Validate confirm password
        if (confirmPassword.isEmpty()) {
            confirmPasswordInputLayout.setError("Please confirm your password");
            confirmPasswordInputLayout.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordInputLayout.setError("Passwords do not match");
            confirmPasswordInputLayout.requestFocus();
            return;
        }

        // Check if terms and conditions are accepted
        if (!checkBoxTerms.isChecked()) {
            Toast.makeText(this, "Please accept the Terms & Conditions", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button and show loading
        buttonRegister.setEnabled(false);
        buttonRegister.setText("CREATING ACCOUNT...");

        // Log the request data for debugging
        Log.d("RegisterActivity", "Register request data:");
        Log.d("RegisterActivity", "Email: " + email);
        Log.d("RegisterActivity", "Username: " + username);
        Log.d("RegisterActivity", "Date of birth: " + dateOfBirth);
        Log.d("RegisterActivity", "Password length: " + password.length());

        // Call register API
        performRegistration(email, username, password, confirmPassword, dateOfBirth);
    }

    private boolean isValidUsername(String username) {
        // Username can only contain letters, numbers and underscore
        return username.matches("^[a-zA-Z0-9_]+$");
    }

    private boolean isValidPassword(String password) {
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasNumber = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");

        return hasLetter && hasNumber && hasSpecial;
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
                runOnUiThread(() -> {
                    Log.d("RegisterActivity", "Register success: " + message);
                    buttonRegister.setEnabled(true);
                    buttonRegister.setText("CREATE ACCOUNT");

                    Toast.makeText(RegisterActivity.this,
                            "Account created successfully! You can now login with your credentials.",
                            Toast.LENGTH_LONG).show();

                    // Navigate to LoginActivity with the registered email
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    intent.putExtra("email", email);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    finish();
                });
            }

            @Override
            public void onError(int statusCode, String message) {
                runOnUiThread(() -> {
                    Log.e("RegisterActivity", "Register error - Status: " + statusCode + ", Message: " + message);
                    buttonRegister.setEnabled(true);
                    buttonRegister.setText("CREATE ACCOUNT");

                    // Handle specific error cases
                    if (message.toLowerCase().contains("email is already exists") ||
                            message.toLowerCase().contains("email already exists") ||
                            message.toLowerCase().contains("email")) {
                        emailInputLayout.setError("This email is already registered");
                        emailInputLayout.requestFocus();
                    } else if (message.toLowerCase().contains("username is already exists") ||
                            message.toLowerCase().contains("username already exists") ||
                            message.toLowerCase().contains("username")) {
                        usernameInputLayout.setError("This username is already taken");
                        usernameInputLayout.requestFocus();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration failed: " + message, Toast.LENGTH_LONG).show();
                    }

                    Animation shake = AnimationUtils.loadAnimation(RegisterActivity.this, R.anim.shake);
                    registerCard.startAnimation(shake);
                });
            }

            @Override
            public void onNetworkError(String message) {
                runOnUiThread(() -> {
                    Log.e("RegisterActivity", "Network error: " + message);
                    buttonRegister.setEnabled(true);
                    buttonRegister.setText("CREATE ACCOUNT");
                    Toast.makeText(RegisterActivity.this, "Network error: " + message, Toast.LENGTH_LONG).show();

                    Animation shake = AnimationUtils.loadAnimation(RegisterActivity.this, R.anim.shake);
                    registerCard.startAnimation(shake);
                });
            }
        });
    }

    private void showTermsAndConditions() {
        Toast.makeText(this, "Terms & Conditions: By using this app, you agree to our terms of service and privacy policy.",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}