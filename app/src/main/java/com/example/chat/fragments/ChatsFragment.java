package com.example.chat.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.chat.R;
import com.google.android.material.textfield.TextInputEditText;

public class ChatsFragment extends Fragment {
    private static final String TAG = "ChatsFragment";

    // Views
    private TextInputEditText editTextSearch;
    private RecyclerView recyclerViewChats;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView textViewEmpty;

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
        showEmptyState();
    }

    private void initViews(View view) {
        editTextSearch = view.findViewById(R.id.editTextSearch);
        recyclerViewChats = view.findViewById(R.id.recyclerViewChats);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        textViewEmpty = view.findViewById(R.id.textViewEmpty);
    }

    private void setupRecyclerView() {
        recyclerViewChats.setLayoutManager(new LinearLayoutManager(getContext()));

        // Setup swipe refresh
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // TODO: Implement refresh chats
            swipeRefreshLayout.setRefreshing(false);
        });

        swipeRefreshLayout.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorAccent
        );
    }

    private void setupSearch() {
        // TODO: Implement search functionality
    }

    private void showEmptyState() {
        textViewEmpty.setVisibility(View.VISIBLE);
        recyclerViewChats.setVisibility(View.GONE);
        textViewEmpty.setText("No conversations yet.\nStart chatting with your friends!");
    }
}