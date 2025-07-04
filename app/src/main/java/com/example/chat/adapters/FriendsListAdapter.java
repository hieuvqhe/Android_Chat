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
import com.example.chat.models.Friend;
import com.example.chat.models.UserInfo;
import com.example.chat.utils.FriendUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class FriendsListAdapter extends RecyclerView.Adapter<FriendsListAdapter.FriendViewHolder> {

    private List<Friend> friendsList;
    private OnFriendActionListener listener;
    private Context context;

    public interface OnFriendActionListener {
        void onUnfriend(Friend friend);
        void onStartChat(Friend friend);
        void onViewProfile(Friend friend);
    }

    public FriendsListAdapter(List<Friend> friendsList, OnFriendActionListener listener) {
        this.friendsList = friendsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        Friend friend = friendsList.get(position);
        holder.bind(friend);
    }

    @Override
    public int getItemCount() {
        return friendsList.size();
    }

    class FriendViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardView;
        private ImageView imageViewAvatar;
        private ImageView imageViewVerified;
        private ImageView imageViewOnlineStatus;
        private TextView textViewUsername;
        private TextView textViewBio;
        private TextView textViewEmail;
        private TextView textViewFriendSince;
        private MaterialButton buttonChat;
        private MaterialButton buttonMore;
        private View viewProfileClickArea;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardView);
            imageViewAvatar = itemView.findViewById(R.id.imageViewAvatar);
            imageViewVerified = itemView.findViewById(R.id.imageViewVerified);
            imageViewOnlineStatus = itemView.findViewById(R.id.imageViewOnlineStatus);
            textViewUsername = itemView.findViewById(R.id.textViewUsername);
            textViewBio = itemView.findViewById(R.id.textViewBio);
            textViewEmail = itemView.findViewById(R.id.textViewEmail);
            textViewFriendSince = itemView.findViewById(R.id.textViewFriendSince);
            buttonChat = itemView.findViewById(R.id.buttonChat);
            buttonMore = itemView.findViewById(R.id.buttonMore);
            viewProfileClickArea = itemView.findViewById(R.id.profileClickArea);
        }

        public void bind(Friend friend) {
            // FIXED: Use FriendUtils to get friend user info safely
            UserInfo friendUser = FriendUtils.getFriendUser(friend);
            if (friendUser == null) {
                // If no friend user found, hide the item or show error state
                cardView.setVisibility(View.GONE);
                return;
            }

            // Ensure card is visible
            cardView.setVisibility(View.VISIBLE);

            // Set username - FIXED: Use FriendUtils
            String displayName = FriendUtils.getUserDisplayName(friendUser);
            textViewUsername.setText(displayName);

            // Set email - FIXED: Use safe email getter
            String email = FriendUtils.getUserEmail(friendUser);
            if (!email.isEmpty()) {
                textViewEmail.setText(email);
                textViewEmail.setVisibility(View.VISIBLE);
            } else {
                textViewEmail.setVisibility(View.GONE);
            }

            // Set bio - FIXED: Use FriendUtils for safe bio handling
            String bio = FriendUtils.getUserBio(friendUser);
            if (!bio.equals("No bio available")) {
                textViewBio.setText(bio);
                textViewBio.setVisibility(View.VISIBLE);
            } else {
                textViewBio.setVisibility(View.GONE);
            }

            // Set friend since date - FIXED: Safe date formatting
            if (friend.getCreatedAt() != null) {
                String friendSince = "Friends since " + FriendUtils.getFormattedDate(friend.getCreatedAt());
                textViewFriendSince.setText(friendSince);
                textViewFriendSince.setVisibility(View.VISIBLE);
            } else {
                textViewFriendSince.setVisibility(View.GONE);
            }

            // Set avatar - FIXED: Better null handling
            loadUserAvatar(friendUser);

            // Set verification badge - FIXED: Use FriendUtils
            boolean isVerified = FriendUtils.isUserVerified(friendUser);
            imageViewVerified.setVisibility(isVerified ? View.VISIBLE : View.GONE);

            // Set online status - FIXED: Better handling of activeStatus
            try {
                if (friend.getActiveStatus() != 0) {
                    boolean isOnline = FriendUtils.isUserOnline(friend.getActiveStatus());
                    imageViewOnlineStatus.setVisibility(isOnline ? View.VISIBLE : View.GONE);
                } else {
                    // If activeStatus is 0, check user's activeStatus
                    if (friendUser.getActiveStatus() != 0) {
                        boolean isOnline = FriendUtils.isUserOnline(friendUser.getActiveStatus());
                        imageViewOnlineStatus.setVisibility(isOnline ? View.VISIBLE : View.GONE);
                    } else {
                        imageViewOnlineStatus.setVisibility(View.GONE);
                    }
                }
            } catch (Exception e) {
                // If any error in determining online status, hide the indicator
                imageViewOnlineStatus.setVisibility(View.GONE);
            }

            // Set click listeners
            buttonChat.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onStartChat(friend);
                }
            });

            buttonMore.setOnClickListener(v -> {
                showMoreOptions(friend);
            });

            if (viewProfileClickArea != null) {
                viewProfileClickArea.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onViewProfile(friend);
                    }
                });
            }

            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewProfile(friend);
                }
            });
        }

        private void loadUserAvatar(UserInfo user) {
            // FIXED: Better avatar loading with null handling
            String avatarUrl = FriendUtils.getUserAvatarUrl(user);

            RequestOptions requestOptions = new RequestOptions()
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .circleCrop();

            try {
                if (avatarUrl != null && !avatarUrl.isEmpty()) {
                    Glide.with(context)
                            .load(avatarUrl)
                            .apply(requestOptions)
                            .into(imageViewAvatar);
                } else {
                    // Use default avatar
                    Glide.with(context)
                            .load(R.drawable.default_avatar)
                            .apply(requestOptions)
                            .into(imageViewAvatar);
                }
            } catch (Exception e) {
                // If Glide fails, set default avatar directly
                imageViewAvatar.setImageResource(R.drawable.default_avatar);
            }
        }

        private void showMoreOptions(Friend friend) {
            try {
                // Create popup menu for more options
                androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(context, buttonMore);
                popup.getMenuInflater().inflate(R.menu.friend_options_menu, popup.getMenu());

                popup.setOnMenuItemClickListener(item -> {
                    int itemId = item.getItemId();
                    if (itemId == R.id.action_view_profile) {
                        if (listener != null) {
                            listener.onViewProfile(friend);
                        }
                        return true;
                    } else if (itemId == R.id.action_unfriend) {
                        if (listener != null) {
                            listener.onUnfriend(friend);
                        }
                        return true;
                    }
                    return false;
                });

                popup.show();
            } catch (Exception e) {
                // If popup menu fails, just call view profile
                if (listener != null) {
                    listener.onViewProfile(friend);
                }
            }
        }
    }

    /**
     * Update friends list
     */
    public void updateFriends(List<Friend> newFriends) {
        this.friendsList.clear();
        this.friendsList.addAll(newFriends);
        notifyDataSetChanged();
    }

    /**
     * Add more friends (for pagination)
     */
    public void addFriends(List<Friend> newFriends) {
        int startPosition = this.friendsList.size();
        this.friendsList.addAll(newFriends);
        notifyItemRangeInserted(startPosition, newFriends.size());
    }

    /**
     * Remove friend from list
     */
    public void removeFriend(Friend friend) {
        int position = friendsList.indexOf(friend);
        if (position != -1) {
            friendsList.remove(position);
            notifyItemRemoved(position);
        }
    }

    /**
     * Clear all friends
     */
    public void clearFriends() {
        friendsList.clear();
        notifyDataSetChanged();
    }
}