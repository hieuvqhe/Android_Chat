package com.example.chat.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.chat.EditProfileActivity;
import com.example.chat.LoginActivity;
import com.example.chat.R;
import com.example.chat.models.LogoutRequest;
import com.example.chat.models.User;
import com.example.chat.network.ApiCallback;
import com.example.chat.network.NetworkManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";

    // Views
    private ImageView imageViewAvatar;
    private TextView textViewUsername;
    private TextView textViewEmail;
    private TextView textViewBio;
    private TextView textViewLocation;
    private TextView textViewWebsite;
    private MaterialCardView cardEditProfile;
    private MaterialCardView cardSettings;
    private MaterialCardView cardAbout;
    private MaterialButton buttonLogout;

    // Services
    private NetworkManager networkManager;

    // Data
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initServices();
        setupClickListeners();
        loadUserInfo();
    }

    private void initViews(View view) {
        imageViewAvatar = view.findViewById(R.id.imageViewAvatar);
        textViewUsername = view.findViewById(R.id.textViewUsername);
        textViewEmail = view.findViewById(R.id.textViewEmail);
        textViewBio = view.findViewById(R.id.textViewBio);
        textViewLocation = view.findViewById(R.id.textViewLocation);
        textViewWebsite = view.findViewById(R.id.textViewWebsite);
        cardEditProfile = view.findViewById(R.id.cardEditProfile);
        cardSettings = view.findViewById(R.id.cardSettings);
        cardAbout = view.findViewById(R.id.cardAbout);
        buttonLogout = view.findViewById(R.id.buttonLogout);
    }

    private void initServices() {
        networkManager = NetworkManager.getInstance(getContext());
    }


    private void setupClickListeners() {
        cardEditProfile.setOnClickListener(v -> {
            if (currentUser != null) {
                Intent intent = new Intent(getActivity(), EditProfileActivity.class);
                intent.putExtra("CURRENT_USER", currentUser); // Pass user data
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "User data not loaded yet.", Toast.LENGTH_SHORT).show();
            }
        });

        cardSettings.setOnClickListener(v -> {
            showComingSoon("Settings");
        });

        cardAbout.setOnClickListener(v -> {
            showComingSoon("About");
        });

        buttonLogout.setOnClickListener(v -> {
            logout();
        });
    }

    private void loadUserInfo() {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            Log.e(TAG, "No authorization token found");
            navigateToLogin();
            return;
        }

        networkManager.getApiService().getMyProfile(authHeader).enqueue(new ApiCallback<User>() {
            @Override
            public void onSuccess(User result, String message) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        currentUser = result;
                        displayUserInfo(result);
                    });
                }
            }

            @Override
            public void onError(int statusCode, String message) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Log.e(TAG, "Error loading profile: " + message);
                        Toast.makeText(getContext(), "Failed to load profile: " + message, Toast.LENGTH_SHORT).show();

                        if (statusCode == 401) {
                            navigateToLogin();
                        }
                    });
                }
            }

            @Override
            public void onNetworkError(String message) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Log.e(TAG, "Network error: " + message);
                        Toast.makeText(getContext(), "Network error: " + message, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void displayUserInfo(User user) {
        if (user == null) return;

        // Set username
        if (user.getUsername() != null && !user.getUsername().trim().isEmpty()) {
            textViewUsername.setText(user.getUsername());
        } else {
            textViewUsername.setText("No username");
        }

        // Set email
        if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
            textViewEmail.setText(user.getEmail());
        } else {
            textViewEmail.setText("No email");
        }

        // Set bio
        if (textViewBio != null) {
            if (user.getBio() != null && !user.getBio().trim().isEmpty()) {
                textViewBio.setText(user.getBio());
                textViewBio.setVisibility(View.VISIBLE);
            } else {
                textViewBio.setVisibility(View.GONE);
            }
        }

        // Set location
        if (textViewLocation != null) {
            if (user.getLocation() != null && !user.getLocation().trim().isEmpty()) {
                textViewLocation.setText(user.getLocation());
                textViewLocation.setVisibility(View.VISIBLE);
            } else {
                textViewLocation.setVisibility(View.GONE);
            }
        }

        // Set website
        if (textViewWebsite != null) {
            if (user.getWebsite() != null && !user.getWebsite().trim().isEmpty()) {
                textViewWebsite.setText(user.getWebsite());
                textViewWebsite.setVisibility(View.VISIBLE);
            } else {
                textViewWebsite.setVisibility(View.GONE);
            }
        }

        // Load avatar
        if (user.getAvatar() != null && !user.getAvatar().trim().isEmpty()) {
            Glide.with(this)
                    .load(user.getAvatar())
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

    @Override
    public void onResume() {
        super.onResume();
        // Reload user info when returning from edit profile
        loadUserInfo();
    }

    private void logout() {
        String refreshToken = networkManager.getRefreshToken();

        if (refreshToken != null) {
            LogoutRequest request = new LogoutRequest(refreshToken);

            networkManager.getApiService().logout(request).enqueue(new ApiCallback<Object>() {
                @Override
                public void onSuccess(Object result, String message) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            networkManager.clearTokens();
                            navigateToLogin();
                        });
                    }
                }

                @Override
                public void onError(int statusCode, String message) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            networkManager.clearTokens();
                            navigateToLogin();
                        });
                    }
                }

                @Override
                public void onNetworkError(String message) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            networkManager.clearTokens();
                            navigateToLogin();
                        });
                    }
                }
            });
        } else {
            networkManager.clearTokens();
            navigateToLogin();
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private void showComingSoon(String feature) {
        Toast.makeText(getContext(), feature + " feature coming soon!", Toast.LENGTH_SHORT).show();
    }
}

