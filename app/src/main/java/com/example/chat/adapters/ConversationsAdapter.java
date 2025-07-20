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
import com.example.chat.models.Conversation;
import com.example.chat.utils.TimeUtils;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class ConversationsAdapter extends RecyclerView.Adapter<ConversationsAdapter.ConversationViewHolder> {

    private List<Conversation> conversationsList;
    private OnConversationActionListener listener;
    private Context context;

    public interface OnConversationActionListener {
        void onConversationClick(Conversation conversation);

        void onConversationLongClick(Conversation conversation);

        void onDeleteConversation(Conversation conversation);

        void onMuteConversation(Conversation conversation);

        void onPinConversation(Conversation conversation);
    }

    public ConversationsAdapter(List<Conversation> conversationsList, OnConversationActionListener listener) {
        this.conversationsList = conversationsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    private float dpToPx(float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        Conversation conversation = conversationsList.get(position);
        holder.bind(conversation);
    }

    @Override
    public int getItemCount() {
        return conversationsList.size();
    }

    class ConversationViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardView;
        private ImageView imageViewAvatar;
        private ImageView imageViewOnlineStatus;
        private ImageView imageViewMuted;
        private ImageView imageViewPinned;
        private TextView textViewName;
        private TextView textViewLastMessage;
        private TextView textViewTime;
        private TextView textViewUnreadCount;
        private View viewUnreadIndicator;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardView);
            imageViewAvatar = itemView.findViewById(R.id.imageViewAvatar);
            imageViewOnlineStatus = itemView.findViewById(R.id.imageViewOnlineStatus);
            imageViewMuted = itemView.findViewById(R.id.imageViewMuted);
            imageViewPinned = itemView.findViewById(R.id.imageViewPinned);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewLastMessage = itemView.findViewById(R.id.textViewLastMessage);
            textViewTime = itemView.findViewById(R.id.textViewTime);
            textViewUnreadCount = itemView.findViewById(R.id.textViewUnreadCount);
            viewUnreadIndicator = itemView.findViewById(R.id.viewUnreadIndicator);
        }

        public void bind(Conversation conversation) {
            // Set conversation name
            textViewName.setText(conversation.getDisplayName());

            // Set last message
            if (conversation.getContent() != null && !conversation.getContent().isEmpty()) {
                textViewLastMessage.setText(conversation.getContent());
                textViewLastMessage.setVisibility(View.VISIBLE);
            } else {
                textViewLastMessage.setText("No messages yet");
                textViewLastMessage.setVisibility(View.VISIBLE);
            }

            // Set time
            if (conversation.getUpdatedAt() != null) {
                String timeText = TimeUtils.getRelativeTime(conversation.getUpdatedAt());
                textViewTime.setText(timeText);
                textViewTime.setVisibility(View.VISIBLE);
            } else {
                textViewTime.setVisibility(View.GONE);
            }

            // Set avatar
            loadConversationAvatar(conversation);

            // Set unread count
            if (conversation.getUnreadCount() > 0) {
                textViewUnreadCount.setText(String.valueOf(conversation.getUnreadCount()));
                textViewUnreadCount.setVisibility(View.VISIBLE);
                viewUnreadIndicator.setVisibility(View.VISIBLE);
                // Make text bold for unread conversations
                textViewName.setTypeface(null, android.graphics.Typeface.BOLD);
                textViewLastMessage.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                textViewUnreadCount.setVisibility(View.GONE);
                viewUnreadIndicator.setVisibility(View.GONE);
                // Normal text for read conversations
                textViewName.setTypeface(null, android.graphics.Typeface.NORMAL);
                textViewLastMessage.setTypeface(null, android.graphics.Typeface.NORMAL);
            }

            // Set muted indicator
            if (conversation.isMuted()) {
                imageViewMuted.setVisibility(View.VISIBLE);
            } else {
                imageViewMuted.setVisibility(View.GONE);
            }
            float elevationPinned = dpToPx(8);
            float elevationNormal = dpToPx(4);
            // Set pinned indicator
            if (conversation.isPinned()) {
                imageViewPinned.setVisibility(View.VISIBLE);
                cardView.setCardElevation(elevationPinned); // Slightly elevated for pinned
            } else {
                imageViewPinned.setVisibility(View.GONE);
                cardView.setCardElevation(elevationNormal); // Normal elevation
            }

            // Set online status for private chats
            if (conversation.isPrivateChat() && conversation.getOtherParticipant() != null) {
                // You can implement online status check here
                // For now, hide it as we don't have real-time presence
                imageViewOnlineStatus.setVisibility(View.GONE);
            } else {
                imageViewOnlineStatus.setVisibility(View.GONE);
            }

            // Set click listeners
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onConversationClick(conversation);
                }
            });

            cardView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onConversationLongClick(conversation);
                    return true;
                }
                return false;
            });
        }

        private void loadConversationAvatar(Conversation conversation) {
            String avatarUrl = conversation.getDisplayAvatar();

            RequestOptions requestOptions = new RequestOptions()
                    .placeholder(getDefaultAvatar(conversation))
                    .error(getDefaultAvatar(conversation))
                    .circleCrop();

            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                Glide.with(context)
                        .load(avatarUrl)
                        .apply(requestOptions)
                        .into(imageViewAvatar);
            } else {
                Glide.with(context)
                        .load(getDefaultAvatar(conversation))
                        .apply(requestOptions)
                        .into(imageViewAvatar);
            }
        }

        private int getDefaultAvatar(Conversation conversation) {
            if (conversation.isGroupChat()) {
                return R.drawable.ic_people; // Group default icon
            } else {
                return R.drawable.default_avatar; // User default avatar
            }
        }
    }

    /**
     * Update conversations list
     */
    public void updateConversations(List<Conversation> newConversations) {
        this.conversationsList.clear();
        this.conversationsList.addAll(newConversations);
        notifyDataSetChanged();
    }

    /**
     * Add more conversations (for pagination)
     */
    public void addConversations(List<Conversation> newConversations) {
        int startPosition = this.conversationsList.size();
        this.conversationsList.addAll(newConversations);
        notifyItemRangeInserted(startPosition, newConversations.size());
    }

    /**
     * Remove conversation from list
     */
    public void removeConversation(Conversation conversation) {
        int position = conversationsList.indexOf(conversation);
        if (position != -1) {
            conversationsList.remove(position);
            notifyItemRemoved(position);
        }
    }

    /**
     * Update specific conversation
     */
    public void updateConversation(Conversation conversation) {
        int position = findConversationPosition(conversation);
        if (position != -1) {
            conversationsList.set(position, conversation);
            notifyItemChanged(position);
        }
    }

    /**
     * Move conversation to top (for new messages)
     */
    public void moveConversationToTop(Conversation conversation) {
        int position = findConversationPosition(conversation);
        if (position != -1 && position != 0) {
            conversationsList.remove(position);
            conversationsList.add(0, conversation);
            notifyItemMoved(position, 0);
            notifyItemChanged(0); // Update the content
        } else if (position == -1) {
            // Add new conversation at top
            conversationsList.add(0, conversation);
            notifyItemInserted(0);
        }
    }

    /**
     * Clear all conversations
     */
    public void clearConversations() {
        conversationsList.clear();
        notifyDataSetChanged();
    }

    /**
     * Find conversation position by ID
     */
    private int findConversationPosition(Conversation conversation) {
        for (int i = 0; i < conversationsList.size(); i++) {
            if (conversationsList.get(i).getId().equals(conversation.getId())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Get conversation at position
     */
    public Conversation getConversationAt(int position) {
        if (position >= 0 && position < conversationsList.size()) {
            return conversationsList.get(position);
        }
        return null;
    }
}