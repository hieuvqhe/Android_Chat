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
import com.example.chat.models.Friend;
import com.example.chat.models.UserInfo;
import com.example.chat.utils.FriendUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class SentRequestsAdapter extends RecyclerView.Adapter<SentRequestsAdapter.SentRequestViewHolder> {

    private List<Friend> sentRequestsList;
    private OnSentRequestActionListener listener;
    private Context context;

    public interface OnSentRequestActionListener {
        void onCancelRequest(Friend request);
        void onViewProfile(Friend request);
    }

    public SentRequestsAdapter(List<Friend> sentRequestsList, OnSentRequestActionListener listener) {
        this.sentRequestsList = sentRequestsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SentRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_sent_request, parent, false);
        return new SentRequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SentRequestViewHolder holder, int position) {
        Friend request = sentRequestsList.get(position);
        holder.bind(request);
    }

    @Override
    public int getItemCount() {
        return sentRequestsList.size();
    }

    class SentRequestViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardView;
        private ImageView imageViewAvatar;
        private ImageView imageViewVerified;
        private TextView textViewUsername;
        private TextView textViewBio;
        private TextView textViewEmail;
        private TextView textViewSentDate;
        private TextView textViewStatus;
        private MaterialButton buttonCancel;
        private View viewProfileClickArea;

        public SentRequestViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardView);
            imageViewAvatar = itemView.findViewById(R.id.imageViewAvatar);
            imageViewVerified = itemView.findViewById(R.id.imageViewVerified);
            textViewUsername = itemView.findViewById(R.id.textViewUsername);
            textViewBio = itemView.findViewById(R.id.textViewBio);
            textViewEmail = itemView.findViewById(R.id.textViewEmail);
            textViewSentDate = itemView.findViewById(R.id.textViewSentDate);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
            buttonCancel = itemView.findViewById(R.id.buttonCancel);
            viewProfileClickArea = itemView.findViewById(R.id.profileClickArea);
        }

        public void bind(Friend request) {
            UserInfo friendUser = FriendUtils.getFriendUser(request);
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

            // Set sent date
            if (request.getCreatedAt() != null) {
                String sentDate = "Sent " + FriendUtils.getFormattedDate(request.getCreatedAt());
                textViewSentDate.setText(sentDate);
                textViewSentDate.setVisibility(View.VISIBLE);
            } else {
                textViewSentDate.setVisibility(View.GONE);
            }

            // Set status
            textViewStatus.setText("Pending");
            textViewStatus.setVisibility(View.VISIBLE);

            // Set avatar
            loadUserAvatar(friendUser);

            // Set verification badge
            boolean isVerified = FriendUtils.isUserVerified(friendUser);
            imageViewVerified.setVisibility(isVerified ? View.VISIBLE : View.GONE);

            // Set click listeners
            buttonCancel.setOnClickListener(v -> {
                if (listener != null) {
                    // Disable button to prevent double clicks
                    buttonCancel.setEnabled(false);
                    buttonCancel.setText("Cancelling...");

                    listener.onCancelRequest(request);
                }
            });

            viewProfileClickArea.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewProfile(request);
                }
            });

            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewProfile(request);
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

    /**
     * Update sent requests list
     */
    public void updateSentRequests(List<Friend> newRequests) {
        this.sentRequestsList.clear();
        this.sentRequestsList.addAll(newRequests);
        notifyDataSetChanged();
    }

    /**
     * Add more sent requests (for pagination)
     */
    public void addSentRequests(List<Friend> newRequests) {
        int startPosition = this.sentRequestsList.size();
        this.sentRequestsList.addAll(newRequests);
        notifyItemRangeInserted(startPosition, newRequests.size());
    }

    /**
     * Remove sent request from list
     */
    public void removeSentRequest(Friend request) {
        int position = sentRequestsList.indexOf(request);
        if (position != -1) {
            sentRequestsList.remove(position);
            notifyItemRemoved(position);
        }
    }

    /**
     * Clear all sent requests
     */
    public void clearSentRequests() {
        sentRequestsList.clear();
        notifyDataSetChanged();
    }
}