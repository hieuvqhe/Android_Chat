package com.example.chat;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.chat.adapters.FindUsersAdapter;
import com.example.chat.models.User;
import com.example.chat.models.UserInfo;
import com.example.chat.network.ApiCallback;
import com.example.chat.network.NetworkManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    private static final String TAG = "SearchActivity";
    
    // Views
    private MaterialToolbar toolbar;
    private TextInputEditText editTextSearch;
    private RecyclerView recyclerViewResults;
    private ProgressBar progressBar;
    private TextView textViewEmpty;
    
    // Data
    private List<UserInfo> searchResults;
    private FindUsersAdapter adapter;
    
    // Services
    private NetworkManager networkManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        
        try {
            initViews();
            setupToolbar();
            setupRecyclerView();
            setupSearch();
            
            // Initialize services
            networkManager = NetworkManager.getInstance(this);
            
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            finish();
        }
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        editTextSearch = findViewById(R.id.editTextSearch);
        recyclerViewResults = findViewById(R.id.recyclerViewResults);
        progressBar = findViewById(R.id.progressBar);
        textViewEmpty = findViewById(R.id.textViewEmpty);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Search Users");
        }
    }
    
    private void setupRecyclerView() {
        searchResults = new ArrayList<>();
        adapter = new FindUsersAdapter(searchResults, new FindUsersAdapter.OnUserActionListener() {
            @Override
            public void onAddFriend(UserInfo user) {
                // Handle add friend - you can implement this
                android.widget.Toast.makeText(SearchActivity.this, "Add friend: " + user.getUsername(), android.widget.Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onViewProfile(UserInfo user) {
                openUserProfile(user);
            }
        });
        
        recyclerViewResults.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewResults.setAdapter(adapter);
    }
    
    private void setupSearch() {
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.length() >= 2) {
                    searchUsers(query);
                } else {
                    clearResults();
                }
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Focus on search input
        editTextSearch.requestFocus();
    }
    
    private void searchUsers(String query) {
        showLoading(true);
        
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            showError("Authentication required");
            return;
        }
        
        Log.d(TAG, "Searching users with query: " + query);
        
        networkManager.getApiService().searchUsers(authHeader, query, 1, 20)
            .enqueue(new ApiCallback<List<UserInfo>>() {
                @Override
                public void onSuccess(List<UserInfo> result, String message) {
                    Log.d(TAG, "Search successful, found " + result.size() + " users");

                    runOnUiThread(() -> {
                        searchResults.clear();
                        // Add UserInfo directly
                        searchResults.addAll(result);
                        adapter.notifyDataSetChanged();
                        showLoading(false);
                        updateEmptyState();
                    });
                }
                
                @Override
                public void onError(int statusCode, String error) {
                    Log.e(TAG, "Search failed: " + error);
                    
                    runOnUiThread(() -> {
                        showError("Search failed: " + error);
                        showLoading(false);
                    });
                }
                
                @Override
                public void onNetworkError(String error) {
                    Log.e(TAG, "Network error: " + error);
                    
                    runOnUiThread(() -> {
                        showError("Network error: " + error);
                        showLoading(false);
                    });
                }
            });
    }
    
    private void clearResults() {
        searchResults.clear();
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }
    
    private void openUserProfile(UserInfo user) {
        Intent intent = new Intent(this, ViewProfileActivity.class);
        intent.putExtra(ViewProfileActivity.EXTRA_USERNAME, user.getUsername());
        intent.putExtra(ViewProfileActivity.EXTRA_USER_ID, user.getId());
        startActivity(intent);
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerViewResults.setVisibility(show ? View.GONE : View.VISIBLE);
    }
    
    private void showError(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG).show();
    }
    
    private void updateEmptyState() {
        boolean isEmpty = searchResults.isEmpty();
        textViewEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerViewResults.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        
        String query = editTextSearch.getText().toString().trim();
        if (isEmpty && query.length() >= 2) {
            textViewEmpty.setText("No users found for \"" + query + "\"");
        } else if (isEmpty) {
            textViewEmpty.setText("Type at least 2 characters to search");
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
