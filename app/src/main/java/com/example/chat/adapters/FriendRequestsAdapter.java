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

public class FriendRequestsAdapter extends RecyclerView.Adapter<FriendRequestsAdapter.RequestViewHolder> {

    private List<Friend> requestsList;
    private OnRequestActionListener listener;
    private Context context;

    public interface OnRequestActionListener {
        void onAcceptRequest(Friend request);
        void onRejectRequest(Friend request);
        void onViewProfile(Friend request);
    }

    public FriendRequestsAdapter(List<Friend> requestsList, OnRequestActionListener listener) {
        this.requestsList = requestsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_friend_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        Friend request = requestsList.get(position);
        holder.bind(request);
    }

    @Override
    public int getItemCount() {
        return requestsList.size();
    }

    class RequestViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardView;
        private ImageView imageViewAvatar;
        private ImageView imageViewVerified;
        private TextView textViewUsername;
        private TextView textViewBio;
        private TextView textViewEmail;
        private TextView textViewRequestDate;
        private MaterialButton buttonAccept;
        private MaterialButton buttonReject;
        private View viewProfileClickArea;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardView);
            imageViewAvatar = itemView.findViewById(R.id.imageViewAvatar);
            imageViewVerified = itemView.findViewById(R.id.imageViewVerified);
            textViewUsername = itemView.findViewById(R.id.textViewUsername);
            textViewBio = itemView.findViewById(R.id.textViewBio);
            textViewEmail = itemView.findViewById(R.id.textViewEmail);
            textViewRequestDate = itemView.findViewById(R.id.textViewRequestDate);
            buttonAccept = itemView.findViewById(R.id.buttonAccept);
            buttonReject = itemView.findViewById(R.id.buttonReject);
            viewProfileClickArea = itemView.findViewById(R.id.profileClickArea);
        }

        public void bind(Friend request) {
            UserInfo senderUser = FriendUtils.getFriendUser(request);
            if (senderUser == null) return;

            // Set username
            String displayName = FriendUtils.getUserDisplayName(senderUser);
            textViewUsername.setText(displayName);

            // Set email
            if (senderUser.getEmail() != null && !senderUser.getEmail().isEmpty()) {
                textViewEmail.setText(senderUser.getEmail());
                textViewEmail.setVisibility(View.VISIBLE);
            } else {
                textViewEmail.setVisibility(View.GONE);
            }

            // Set bio
            String bio = FriendUtils.getUserBio(senderUser);
            if (bio != null && !bio.equals("No bio available")) {
                textViewBio.setText(bio);
                textViewBio.setVisibility(View.VISIBLE);
            } else {
                textViewBio.setVisibility(View.GONE);
            }

            // Set request date
            if (request.getCreatedAt() != null) {
                String requestDate = "Sent " + FriendUtils.getFormattedDate(request.getCreatedAt());
                textViewRequestDate.setText(requestDate);
                textViewRequestDate.setVisibility(View.VISIBLE);
            } else {
                textViewRequestDate.setVisibility(View.GONE);
            }

            // Set avatar
            loadUserAvatar(senderUser);

            // Set verification badge
            boolean isVerified = FriendUtils.isUserVerified(senderUser);
            imageViewVerified.setVisibility(isVerified ? View.VISIBLE : View.GONE);

            // Set click listeners
            buttonAccept.setOnClickListener(v -> {
                if (listener != null) {
                    // Disable buttons to prevent double clicks
                    buttonAccept.setEnabled(false);
                    buttonReject.setEnabled(false);
                    buttonAccept.setText("Accepting...");

                    listener.onAcceptRequest(request);
                }
            });

            buttonReject.setOnClickListener(v -> {
                if (listener != null) {
                    // Disable buttons to prevent double clicks
                    buttonAccept.setEnabled(false);
                    buttonReject.setEnabled(false);
                    buttonReject.setText("Rejecting...");

                    listener.onRejectRequest(request);
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
     * Update requests list
     */
    public void updateRequests(List<Friend> newRequests) {
        this.requestsList.clear();
        this.requestsList.addAll(newRequests);
        notifyDataSetChanged();
    }

    /**
     * Add more requests (for pagination)
     */
    public void addRequests(List<Friend> newRequests) {
        int startPosition = this.requestsList.size();
        this.requestsList.addAll(newRequests);
        notifyItemRangeInserted(startPosition, newRequests.size());
    }

    /**
     * Remove request from list
     */
    public void removeRequest(Friend request) {
        int position = requestsList.indexOf(request);
        if (position != -1) {
            requestsList.remove(position);
            notifyItemRemoved(position);
        }
    }

    /**
     * Clear all requests
     */
    public void clearRequests() {
        requestsList.clear();
        notifyDataSetChanged();
    }
}