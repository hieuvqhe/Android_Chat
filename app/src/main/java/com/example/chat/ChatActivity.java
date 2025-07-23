package com.example.chat;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.chat.adapters.MessagesAdapter;
import com.example.chat.models.Conversation;
import com.example.chat.models.Message;
import com.example.chat.network.ApiCallback;
import com.example.chat.network.NetworkManager;
import com.example.chat.services.ConversationService;
import com.example.chat.services.MessageService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";

    // Intent extras
    public static final String EXTRA_CONVERSATION_ID = "conversation_id";
    public static final String EXTRA_CONVERSATION_NAME = "conversation_name";
    public static final String EXTRA_CONVERSATION_TYPE = "conversation_type";
    public static final String EXTRA_OTHER_USER_ID = "other_user_id";

    // Views
    private MaterialToolbar toolbar;
    private ImageView imageViewAvatar;
    private TextView textViewName;
    private TextView textViewStatus;
    private RecyclerView recyclerViewMessages;
    private TextInputEditText editTextMessage;
    private MaterialButton buttonSend;
    private ProgressBar progressBar;
    private View layoutEmpty;
    private TextView textViewEmpty;

    // Data
    private String conversationId;
    private String conversationName;
    private int conversationType; // 0 = private, 1 = group
    private String otherUserId; // For private chats
    private String currentUserId;

    private List<Message> messagesList = new ArrayList<>();
    private MessagesAdapter messagesAdapter;

    // Services
    private MessageService messageService;
    private ConversationService conversationService;
    private NetworkManager networkManager;

    // Pagination
    private boolean isLoading = false;
    private boolean hasMoreMessages = true;
    private int currentPage = 1;
    private static final int PAGE_SIZE = 30;

    // Reply context
    private Message currentReplyToMessage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        getIntentData();
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupMessageInput();
        initServices();
        loadConversationDetails();
        loadMessages();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        conversationId = intent.getStringExtra(EXTRA_CONVERSATION_ID);
        conversationName = intent.getStringExtra(EXTRA_CONVERSATION_NAME);
        conversationType = intent.getIntExtra(EXTRA_CONVERSATION_TYPE, 0);
        otherUserId = intent.getStringExtra(EXTRA_OTHER_USER_ID);

        if (conversationId == null) {
            Toast.makeText(this, "Invalid conversation", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        imageViewAvatar = findViewById(R.id.imageViewAvatar);
        textViewName = findViewById(R.id.textViewName);
        textViewStatus = findViewById(R.id.textViewStatus);
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        textViewEmpty = findViewById(R.id.textViewEmpty);
    }

    private void setupToolbar() {
        try {
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setDisplayShowTitleEnabled(false); // We'll use custom title
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up toolbar", e);
        }

        // Set conversation name
        textViewName.setText(conversationName != null ? conversationName : "Chat");

        // Set status text
        if (conversationType == 1) {
            textViewStatus.setText("Group chat");
        } else {
            textViewStatus.setText("Online"); // You can implement real-time status later
        }

        // Make toolbar clickable for info
        View toolbarClickArea = findViewById(R.id.toolbarClickArea);
        if (toolbarClickArea != null) {
            toolbarClickArea.setOnClickListener(v -> openChatInfo());
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        networkManager = NetworkManager.getInstance(this);
        currentUserId = networkManager.getUserId();

        messagesAdapter = new MessagesAdapter(messagesList, currentUserId, new MessagesAdapter.OnMessageActionListener() {
            @Override
            public void onMessageClick(Message message) {
                // Handle message click
            }

            @Override
            public void onMessageLongClick(Message message) {
                showMessageOptions(message);
            }

            @Override
            public void onReplyClick(Message message) {
                startReply(message);
            }

            @Override
            public void onEditMessage(Message message) {
                editMessage(message);
            }

            @Override
            public void onDeleteMessage(Message message) {
                deleteMessage(message);
            }

            @Override
            public void onReactionClick(Message message) {
                // TODO: Implement reaction functionality
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Start from bottom
        recyclerViewMessages.setLayoutManager(layoutManager);
        recyclerViewMessages.setAdapter(messagesAdapter);

        // Setup pagination for loading older messages
        recyclerViewMessages.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && !isLoading && hasMoreMessages) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    // Load more when scrolled to top
                    if (firstVisibleItemPosition <= 3 && dy < 0) {
                        loadMoreMessages();
                    }
                }
            }
        });
    }

    private void setupMessageInput() {
        // Enable/disable send button based on text input
        editTextMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                buttonSend.setEnabled(s.toString().trim().length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Send message on button click
        buttonSend.setOnClickListener(v -> sendMessage());
        buttonSend.setEnabled(false);

        // Send message on Enter key (optional)
        editTextMessage.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    private void initServices() {
        messageService = new MessageService(networkManager);
        conversationService = new ConversationService(networkManager);
    }

    private void loadConversationDetails() {
        conversationService.getConversationDetails(conversationId, new ApiCallback<Conversation>() {
            @Override
            public void onSuccess(Conversation result, String message) {
                runOnUiThread(() -> {
                    updateToolbarWithConversation(result);
                });
            }

            @Override
            public void onError(int statusCode, String message) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Error loading conversation details: " + message);
                });
            }

            @Override
            public void onNetworkError(String message) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Network error loading conversation details: " + message);
                });
            }
        });
    }

    private void updateToolbarWithConversation(Conversation conversation) {
        // Update name
        textViewName.setText(conversation.getDisplayName());

        // Update avatar
        String avatarUrl = conversation.getDisplayAvatar();
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            RequestOptions requestOptions = new RequestOptions()
                    .placeholder(getDefaultAvatar())
                    .error(getDefaultAvatar())
                    .circleCrop();

            Glide.with(this)
                    .load(avatarUrl)
                    .apply(requestOptions)
                    .into(imageViewAvatar);
        } else {
            imageViewAvatar.setImageResource(getDefaultAvatar());
        }

        // Update status
        if (conversation.isGroupChat()) {
            textViewStatus.setText(conversation.getMemberCount() + " members");
        } else {
            textViewStatus.setText("Online"); // TODO: Implement real status
        }
    }

    private int getDefaultAvatar() {
        return conversationType == 1 ? R.drawable.ic_people : R.drawable.default_avatar;
    }

    private void loadMessages() {
        if (isLoading) return;

        showLoading(true);
        isLoading = true;

        messageService.getMessages(conversationId, currentPage, PAGE_SIZE, new MessageService.MessageCallback<List<Message>>() {
            @Override
            public void onSuccess(List<Message> messages, boolean hasMore) {
                runOnUiThread(() -> {
                    showLoading(false);
                    isLoading = false;
                    hasMoreMessages = hasMore;

                    if (messages != null) {
                        if (currentPage == 1) {
                            messagesList.clear();
                            messagesList.addAll(messages);
                            messagesAdapter.notifyDataSetChanged();

                            // Scroll to bottom for first load
                            if (!messages.isEmpty()) {
                                recyclerViewMessages.scrollToPosition(messages.size() - 1);
                            }
                        } else {
                            // Add older messages at the beginning
                            messagesAdapter.addMessagesAtBeginning(messages);
                        }

                        markMessagesAsRead();
                        updateEmptyState();
                    }

                    Log.d(TAG, "Loaded " + (messages != null ? messages.size() : 0) + " messages");
                });
            }

            @Override
            public void onError(int statusCode, String message) {
                runOnUiThread(() -> {
                    showLoading(false);
                    isLoading = false;
                    Toast.makeText(ChatActivity.this, "Error loading messages: " + message, Toast.LENGTH_SHORT).show();
                    updateEmptyState();
                });
            }

            @Override
            public void onNetworkError(String message) {
                runOnUiThread(() -> {
                    showLoading(false);
                    isLoading = false;
                    Toast.makeText(ChatActivity.this, "Network error: " + message, Toast.LENGTH_SHORT).show();
                    updateEmptyState();
                });
            }
        });
    }

    private void loadMoreMessages() {
        currentPage++;
        loadMessages();
    }

    private void sendMessage() {
        String content = editTextMessage.getText().toString().trim();
        if (content.isEmpty()) return;

        // Clear input immediately
        editTextMessage.setText("");
        buttonSend.setEnabled(false);

        // Check if this is a reply
        if (currentReplyToMessage != null) {
            messageService.replyToMessage(conversationId, content, currentReplyToMessage.getId(), new ApiCallback<Message>() {
                @Override
                public void onSuccess(Message result, String message) {
                    runOnUiThread(() -> {
                        messagesAdapter.addMessage(result);
                        recyclerViewMessages.scrollToPosition(messagesAdapter.getItemCount() - 1);
                        currentReplyToMessage = null; // Clear reply context
                    });
                }

                @Override
                public void onError(int statusCode, String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(ChatActivity.this, "Failed to send reply: " + error, Toast.LENGTH_SHORT).show();
                        editTextMessage.setText(content);
                        editTextMessage.setSelection(content.length());
                        currentReplyToMessage = null;
                    });
                }

                @Override
                public void onNetworkError(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(ChatActivity.this, "Network error. Reply not sent.", Toast.LENGTH_SHORT).show();
                        editTextMessage.setText(content);
                        editTextMessage.setSelection(content.length());
                        currentReplyToMessage = null;
                    });
                }
            });
        } else {
            messageService.sendMessage(conversationId, content, new ApiCallback<Message>() {
            @Override
            public void onSuccess(Message result, String message) {
                runOnUiThread(() -> {
                    // Add message to list
                    messagesAdapter.addMessage(result);
                    recyclerViewMessages.scrollToPosition(messagesAdapter.getItemCount() - 1);
                    updateEmptyState();

                    Log.d(TAG, "Message sent successfully");
                });
            }

            @Override
            public void onError(int statusCode, String message) {
                runOnUiThread(() -> {
                    Toast.makeText(ChatActivity.this, "Failed to send message: " + message, Toast.LENGTH_SHORT).show();
                    // Restore message text
                    editTextMessage.setText(content);
                    editTextMessage.setSelection(content.length());
                });
            }

            @Override
            public void onNetworkError(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(ChatActivity.this, "Network error. Message not sent.", Toast.LENGTH_SHORT).show();
                    // Restore message text
                    editTextMessage.setText(content);
                    editTextMessage.setSelection(content.length());
                });
            }
            });
        }
    }

    private void markMessagesAsRead() {
        messageService.markMessagesAsRead(conversationId, new ApiCallback<Object>() {
            @Override
            public void onSuccess(Object result, String message) {
                // Messages marked as read
                Log.d(TAG, "Messages marked as read");
            }

            @Override
            public void onError(int statusCode, String message) {
                Log.e(TAG, "Error marking messages as read: " + message);
            }

            @Override
            public void onNetworkError(String message) {
                Log.e(TAG, "Network error marking messages as read: " + message);
            }
        });
    }

    private void showMessageOptions(Message message) {
        boolean isMyMessage = message.getSenderId().equals(currentUserId);

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Message Options");

        List<String> options = new ArrayList<>();
        if (isMyMessage) {
            options.add("Edit");
            options.add("Delete");
        }
        options.add("Reply");
        options.add("Copy");

        builder.setItems(options.toArray(new String[0]), (dialog, which) -> {
            String option = options.get(which);
            switch (option) {
                case "Edit":
                    editMessage(message);
                    break;
                case "Delete":
                    confirmDeleteMessage(message);
                    break;
                case "Reply":
                    startReply(message);
                    break;
                case "Copy":
                    copyMessage(message);
                    break;
            }
        });

        builder.show();
    }

    private void editMessage(Message message) {
        // Create edit dialog
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Edit Message");

        // Create EditText for editing
        final com.google.android.material.textfield.TextInputEditText editText =
            new com.google.android.material.textfield.TextInputEditText(this);
        editText.setText(message.getContent());
        editText.setSelection(message.getContent().length());
        editText.setHint("Enter your message...");

        // Add padding
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        editText.setPadding(padding, padding, padding, padding);

        builder.setView(editText);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newContent = editText.getText().toString().trim();
            if (!newContent.isEmpty() && !newContent.equals(message.getContent())) {
                updateMessage(message, newContent);
            }
        });

        builder.setNegativeButton("Cancel", null);

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();

        // Focus and show keyboard
        editText.requestFocus();
        dialog.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    private void updateMessage(Message message, String newContent) {
        messageService.editMessage(message.getId(), newContent, new ApiCallback<Message>() {
            @Override
            public void onSuccess(Message result, String response) {
                runOnUiThread(() -> {
                    // Update message in adapter
                    messagesAdapter.updateMessage(result);
                    Toast.makeText(ChatActivity.this, "Message updated", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(int statusCode, String error) {
                runOnUiThread(() -> {
                    Toast.makeText(ChatActivity.this, "Failed to update message: " + error, Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onNetworkError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(ChatActivity.this, "Network error: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void confirmDeleteMessage(Message message) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Message")
                .setMessage("Are you sure you want to delete this message?")
                .setPositiveButton("Delete", (dialog, which) -> deleteMessage(message))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteMessage(Message message) {
        messageService.deleteMessage(message.getId(), new ApiCallback<Object>() {
            @Override
            public void onSuccess(Object result, String response) {
                runOnUiThread(() -> {
                    messagesAdapter.removeMessage(message);
                    Toast.makeText(ChatActivity.this, "Message deleted", Toast.LENGTH_SHORT).show();
                    updateEmptyState();
                });
            }

            @Override
            public void onError(int statusCode, String message) {
                runOnUiThread(() -> {
                    Toast.makeText(ChatActivity.this, "Error deleting message: " + message, Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onNetworkError(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(ChatActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void copyMessage(Message message) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Message", message.getContent());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Message copied", Toast.LENGTH_SHORT).show();
    }

    private void startReply(Message message) {
        // Set focus to input and add reply context
        editTextMessage.requestFocus();

        // Show reply preview (you can enhance this with a reply bar UI)
        String replyText = "Replying to: " + message.getContent().substring(0, Math.min(50, message.getContent().length()));
        if (message.getContent().length() > 50) {
            replyText += "...";
        }

        Toast.makeText(this, replyText, Toast.LENGTH_LONG).show();

        // Store reply context for when user sends message
        currentReplyToMessage = message;

        // You can add a reply bar UI here to show what message is being replied to
        // For now, we'll just modify the send behavior
    }

    private void openChatInfo() {
        if (conversationType == 1) {
            // Open group info
            Intent intent = new Intent(this, GroupActivity.class);
            intent.putExtra(GroupActivity.EXTRA_GROUP_ID, conversationId);
            startActivity(intent);
        } else {
            // Open user profile
            if (otherUserId != null) {
                Intent intent = new Intent(this, UserProfileActivity.class);
                intent.putExtra(UserProfileActivity.EXTRA_USERNAME, otherUserId);
                startActivity(intent);
            }
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show && currentPage == 1 ? View.VISIBLE : View.GONE);
    }

    private void updateEmptyState() {
        boolean isEmpty = messagesList.isEmpty();
        layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerViewMessages.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        if (isEmpty) {
            textViewEmpty.setText("No messages yet.\nStart the conversation!");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_chat_info) {
            openChatInfo();
            return true;
        } else if (itemId == R.id.action_search) {
            // TODO: Implement search in chat
            Toast.makeText(this, "Search feature coming soon", Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.action_clear_chat) {
            confirmClearChat();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void confirmClearChat() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Clear Chat")
                .setMessage("Are you sure you want to clear all messages in this chat?")
                .setPositiveButton("Clear", (dialog, which) -> clearChat())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void clearChat() {
        // TODO: Implement clear chat functionality
        Toast.makeText(this, "Clear chat feature coming soon", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}