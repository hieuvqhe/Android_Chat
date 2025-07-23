package com.example.chat.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.chat.R;
import com.example.chat.models.Friend;
import com.example.chat.models.UserInfo;
import com.example.chat.utils.FriendUtils;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SelectFriendsAdapter extends RecyclerView.Adapter<SelectFriendsAdapter.FriendViewHolder> {

    private List<Friend> friendsList;
    private List<Friend> filteredFriendsList;
    private Set<Friend> selectedFriends;
    private OnFriendSelectionListener listener;
    private Context context;

    public interface OnFriendSelectionListener {
        void onFriendSelected(Friend friend, boolean isSelected);
        void onFriendClick(Friend friend);
    }

    public SelectFriendsAdapter(List<Friend> friendsList, OnFriendSelectionListener listener) {
        this.friendsList = friendsList;
        this.filteredFriendsList = new ArrayList<>(friendsList);
        this.selectedFriends = new HashSet<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_friend_selection, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        Friend friend = filteredFriendsList.get(position);
        holder.bind(friend);
    }

    @Override
    public int getItemCount() {
        return filteredFriendsList.size();
    }

    public void filter(String query) {
        filteredFriendsList.clear();

        if (query.isEmpty()) {
            filteredFriendsList.addAll(friendsList);
        } else {
            String searchQuery = query.toLowerCase().trim();
            for (Friend friend : friendsList) {
                UserInfo user = FriendUtils.getFriendUser(friend);
                if (user != null) {
                    String username = user.getUsername() != null ? user.getUsername().toLowerCase() : "";
                    String email = user.getEmail() != null ? user.getEmail().toLowerCase() : "";
                    String bio = user.getBio() != null ? user.getBio().toLowerCase() : "";

                    if (username.contains(searchQuery) ||
                            email.contains(searchQuery) ||
                            bio.contains(searchQuery)) {
                        filteredFriendsList.add(friend);
                    }
                }
            }
        }

        notifyDataSetChanged();
    }

    public void toggleSelection(Friend friend) {
        if (selectedFriends.contains(friend)) {
            selectedFriends.remove(friend);
        } else {
            selectedFriends.add(friend);
        }
        notifyDataSetChanged();
    }

    public boolean isSelected(Friend friend) {
        return selectedFriends.contains(friend);
    }

    public void clearSelection() {
        selectedFriends.clear();
        notifyDataSetChanged();
    }

    public Set<Friend> getSelectedFriends() {
        return new HashSet<>(selectedFriends);
    }

    class FriendViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardView;
        private ImageView imageViewAvatar;
        private ImageView imageViewVerified;
        private TextView textViewUsername;
        private TextView textViewEmail;
        private TextView textViewBio;
        private CheckBox checkBoxSelect;
        private View clickArea;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardView);
            imageViewAvatar = itemView.findViewById(R.id.imageViewAvatar);
            imageViewVerified = itemView.findViewById(R.id.imageViewVerified);
            textViewUsername = itemView.findViewById(R.id.textViewUsername);
            textViewEmail = itemView.findViewById(R.id.textViewEmail);
            textViewBio = itemView.findViewById(R.id.textViewBio);
            checkBoxSelect = itemView.findViewById(R.id.checkBoxSelect);
            clickArea = itemView.findViewById(R.id.clickArea);
        }

        public void bind(Friend friend) {
            UserInfo friendUser = FriendUtils.getFriendUser(friend);
            if (friendUser == null) return;

            // Set username
            String displayName = FriendUtils.getUserDisplayName(friendUser);
            textViewUsername.setText(displayName);

            // Set email
            if (friendUser.getEmail() != null && !friendUser.getEmail().isEmpty()) {
                textViewEmail.setText(friendUser.getEmail());
                textViewEmail.setVisibility(View.VISIBLE);
            } else {
                textViewEmail.setVisibility(View.GONE);
            }

            // Set bio
            String bio = FriendUtils.getUserBio(friendUser);
            if (bio != null && !bio.equals("No bio available")) {
                textViewBio.setText(bio);
                textViewBio.setVisibility(View.VISIBLE);
            } else {
                textViewBio.setVisibility(View.GONE);
            }

            // Set avatar
            loadUserAvatar(friendUser);

            // Set verification badge
            boolean isVerified = FriendUtils.isUserVerified(friendUser);
            imageViewVerified.setVisibility(isVerified ? View.VISIBLE : View.GONE);

            // Set selection state
            boolean isSelected = selectedFriends.contains(friend);
            checkBoxSelect.setChecked(isSelected);

            // Set click listeners
            clickArea.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFriendClick(friend);
                }
            });

            checkBoxSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    if (isChecked) {
                        selectedFriends.add(friend);
                    } else {
                        selectedFriends.remove(friend);
                    }
                    listener.onFriendSelected(friend, isChecked);
                }
            });

            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFriendClick(friend);
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
                Glide.with(context)
                        .load(R.drawable.default_avatar)
                        .apply(requestOptions)
                        .into(imageViewAvatar);
            }
        }
    }
}