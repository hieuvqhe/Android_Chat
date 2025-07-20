package com.example.chat.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.chat.ChatActivity;
import com.example.chat.CreateGroupActivity;
import com.example.chat.R;
import com.example.chat.adapters.ConversationsAdapter;
import com.example.chat.models.Conversation;
import com.example.chat.network.NetworkManager;
import com.example.chat.services.ConversationService;
import com.example.chat.utils.PaginationHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;

public class ChatsFragment extends Fragment {
    private static final String TAG = "ChatsFragment";

    // Views
    private TextInputEditText editTextSearch;
    private RecyclerView recyclerViewChats;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView textViewEmpty;
    private ProgressBar progressBar;
    private FloatingActionButton fabCreateGroup;

    // Data
    private List<Conversation> conversationsList = new ArrayList<>();
    private ConversationsAdapter conversationsAdapter;
    private ConversationService conversationService;
    private PaginationHelper paginationHelper;

    // Search
    private String currentSearchQuery = "";
    private boolean isSearching = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupSearch();
        initConversationService();
        loadConversations();
    }

    private void initViews(View view) {
        editTextSearch = view.findViewById(R.id.editTextSearch);
        recyclerViewChats = view.findViewById(R.id.recyclerViewChats);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        textViewEmpty = view.findViewById(R.id.textViewEmpty);
        progressBar = view.findViewById(R.id.progressBar);
        fabCreateGroup = view.findViewById(R.id.fabCreateGroup);
    }

    private void setupRecyclerView() {
        paginationHelper = new PaginationHelper(20);

        conversationsAdapter = new ConversationsAdapter(conversationsList, new ConversationsAdapter.OnConversationActionListener() {
            @Override
            public void onConversationClick(Conversation conversation) {
                openChat(conversation);
            }

            @Override
            public void onConversationLongClick(Conversation conversation) {
                showConversationOptions(conversation);
            }

            @Override
            public void onDeleteConversation(Conversation conversation) {
                deleteConversation(conversation);
            }

            @Override
            public void onMuteConversation(Conversation conversation) {
                toggleMuteConversation(conversation);
            }

            @Override
            public void onPinConversation(Conversation conversation) {
                togglePinConversation(conversation);
            }
        });

        recyclerViewChats.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewChats.setAdapter(conversationsAdapter);

        // Setup pagination
        recyclerViewChats.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && paginationHelper.canLoadMore()) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 3) {
                        loadMoreConversations();
                    }
                }
            }
        });

        // Setup swipe refresh
        swipeRefreshLayout.setOnRefreshListener(() -> {
            refreshConversations();
        });

        swipeRefreshLayout.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorAccent
        );

        // Setup FAB for creating group
        if (fabCreateGroup != null) {
            fabCreateGroup.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), CreateGroupActivity.class);
                startActivity(intent);
            });
        }
    }

    private void setupSearch() {
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (!query.equals(currentSearchQuery)) {
                    currentSearchQuery = query;
                    searchConversations(query);
                }
            }
        });
    }

    private void initConversationService() {
        NetworkManager networkManager = NetworkManager.getInstance(getContext());
        conversationService = new ConversationService(networkManager);
    }

    /**
     * Load conversations
     */
    private void loadConversations() {
        if (paginationHelper.isLoading()) return;

        showLoading(true);
        paginationHelper.setLoading(true);

        conversationService.getAllConversations(paginationHelper.getCurrentPage(), paginationHelper.getPageSize(),
                new ConversationService.ConversationCallback<List<Conversation>>() {
                    @Override
                    public void onSuccess(List<Conversation> conversations, int page, int totalPages) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                showLoading(false);
                                swipeRefreshLayout.setRefreshing(false);

                                if (conversations != null) {
                                    if (page == 1) {
                                        conversationsList.clear();
                                    }
                                    conversationsList.addAll(conversations);
                                    conversationsAdapter.notifyDataSetChanged();

                                    paginationHelper.updatePagination(totalPages);

                                    updateEmptyState();

                                    Log.d(TAG, "Loaded " + conversations.size() + " conversations. Page " + page + "/" + totalPages);
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(int statusCode, String message) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                showLoading(false);
                                swipeRefreshLayout.setRefreshing(false);
                                paginationHelper.setLoading(false);

                                Toast.makeText(getContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
                                updateEmptyState();
                                Log.e(TAG, "Error loading conversations: " + message);
                            });
                        }
                    }

                    @Override
                    public void onNetworkError(String message) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                showLoading(false);
                                swipeRefreshLayout.setRefreshing(false);
                                paginationHelper.setLoading(false);

                                Toast.makeText(getContext(), "Network error: " + message, Toast.LENGTH_SHORT).show();
                                updateEmptyState();
                                Log.e(TAG, "Network error: " + message);
                            });
                        }
                    }
                });
    }

    /**
     * Load more conversations for pagination
     */
    private void loadMoreConversations() {
        if (isSearching) return; // Don't paginate during search

        paginationHelper.incrementPage();
        loadConversations();
    }

    /**
     * Refresh conversations
     */
    private void refreshConversations() {
        paginationHelper.reset();
        isSearching = false;
        currentSearchQuery = "";
        editTextSearch.setText("");
        loadConversations();
    }

    /**
     * Search conversations
     */
    private void searchConversations(String query) {
        if (query.isEmpty()) {
            // Reset to normal list
            isSearching = false;
            refreshConversations();
            return;
        }

        if (query.length() < 2) {
            // Don't search with less than 2 characters
            return;
        }

        isSearching = true;
        showLoading(true);

        conversationService.searchConversations(query, 1, 50, // Search first 50 results
                new ConversationService.ConversationCallback<List<Conversation>>() {
                    @Override
                    public void onSuccess(List<Conversation> conversations, int page, int totalPages) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                showLoading(false);

                                conversationsList.clear();
                                if (conversations != null) {
                                    conversationsList.addAll(conversations);
                                }
                                conversationsAdapter.notifyDataSetChanged();
                                updateEmptyState();

                                Log.d(TAG, "Search results: " + (conversations != null ? conversations.size() : 0) + " conversations");
                            });
                        }
                    }

                    @Override
                    public void onError(int statusCode, String message) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                showLoading(false);
                                Toast.makeText(getContext(), "Search error: " + message, Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Search error: " + message);
                            });
                        }
                    }

                    @Override
                    public void onNetworkError(String message) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                showLoading(false);
                                Toast.makeText(getContext(), "Network error: " + message, Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Network error: " + message);
                            });
                        }
                    }
                });
    }

    /**
     * Open chat activity
     */
    private void openChat(Conversation conversation) {
        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_CONVERSATION_ID, conversation.getId());
        intent.putExtra(ChatActivity.EXTRA_CONVERSATION_NAME, conversation.getDisplayName());
        intent.putExtra(ChatActivity.EXTRA_CONVERSATION_TYPE, conversation.getType());

        if (conversation.isPrivateChat() && conversation.getOtherParticipant() != null) {
            intent.putExtra(ChatActivity.EXTRA_OTHER_USER_ID, conversation.getOtherParticipant().getId());
        }

        startActivity(intent);
    }

    /**
     * Show conversation options
     */
    private void showConversationOptions(Conversation conversation) {
        try {
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
            builder.setTitle(conversation.getDisplayName());

            String[] options;
            if (conversation.isPinned()) {
                options = new String[]{"Open Chat", "Unpin", conversation.isMuted() ? "Unmute" : "Mute", "Delete"};
            } else {
                options = new String[]{"Open Chat", "Pin", conversation.isMuted() ? "Unmute" : "Mute", "Delete"};
            }

            builder.setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0: // Open Chat
                        openChat(conversation);
                        break;
                    case 1: // Pin/Unpin
                        togglePinConversation(conversation);
                        break;
                    case 2: // Mute/Unmute
                        toggleMuteConversation(conversation);
                        break;
                    case 3: // Delete
                        confirmDeleteConversation(conversation);
                        break;
                }
            });

            builder.show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing conversation options", e);
            openChat(conversation); // Fallback to opening chat
        }
    }

    /**
     * Toggle pin conversation
     */
    private void togglePinConversation(Conversation conversation) {
        if (conversation.isPinned()) {
            // Unpin
            conversationService.unpinConversation(conversation.getId(), new com.example.chat.network.ApiCallback<Object>() {
                @Override
                public void onSuccess(Object result, String message) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Conversation unpinned", Toast.LENGTH_SHORT).show();
                            refreshConversations();
                        });
                    }
                }

                @Override
                public void onError(int statusCode, String message) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
                        });
                    }
                }

                @Override
                public void onNetworkError(String message) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });
        } else {
            // Pin
            conversationService.pinConversation(conversation.getId(), new com.example.chat.network.ApiCallback<Object>() {
                @Override
                public void onSuccess(Object result, String message) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Conversation pinned", Toast.LENGTH_SHORT).show();
                            refreshConversations();
                        });
                    }
                }

                @Override
                public void onError(int statusCode, String message) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
                        });
                    }
                }

                @Override
                public void onNetworkError(String message) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });
        }
    }

    /**
     * Toggle mute conversation
     */
    private void toggleMuteConversation(Conversation conversation) {
        if (conversation.isMuted()) {
            // Unmute
            conversationService.unmuteConversation(conversation.getId(), new com.example.chat.network.ApiCallback<Object>() {
                @Override
                public void onSuccess(Object result, String message) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Conversation unmuted", Toast.LENGTH_SHORT).show();
                            refreshConversations();
                        });
                    }
                }

                @Override
                public void onError(int statusCode, String message) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
                        });
                    }
                }

                @Override
                public void onNetworkError(String message) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });
        } else {
            // Mute
            conversationService.muteConversation(conversation.getId(), new com.example.chat.network.ApiCallback<Object>() {
                @Override
                public void onSuccess(Object result, String message) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Conversation muted", Toast.LENGTH_SHORT).show();
                            refreshConversations();
                        });
                    }
                }

                @Override
                public void onError(int statusCode, String message) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
                        });
                    }
                }

                @Override
                public void onNetworkError(String message) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });
        }
    }

    /**
     * Confirm delete conversation
     */
    private void confirmDeleteConversation(Conversation conversation) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete Conversation")
                .setMessage("Are you sure you want to delete this conversation? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteConversation(conversation))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Delete conversation
     */
    private void deleteConversation(Conversation conversation) {
        conversationService.deleteConversation(conversation.getId(), new com.example.chat.network.ApiCallback<Object>() {
            @Override
            public void onSuccess(Object result, String message) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Conversation deleted", Toast.LENGTH_SHORT).show();
                        conversationsAdapter.removeConversation(conversation);
                        updateEmptyState();
                    });
                }
            }

            @Override
            public void onError(int statusCode, String message) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onNetworkError(String message) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    /**
     * Show loading
     */
    private void showLoading(boolean show) {
        if (paginationHelper.getCurrentPage() == 1) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Update empty state
     */
    private void updateEmptyState() {
        boolean isEmpty = conversationsList.isEmpty();
        textViewEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerViewChats.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        if (isEmpty) {
            if (isSearching) {
                textViewEmpty.setText("No conversations found for \"" + currentSearchQuery + "\"");
            } else {
                textViewEmpty.setText("No conversations yet.\nStart chatting with your friends or create a group!");
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh conversations when returning to fragment
        if (!isSearching) {
            refreshConversations();
        }
    }
}