package com.example.chat.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.chat.R;
import com.example.chat.constants.FriendConstants;
import com.example.chat.models.UserInfo;
import com.example.chat.utils.FriendUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class FindUsersAdapter extends RecyclerView.Adapter<FindUsersAdapter.UserViewHolder> {

    private List<UserInfo> usersList;
    private OnUserActionListener listener;
    private Context context;

    public interface OnUserActionListener {
        void onAddFriend(UserInfo user);
        void onViewProfile(UserInfo user);
    }

    public FindUsersAdapter(List<UserInfo> usersList, OnUserActionListener listener) {
        this.usersList = usersList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_find_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserInfo user = usersList.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardView;
        private ImageView imageViewAvatar;
        private ImageView imageViewVerified;
        private ImageView imageViewOnlineStatus;
        private TextView textViewUsername;
        private TextView textViewBio;
        private TextView textViewEmail;
        private MaterialButton buttonAddFriend;
        private View viewProfileClickArea;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardView);
            imageViewAvatar = itemView.findViewById(R.id.imageViewAvatar);
            imageViewVerified = itemView.findViewById(R.id.imageViewVerified);
            imageViewOnlineStatus = itemView.findViewById(R.id.imageViewOnlineStatus);
            textViewUsername = itemView.findViewById(R.id.textViewUsername);
            textViewBio = itemView.findViewById(R.id.textViewBio);
            textViewEmail = itemView.findViewById(R.id.textViewEmail);
            buttonAddFriend = itemView.findViewById(R.id.buttonAddFriend);
            viewProfileClickArea = itemView.findViewById(R.id.profileClickArea);
        }

        public void bind(UserInfo user) {
            // Set username
            String displayName = FriendUtils.getUserDisplayName(user);
            textViewUsername.setText(displayName);

            // Set email
            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                textViewEmail.setText(user.getEmail());
                textViewEmail.setVisibility(View.VISIBLE);
            } else {
                textViewEmail.setVisibility(View.GONE);
            }

            // Set bio
            String bio = FriendUtils.getUserBio(user);
            if (bio != null && !bio.equals("No bio available")) {
                textViewBio.setText(bio);
                textViewBio.setVisibility(View.VISIBLE);
            } else {
                textViewBio.setVisibility(View.GONE);
            }

            // Set avatar
            loadUserAvatar(user);

            // Set verification badge
            boolean isVerified = FriendUtils.isUserVerified(user);
            imageViewVerified.setVisibility(isVerified ? View.VISIBLE : View.GONE);

            // Set online status (if available)
            // Note: UserInfo doesn't have activeStatus, so we'll hide this for now
            imageViewOnlineStatus.setVisibility(View.GONE);

            // Set click listeners
            buttonAddFriend.setOnClickListener(v -> {
                if (listener != null) {
                    // Disable button temporarily to prevent double clicks
                    buttonAddFriend.setEnabled(false);
                    buttonAddFriend.setText("Sending...");

                    listener.onAddFriend(user);

                    // Re-enable after delay
                    buttonAddFriend.postDelayed(() -> {
                        buttonAddFriend.setEnabled(true);
                        buttonAddFriend.setText("Add Friend");
                    }, 2000);
                }
            });

            viewProfileClickArea.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewProfile(user);
                }
            });

            // Add subtle animation
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewProfile(user);
                }
            });
        }

        private void loadUserAvatar(UserInfo user) {
            String avatarUrl = FriendUtils.getUserAvatarUrl(user);

            RequestOptions requestOptions = new RequestOptions()
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .circleCrop();

            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                Glide.with(context)
                        .load(avatarUrl)
                        .apply(requestOptions)
                        .into(imageViewAvatar);
            } else {
                // Use default avatar with user's initial
                Glide.with(context)
                        .load(R.drawable.default_avatar)
                        .apply(requestOptions)
                        .into(imageViewAvatar);
            }
        }
    }

    /**
     * Update users list
     */
    public void updateUsers(List<UserInfo> newUsers) {
        this.usersList.clear();
        this.usersList.addAll(newUsers);
        notifyDataSetChanged();
    }

    /**
     * Add more users (for pagination)
     */
    public void addUsers(List<UserInfo> newUsers) {
        int startPosition = this.usersList.size();
        this.usersList.addAll(newUsers);
        notifyItemRangeInserted(startPosition, newUsers.size());
    }

    /**
     * Remove user from list
     */
    public void removeUser(UserInfo user) {
        int position = usersList.indexOf(user);
        if (position != -1) {
            usersList.remove(position);
            notifyItemRemoved(position);
        }
    }

    /**
     * Clear all users
     */
    public void clearUsers() {
        usersList.clear();
        notifyDataSetChanged();
    }
}