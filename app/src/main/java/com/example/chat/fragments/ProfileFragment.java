package com.example.chat.fragments;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.chat.LoginActivity;
import com.example.chat.R;
import com.example.chat.models.LogoutRequest;
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
    private MaterialCardView cardEditProfile;
    private MaterialCardView cardSettings;
    private MaterialCardView cardAbout;
    private MaterialButton buttonLogout;

    // Services
    private NetworkManager networkManager;

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
            // TODO: Navigate to edit profile
            showComingSoon("Edit Profile");
        });

        cardSettings.setOnClickListener(v -> {
            // TODO: Navigate to settings
            showComingSoon("Settings");
        });

        cardAbout.setOnClickListener(v -> {
            // TODO: Navigate to about
            showComingSoon("About");
        });

        buttonLogout.setOnClickListener(v -> {
            logout();
        });
    }

    private void loadUserInfo() {
        // TODO: Load actual user info from API
        // For now, show placeholder
        textViewUsername.setText("Your Username");
        textViewEmail.setText("your.email@example.com");

        // Load default avatar
        Glide.with(this)
                .load(R.drawable.default_avatar)
                .apply(new RequestOptions().circleCrop())
                .into(imageViewAvatar);
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