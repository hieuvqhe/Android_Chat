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
import com.example.chat.models.Message;
import com.example.chat.utils.TimeUtils;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private List<Message> messagesList;
    private OnMessageActionListener listener;
    private Context context;
    private String currentUserId;

    public interface OnMessageActionListener {
        void onMessageClick(Message message);
        void onMessageLongClick(Message message);
        void onReplyClick(Message message);
        void onEditMessage(Message message);
        void onDeleteMessage(Message message);
        void onReactionClick(Message message);
    }

    public MessagesAdapter(List<Message> messagesList, String currentUserId, OnMessageActionListener listener) {
        this.messagesList = messagesList;
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messagesList.get(position);
        if (message.getSenderId().equals(currentUserId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messagesList.get(position);

        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).bind(message);
        } else if (holder instanceof ReceivedMessageViewHolder) {
            ((ReceivedMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    // Sent Message ViewHolder
    class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView messageCard;
        private TextView textViewMessage;
        private TextView textViewTime;
        private TextView textViewEdited;
        private ImageView imageViewStatus;
        private View replyContainer;
        private TextView textViewReplyTo;
        private TextView textViewReplyContent;

        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageCard = itemView.findViewById(R.id.messageCard);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
            textViewTime = itemView.findViewById(R.id.textViewTime);
            textViewEdited = itemView.findViewById(R.id.textViewEdited);
            imageViewStatus = itemView.findViewById(R.id.imageViewStatus);
            replyContainer = itemView.findViewById(R.id.replyContainer);
            textViewReplyTo = itemView.findViewById(R.id.textViewReplyTo);
            textViewReplyContent = itemView.findViewById(R.id.textViewReplyContent);
        }

        public void bind(Message message) {
            // Set message content
            textViewMessage.setText(message.getContent());

            // Set time
            if (message.getCreatedAt() != null) {
                textViewTime.setText(TimeUtils.getMessageTime(message.getCreatedAt()));
            }

            // Set edited indicator
            if (message.isEdited()) {
                textViewEdited.setVisibility(View.VISIBLE);
            } else {
                textViewEdited.setVisibility(View.GONE);
            }

            // Set message status
            setMessageStatus(message);

            // Set reply if exists
            setReplyInfo(message);

            // Set click listeners
            messageCard.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMessageClick(message);
                }
            });

            messageCard.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onMessageLongClick(message);
                    return true;
                }
                return false;
            });
        }

        private void setMessageStatus(Message message) {
            switch (message.getStatus()) {
                case 0: // Sent
                    imageViewStatus.setImageResource(R.drawable.ic_check);
                    imageViewStatus.setColorFilter(context.getColor(R.color.gray_medium));
                    break;
                case 1: // Delivered
                    imageViewStatus.setImageResource(R.drawable.ic_check);
                    imageViewStatus.setColorFilter(context.getColor(R.color.gray_dark));
                    break;
                case 2: // Read
                    imageViewStatus.setImageResource(R.drawable.ic_check);
                    imageViewStatus.setColorFilter(context.getColor(R.color.colorPrimary));
                    break;
            }
        }

        private void setReplyInfo(Message message) {
            if (message.hasReply() && message.getReplyToMessage() != null) {
                replyContainer.setVisibility(View.VISIBLE);
                Message replyMessage = message.getReplyToMessage();

                textViewReplyTo.setText(replyMessage.getSenderName());
                textViewReplyContent.setText(replyMessage.getContent());
            } else {
                replyContainer.setVisibility(View.GONE);
            }
        }
    }

    // Received Message ViewHolder
    class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView messageCard;
        private ImageView imageViewAvatar;
        private TextView textViewSenderName;
        private TextView textViewMessage;
        private TextView textViewTime;
        private TextView textViewEdited;
        private View replyContainer;
        private TextView textViewReplyTo;
        private TextView textViewReplyContent;

        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageCard = itemView.findViewById(R.id.messageCard);
            imageViewAvatar = itemView.findViewById(R.id.imageViewAvatar);
            textViewSenderName = itemView.findViewById(R.id.textViewSenderName);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
            textViewTime = itemView.findViewById(R.id.textViewTime);
            textViewEdited = itemView.findViewById(R.id.textViewEdited);
            replyContainer = itemView.findViewById(R.id.replyContainer);
            textViewReplyTo = itemView.findViewById(R.id.textViewReplyTo);
            textViewReplyContent = itemView.findViewById(R.id.textViewReplyContent);
        }

        public void bind(Message message) {
            // Set sender name
            textViewSenderName.setText(message.getSenderName());

            // Set message content
            textViewMessage.setText(message.getContent());

            // Set time
            if (message.getCreatedAt() != null) {
                textViewTime.setText(TimeUtils.getMessageTime(message.getCreatedAt()));
            }

            // Set edited indicator
            if (message.isEdited()) {
                textViewEdited.setVisibility(View.VISIBLE);
            } else {
                textViewEdited.setVisibility(View.GONE);
            }

            // Set sender avatar
            loadSenderAvatar(message);

            // Set reply if exists
            setReplyInfo(message);

            // Set click listeners
            messageCard.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMessageClick(message);
                }
            });

            messageCard.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onMessageLongClick(message);
                    return true;
                }
                return false;
            });
        }

        private void loadSenderAvatar(Message message) {
            String avatarUrl = message.getSenderAvatar();

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

        private void setReplyInfo(Message message) {
            if (message.hasReply() && message.getReplyToMessage() != null) {
                replyContainer.setVisibility(View.VISIBLE);
                Message replyMessage = message.getReplyToMessage();

                textViewReplyTo.setText(replyMessage.getSenderName());
                textViewReplyContent.setText(replyMessage.getContent());
            } else {
                replyContainer.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Update messages list
     */
    public void updateMessages(List<Message> newMessages) {
        this.messagesList.clear();
        this.messagesList.addAll(newMessages);
        notifyDataSetChanged();
    }

    /**
     * Add new message at the end
     */
    public void addMessage(Message message) {
        messagesList.add(message);
        notifyItemInserted(messagesList.size() - 1);
    }

    /**
     * Add messages at the beginning (for pagination)
     */
    public void addMessagesAtBeginning(List<Message> newMessages) {
        messagesList.addAll(0, newMessages);
        notifyItemRangeInserted(0, newMessages.size());
    }

    /**
     * Update specific message
     */
    public void updateMessage(Message message) {
        int position = findMessagePosition(message);
        if (position != -1) {
            messagesList.set(position, message);
            notifyItemChanged(position);
        }
    }

    /**
     * Remove message
     */
    public void removeMessage(Message message) {
        int position = findMessagePosition(message);
        if (position != -1) {
            messagesList.remove(position);
            notifyItemRemoved(position);
        }
    }

    /**
     * Remove message by ID
     */
    public void removeMessageById(String messageId) {
        for (int i = 0; i < messagesList.size(); i++) {
            if (messagesList.get(i).getId().equals(messageId)) {
                messagesList.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    /**
     * Clear all messages
     */
    public void clearMessages() {
        messagesList.clear();
        notifyDataSetChanged();
    }

    /**
     * Find message position by ID
     */
    private int findMessagePosition(Message message) {
        for (int i = 0; i < messagesList.size(); i++) {
            if (messagesList.get(i).getId().equals(message.getId())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Get message at position
     */
    public Message getMessageAt(int position) {
        if (position >= 0 && position < messagesList.size()) {
            return messagesList.get(position);
        }
        return null;
    }

    /**
     * Check if should show date separator
     */
    public boolean shouldShowDateSeparator(int position) {
        if (position == 0) return true;

        Message currentMessage = messagesList.get(position);
        Message previousMessage = messagesList.get(position - 1);

        if (currentMessage.getCreatedAt() == null || previousMessage.getCreatedAt() == null) {
            return false;
        }

        return !TimeUtils.isSameDay(currentMessage.getCreatedAt(), previousMessage.getCreatedAt());
    }

    /**
     * Get date for separator
     */
    public String getDateSeparatorText(int position) {
        Message message = messagesList.get(position);
        if (message.getCreatedAt() != null) {
            return TimeUtils.getDateSeparator(message.getCreatedAt());
        }
        return "";
    }
}