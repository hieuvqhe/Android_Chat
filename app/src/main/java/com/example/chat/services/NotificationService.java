package com.example.chat.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.example.chat.ChatActivity;
import com.example.chat.MainActivity;
import com.example.chat.R;
import com.example.chat.api.ApiService;
import com.example.chat.models.ApiResponse;
import com.example.chat.models.Notification;
import com.example.chat.network.ApiCallback;
import com.example.chat.network.NetworkManager;
import retrofit2.Call;
import java.util.List;

public class NotificationService {
    private static final String TAG = "NotificationService";
    private static final String CHANNEL_ID = "chat_notifications";
    private static final String CHANNEL_NAME = "Chat Notifications";
    private static final String CHANNEL_DESCRIPTION = "Notifications for new messages and activities";
    
    private static NotificationService instance;
    private Context context;
    private ApiService apiService;
    private NetworkManager networkManager;
    private NotificationManagerCompat notificationManager;
    
    // Notification IDs
    private static final int MESSAGE_NOTIFICATION_ID = 1001;
    private static final int FRIEND_REQUEST_NOTIFICATION_ID = 1002;
    private static final int GROUP_INVITE_NOTIFICATION_ID = 1003;
    
    private NotificationService(Context context) {
        this.context = context.getApplicationContext();
        this.networkManager = NetworkManager.getInstance(context);
        this.apiService = networkManager.getApiService();
        this.notificationManager = NotificationManagerCompat.from(this.context);
        createNotificationChannel();
    }
    
    public static synchronized NotificationService getInstance(Context context) {
        if (instance == null) {
            instance = new NotificationService(context);
        }
        return instance;
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableLights(true);
            channel.enableVibration(true);
            
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
    
    // API calls for notifications
    public void getNotifications(int page, int limit, String status, ApiCallback<List<Notification>> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }
        
        Call<ApiResponse<List<Object>>> call = apiService.getNotifications(authHeader, page, limit, status);
        call.enqueue(new retrofit2.Callback<ApiResponse<List<Object>>>() {
            @Override
            public void onResponse(retrofit2.Call<ApiResponse<List<Object>>> call, retrofit2.Response<ApiResponse<List<Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // TODO: Parse Object list to Notification list
                    // For now, return empty list
                    callback.onSuccess(null, "Notifications retrieved");
                } else {
                    callback.onError(response.code(), "Failed to get notifications");
                }
            }
            
            @Override
            public void onFailure(retrofit2.Call<ApiResponse<List<Object>>> call, Throwable t) {
                callback.onNetworkError(t.getMessage());
            }
        });
    }
    
    public void markNotificationRead(String notificationId, ApiCallback<Object> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }
        
        Call<ApiResponse<Object>> call = apiService.markNotificationRead(authHeader, notificationId);
        call.enqueue(callback);
    }
    
    public void markAllNotificationsRead(ApiCallback<Object> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }
        
        Call<ApiResponse<Object>> call = apiService.markAllNotificationsRead(authHeader);
        call.enqueue(callback);
    }
    
    public void deleteNotification(String notificationId, ApiCallback<Object> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }
        
        Call<ApiResponse<Object>> call = apiService.deleteNotification(authHeader, notificationId);
        call.enqueue(callback);
    }
    
    public void getUnreadNotificationsCount(ApiCallback<Object> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }
        
        Call<ApiResponse<Object>> call = apiService.getUnreadNotificationsCount(authHeader);
        call.enqueue(callback);
    }
    
    public void clearAllNotifications(ApiCallback<Object> callback) {
        String authHeader = networkManager.getAuthorizationHeader();
        if (authHeader == null) {
            callback.onError(401, "Please log in first");
            return;
        }
        
        Call<ApiResponse<Object>> call = apiService.clearAllNotifications(authHeader);
        call.enqueue(callback);
    }
    
    // Local notification display methods
    public void showMessageNotification(String title, String content, String conversationId, String conversationName) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_CONVERSATION_ID, conversationId);
        intent.putExtra(ChatActivity.EXTRA_CONVERSATION_NAME, conversationName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 
            MESSAGE_NOTIFICATION_ID, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_message)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL);
        
        notificationManager.notify(MESSAGE_NOTIFICATION_ID, builder.build());
    }
    
    public void showFriendRequestNotification(String title, String content, String friendRequestId) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("open_friends", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 
            FRIEND_REQUEST_NOTIFICATION_ID, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_person_add)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL);
        
        notificationManager.notify(FRIEND_REQUEST_NOTIFICATION_ID, builder.build());
    }
    
    public void showGroupInviteNotification(String title, String content, String groupId) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("open_groups", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 
            GROUP_INVITE_NOTIFICATION_ID, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_group)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL);
        
        notificationManager.notify(GROUP_INVITE_NOTIFICATION_ID, builder.build());
    }
    
    public void cancelNotification(int notificationId) {
        notificationManager.cancel(notificationId);
    }
    
    public void cancelAllNotifications() {
        notificationManager.cancelAll();
    }
}
