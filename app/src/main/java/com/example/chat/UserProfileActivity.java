package com.example.chat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.chat.models.AddFriendRequest;
import com.example.chat.models.Friend;
import com.example.chat.models.User;
import com.example.chat.network.ApiCallback;
import com.example.chat.network.NetworkManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

/**
 * Activity hiển thị thông tin chi tiết của người dùng
 * Cho phép gửi lời mời kết bạn và nhắn tin
 *
 * Features:
 * - Hiển thị thông tin cá nhân (avatar, bio, location, website)
 * - Gửi lời mời kết bạn
 * - Mở website của người dùng
 * - Xác thực trạng thái verified
 *
 * @author Your Name
 * @version 2.0
 */
public class UserProfileActivity extends AppCompatActivity implements UserProfileContract.View {

    private static final String TAG = "UserProfileActivity";

    // Intent configuration constants
    public static class IntentKeys {
        public static final String USERNAME = "extra_username";
        public static final String USERNAME_ALTERNATIVE = "username";
        public static final String USER_ID = "user_id";
    }

    // UI State constants
    private static class UIState {
        static final String LOADING_TEXT = "Đang tải...";
        static final String SENDING_TEXT = "Đang gửi...";
        static final String ADD_FRIEND_TEXT = "Kết bạn";
        static final String REQUEST_SENT_TEXT = "Đã gửi lời mời";
    }

    // View holder pattern for better organization
    private static class ViewComponents {
        final MaterialToolbar toolbar;
        final ImageView avatarImage;
        final TextView usernameText;
        final TextView emailText;
        final TextView verificationStatusText;
        final TextView bioText;
        final TextView locationText;
        final TextView websiteText;
        final MaterialCardView bioCard;
        final MaterialCardView locationCard;
        final MaterialCardView websiteCard;
        final MaterialButton sendMessageButton;
        final MaterialButton addFriendButton;

        ViewComponents(AppCompatActivity activity) {
            toolbar = activity.findViewById(R.id.toolbar);
            avatarImage = activity.findViewById(R.id.imageViewAvatar);
            usernameText = activity.findViewById(R.id.textViewUsername);
            emailText = activity.findViewById(R.id.textViewEmail);
            verificationStatusText = activity.findViewById(R.id.textViewVerifyStatus);
            bioText = activity.findViewById(R.id.textViewBio);
            locationText = activity.findViewById(R.id.textViewLocation);
            websiteText = activity.findViewById(R.id.textViewWebsite);
            bioCard = activity.findViewById(R.id.cardBio);
            locationCard = activity.findViewById(R.id.cardLocation);
            websiteCard = activity.findViewById(R.id.cardWebsite);
            sendMessageButton = activity.findViewById(R.id.buttonSendMessage);
            addFriendButton = activity.findViewById(R.id.buttonAddFriend);
        }
    }

    // Member variables
    private ViewComponents viewComponents;
    private UserProfileContract.Presenter presenter;
    private String targetUsername;
    private User currentUserProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        if (!extractIntentData()) {
            handleInvalidIntent();
            return;
        }

        initializeComponents();
        setupUserInterface();
        loadUserProfileData();
    }

    /**
     * Trích xuất dữ liệu từ Intent
     * @return true nếu có username hợp lệ, false nếu không
     */
    private boolean extractIntentData() {
        Intent intent = getIntent();
        targetUsername = intent.getStringExtra(IntentKeys.USERNAME);

        if (TextUtils.isEmpty(targetUsername)) {
            targetUsername = intent.getStringExtra(IntentKeys.USERNAME_ALTERNATIVE);
        }

        Log.d(TAG, "Extracted username from intent: " + targetUsername);
        return !TextUtils.isEmpty(targetUsername);
    }

    /**
     * Xử lý trường hợp Intent không hợp lệ
     */
    private void handleInvalidIntent() {
        Log.e(TAG, "No valid username provided in intent");
        showErrorMessage("Lỗi: Không có thông tin người dùng");
        finishActivity();
    }

    /**
     * Khởi tạo các components cần thiết
     */
    private void initializeComponents() {
        viewComponents = new ViewComponents(this);
        presenter = new UserProfilePresenter(this, NetworkManager.getInstance(this));
    }

    /**
     * Thiết lập giao diện người dùng
     */
    private void setupUserInterface() {
        configureToolbar();
        setupClickListeners();
    }

    /**
     * Cấu hình toolbar
     */
    private void configureToolbar() {
        setSupportActionBar(viewComponents.toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(targetUsername);
        }

        viewComponents.toolbar.setNavigationOnClickListener(v -> handleBackNavigation());
    }

    /**
     * Thiết lập các sự kiện click
     */
    private void setupClickListeners() {
        viewComponents.sendMessageButton.setOnClickListener(this::onSendMessageClicked);
        viewComponents.addFriendButton.setOnClickListener(this::onAddFriendClicked);
        viewComponents.websiteCard.setOnClickListener(this::onWebsiteClicked);
    }

    /**
     * Xử lý sự kiện click nút gửi tin nhắn
     */
    private void onSendMessageClicked(View view) {
        showInfoMessage("Tính năng nhắn tin sẽ sớm ra mắt!");
    }

    /**
     * Xử lý sự kiện click nút kết bạn
     */
    private void onAddFriendClicked(View view) {
        if (currentUserProfile != null) {
            presenter.sendFriendRequest(currentUserProfile);
        } else {
            showErrorMessage("Thông tin người dùng chưa được tải");
        }
    }

    /**
     * Xử lý sự kiện click vào website
     */
    private void onWebsiteClicked(View view) {
        if (currentUserProfile != null && !TextUtils.isEmpty(currentUserProfile.getWebsite())) {
            openExternalWebsite(currentUserProfile.getWebsite());
        }
    }

    /**
     * Tải dữ liệu người dùng từ server
     */
    private void loadUserProfileData() {
        Log.d(TAG, "Loading profile data for username: " + targetUsername);
        presenter.loadUserProfile(targetUsername);
    }

    // Implementation of UserProfileContract.View interface

    @Override
    public void showUserProfile(User user) {
        if (user == null) {
            Log.w(TAG, "Received null user profile");
            return;
        }

        currentUserProfile = user;
        populateUserInterface(user);
        Log.d(TAG, "User profile displayed successfully: " + user.getUsername());
    }

    /**
     * Điền thông tin người dùng vào giao diện
     */
    private void populateUserInterface(User user) {
        updateToolbarTitle(user.getUsername());
        updateUserBasicInfo(user);
        updateVerificationStatus(user);
        updateOptionalFields(user);
        loadUserAvatar(user);
    }

    /**
     * Cập nhật tiêu đề toolbar
     */
    private void updateToolbarTitle(String username) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(
                    !TextUtils.isEmpty(username) ? username : "Hồ sơ người dùng"
            );
        }
    }

    /**
     * Cập nhật thông tin cơ bản của người dùng
     */
    private void updateUserBasicInfo(User user) {
        viewComponents.usernameText.setText(
                !TextUtils.isEmpty(user.getUsername()) ? user.getUsername() : "Không có tên"
        );

        viewComponents.emailText.setText(
                !TextUtils.isEmpty(user.getEmail()) ? user.getEmail() : "Không có email"
        );
    }

    /**
     * Cập nhật trạng thái xác thực
     */
    private void updateVerificationStatus(User user) {
        try {
            boolean isVerified = user.getVerify() != null && user.getVerify() == 1;

            if (isVerified) {
                viewComponents.verificationStatusText.setText("✓ Đã xác thực");
                viewComponents.verificationStatusText.setTextColor(
                        ContextCompat.getColor(this, android.R.color.holo_green_dark)
                );
            } else {
                viewComponents.verificationStatusText.setText("Chưa xác thực");
                viewComponents.verificationStatusText.setTextColor(
                        ContextCompat.getColor(this, android.R.color.darker_gray)
                );
            }

            viewComponents.verificationStatusText.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            Log.w(TAG, "Error updating verification status: " + e.getMessage());
            viewComponents.verificationStatusText.setVisibility(View.GONE);
        }
    }

    /**
     * Cập nhật các trường thông tin tùy chọn
     */
    private void updateOptionalFields(User user) {
        updateOptionalField(user.getBio(), viewComponents.bioText, viewComponents.bioCard);
        updateOptionalField(user.getLocation(), viewComponents.locationText, viewComponents.locationCard);
        updateOptionalField(user.getWebsite(), viewComponents.websiteText, viewComponents.websiteCard);
    }

    /**
     * Cập nhật một trường thông tin tùy chọn
     */
    private void updateOptionalField(String value, TextView textView, MaterialCardView cardView) {
        if (!TextUtils.isEmpty(value)) {
            textView.setText(value);
            cardView.setVisibility(View.VISIBLE);
        } else {
            cardView.setVisibility(View.GONE);
        }
    }

    /**
     * Tải và hiển thị avatar của người dùng
     */
    private void loadUserAvatar(User user) {
        RequestOptions options = new RequestOptions()
                .circleCrop()
                .placeholder(R.drawable.default_avatar)
                .error(R.drawable.default_avatar);

        if (!TextUtils.isEmpty(user.getAvatar())) {
            Glide.with(this)
                    .load(user.getAvatar())
                    .apply(options)
                    .into(viewComponents.avatarImage);
        } else {
            Glide.with(this)
                    .load(R.drawable.default_avatar)
                    .apply(options)
                    .into(viewComponents.avatarImage);
        }
    }

    @Override
    public void showLoadingState() {
        // Có thể thêm progress bar hoặc loading indicator
        Log.d(TAG, "Showing loading state");
    }

    @Override
    public void hideLoadingState() {
        // Ẩn loading indicator
        Log.d(TAG, "Hiding loading state");
    }

    @Override
    public void showFriendRequestSent() {
        showSuccessMessage("Đã gửi lời mời kết bạn tới " + currentUserProfile.getUsername());
        updateAddFriendButtonState(UIState.REQUEST_SENT_TEXT, false);
    }

    @Override
    public void showFriendRequestLoading() {
        updateAddFriendButtonState(UIState.SENDING_TEXT, false);
    }

    @Override
    public void resetFriendRequestButton() {
        updateAddFriendButtonState(UIState.ADD_FRIEND_TEXT, true);
    }

    /**
     * Cập nhật trạng thái nút kết bạn
     */
    private void updateAddFriendButtonState(String text, boolean enabled) {
        viewComponents.addFriendButton.setText(text);
        viewComponents.addFriendButton.setEnabled(enabled);
    }

    @Override
    public void showErrorMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Log.e(TAG, "Error: " + message);
    }

    @Override
    public void showSuccessMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Success: " + message);
    }

    @Override
    public void showInfoMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Log.i(TAG, "Info: " + message);
    }

    @Override
    public void handleAuthenticationError() {
        showErrorMessage("Lỗi xác thực");
        finishActivity();
    }

    @Override
    public void handleUserNotFound() {
        showErrorMessage("Không tìm thấy người dùng");
        finishActivity();
    }

    @Override
    public void handleNetworkError(String message) {
        showErrorMessage("Lỗi kết nối: " + message);
    }

    /**
     * Mở website bên ngoài
     */
    private void openExternalWebsite(String website) {
        try {
            String url = website;
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url;
            }

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening website: " + e.getMessage());
            showErrorMessage("Không thể mở website");
        }
    }

    /**
     * Xử lý navigation back
     */
    private void handleBackNavigation() {
        finishActivity();
    }

    /**
     * Kết thúc activity với animation
     */
    private void finishActivity() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            handleBackNavigation();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.onDestroy();
        }
    }
}

/**
 * Contract interface định nghĩa tương tác giữa View và Presenter
 */
interface UserProfileContract {

    interface View {
        void showUserProfile(User user);
        void showLoadingState();
        void hideLoadingState();
        void showFriendRequestSent();
        void showFriendRequestLoading();
        void resetFriendRequestButton();
        void showErrorMessage(String message);
        void showSuccessMessage(String message);
        void showInfoMessage(String message);
        void handleAuthenticationError();
        void handleUserNotFound();
        void handleNetworkError(String message);
    }

    interface Presenter {
        void loadUserProfile(String username);
        void sendFriendRequest(User user);
        void onDestroy();
    }
}

/**
 * Presenter xử lý logic nghiệp vụ cho UserProfile
 */
class UserProfilePresenter implements UserProfileContract.Presenter {

    private static final String TAG = "UserProfilePresenter";

    private final UserProfileContract.View view;
    private final NetworkManager networkManager;

    public UserProfilePresenter(UserProfileContract.View view, NetworkManager networkManager) {
        this.view = view;
        this.networkManager = networkManager;
    }

    @Override
    public void loadUserProfile(String username) {
        if (TextUtils.isEmpty(username)) {
            view.showErrorMessage("Tên người dùng không hợp lệ");
            return;
        }

        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            view.handleAuthenticationError();
            return;
        }

        view.showLoadingState();

        networkManager.getApiService().getUserProfile(authHeader, username)
                .enqueue(new ApiCallback<User>() {
                    @Override
                    public void onSuccess(User result, String message) {
                        view.hideLoadingState();
                        view.showUserProfile(result);
                    }

                    @Override
                    public void onError(int statusCode, String message) {
                        view.hideLoadingState();

                        switch (statusCode) {
                            case 401:
                                view.handleAuthenticationError();
                                break;
                            case 404:
                                view.handleUserNotFound();
                                break;
                            default:
                                view.showErrorMessage("Lỗi tải thông tin: " + message);
                                break;
                        }
                    }

                    @Override
                    public void onNetworkError(String message) {
                        view.hideLoadingState();
                        view.handleNetworkError(message);
                    }
                });
    }

    @Override
    public void sendFriendRequest(User user) {
        if (user == null) {
            view.showErrorMessage("Thông tin người dùng không hợp lệ");
            return;
        }

        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            view.handleAuthenticationError();
            return;
        }

        view.showFriendRequestLoading();

        AddFriendRequest request = new AddFriendRequest(user.getId());

        networkManager.getApiService().addFriend(authHeader, request)
                .enqueue(new ApiCallback<Friend>() {
                    @Override
                    public void onSuccess(Friend result, String message) {
                        view.showFriendRequestSent();
                    }

                    @Override
                    public void onError(int statusCode, String message) {
                        view.resetFriendRequestButton();
                        view.showErrorMessage("Lỗi gửi lời mời: " + message);
                    }

                    @Override
                    public void onNetworkError(String message) {
                        view.resetFriendRequestButton();
                        view.handleNetworkError(message);
                    }
                });
    }

    @Override
    public void onDestroy() {
        // Cleanup resources nếu cần
        Log.d(TAG, "Presenter destroyed");
    }
}