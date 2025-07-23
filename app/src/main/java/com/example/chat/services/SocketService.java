package com.example.chat.services;

import android.content.Context;
import android.util.Log;
import com.example.chat.models.Message;
import com.example.chat.models.Notification;
import com.example.chat.network.NetworkManager;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.URISyntaxException;
import java.util.concurrent.CopyOnWriteArrayList;

public class SocketService {
    private static final String TAG = "SocketService";
    private static final String SOCKET_URL = "https://realtime-chat-app-tam7.onrender.com/";
    
    private static SocketService instance;
    private Socket socket;
    private NetworkManager networkManager;
    private boolean isConnected = false;
    
    // Listeners for different events
    private final CopyOnWriteArrayList<MessageListener> messageListeners = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<NotificationListener> notificationListeners = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<ConnectionListener> connectionListeners = new CopyOnWriteArrayList<>();
    
    // Interfaces for callbacks
    public interface MessageListener {
        void onNewMessage(Message message);
        void onMessageUpdated(Message message);
        void onMessageDeleted(String messageId);
        void onTypingStart(String userId, String conversationId);
        void onTypingStop(String userId, String conversationId);
    }
    
    public interface NotificationListener {
        void onNewNotification(Notification notification);
        void onNotificationRead(String notificationId);
        void onNotificationDeleted(String notificationId);
    }
    
    public interface ConnectionListener {
        void onConnected();
        void onDisconnected();
        void onError(String error);
    }
    
    private SocketService(Context context) {
        this.networkManager = NetworkManager.getInstance(context);
        initSocket();
    }
    
    public static synchronized SocketService getInstance(Context context) {
        if (instance == null) {
            instance = new SocketService(context.getApplicationContext());
        }
        return instance;
    }
    
    private void initSocket() {
        try {
            IO.Options options = new IO.Options();
            options.timeout = 10000;
            options.reconnection = true;
            options.reconnectionAttempts = 5;
            options.reconnectionDelay = 1000;
            
            socket = IO.socket(SOCKET_URL, options);
            setupSocketListeners();
            
        } catch (URISyntaxException e) {
            Log.e(TAG, "Socket initialization error", e);
        }
    }
    
    private void setupSocketListeners() {
        // Connection events
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d(TAG, "Socket connected");
                isConnected = true;
                authenticateSocket();
                notifyConnectionListeners(true);
            }
        });
        
        socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d(TAG, "Socket disconnected");
                isConnected = false;
                notifyConnectionListeners(false);
            }
        });
        
        socket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.e(TAG, "Socket connection error: " + args[0]);
                notifyConnectionError(args[0].toString());
            }
        });
        
        // Message events
        socket.on("new_message", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                handleNewMessage(args[0]);
            }
        });
        
        socket.on("message_updated", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                handleMessageUpdated(args[0]);
            }
        });
        
        socket.on("message_deleted", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                handleMessageDeleted(args[0]);
            }
        });
        
        // Typing events
        socket.on("typing_start", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                handleTypingStart(args[0]);
            }
        });
        
        socket.on("typing_stop", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                handleTypingStop(args[0]);
            }
        });
        
        // Notification events
        socket.on("new_notification", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                handleNewNotification(args[0]);
            }
        });
        
        socket.on("notification_read", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                handleNotificationRead(args[0]);
            }
        });
    }
    
    private void authenticateSocket() {
        String accessToken = networkManager.getAccessToken();
        if (accessToken != null) {
            JSONObject authData = new JSONObject();
            try {
                authData.put("token", accessToken);
                socket.emit("authenticate", authData);
                Log.d(TAG, "Socket authentication sent");
            } catch (JSONException e) {
                Log.e(TAG, "Error creating auth data", e);
            }
        }
    }
    
    // Connection management
    public void connect() {
        if (socket != null && !isConnected) {
            socket.connect();
            Log.d(TAG, "Connecting socket...");
        }
    }
    
    public void disconnect() {
        if (socket != null && isConnected) {
            socket.disconnect();
            Log.d(TAG, "Disconnecting socket...");
        }
    }
    
    public boolean isConnected() {
        return isConnected && socket != null && socket.connected();
    }
    
    // Join/Leave conversation rooms
    public void joinConversation(String conversationId) {
        if (isConnected()) {
            JSONObject data = new JSONObject();
            try {
                data.put("conversation_id", conversationId);
                socket.emit("join_conversation", data);
                Log.d(TAG, "Joined conversation: " + conversationId);
            } catch (JSONException e) {
                Log.e(TAG, "Error joining conversation", e);
            }
        }
    }
    
    public void leaveConversation(String conversationId) {
        if (isConnected()) {
            JSONObject data = new JSONObject();
            try {
                data.put("conversation_id", conversationId);
                socket.emit("leave_conversation", data);
                Log.d(TAG, "Left conversation: " + conversationId);
            } catch (JSONException e) {
                Log.e(TAG, "Error leaving conversation", e);
            }
        }
    }
    
    // Typing indicators
    public void sendTypingStart(String conversationId) {
        if (isConnected()) {
            JSONObject data = new JSONObject();
            try {
                data.put("conversation_id", conversationId);
                socket.emit("typing_start", data);
            } catch (JSONException e) {
                Log.e(TAG, "Error sending typing start", e);
            }
        }
    }
    
    public void sendTypingStop(String conversationId) {
        if (isConnected()) {
            JSONObject data = new JSONObject();
            try {
                data.put("conversation_id", conversationId);
                socket.emit("typing_stop", data);
            } catch (JSONException e) {
                Log.e(TAG, "Error sending typing stop", e);
            }
        }
    }
    
    // Listener management
    public void addMessageListener(MessageListener listener) {
        messageListeners.add(listener);
    }
    
    public void removeMessageListener(MessageListener listener) {
        messageListeners.remove(listener);
    }
    
    public void addNotificationListener(NotificationListener listener) {
        notificationListeners.add(listener);
    }
    
    public void removeNotificationListener(NotificationListener listener) {
        notificationListeners.remove(listener);
    }
    
    public void addConnectionListener(ConnectionListener listener) {
        connectionListeners.add(listener);
    }
    
    public void removeConnectionListener(ConnectionListener listener) {
        connectionListeners.remove(listener);
    }
    
    // Event handlers
    private void handleNewMessage(Object data) {
        try {
            JSONObject messageData = (JSONObject) data;
            // Parse message from JSON - you may need to use Gson here
            Log.d(TAG, "New message received: " + messageData.toString());

            // Notify all message listeners
            for (MessageListener listener : messageListeners) {
                // You'll need to parse JSON to Message object
                // Message message = parseMessageFromJson(messageData);
                // listener.onNewMessage(message);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling new message", e);
        }
    }

    private void handleMessageUpdated(Object data) {
        try {
            JSONObject messageData = (JSONObject) data;
            Log.d(TAG, "Message updated: " + messageData.toString());

            for (MessageListener listener : messageListeners) {
                // Message message = parseMessageFromJson(messageData);
                // listener.onMessageUpdated(message);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling message update", e);
        }
    }

    private void handleMessageDeleted(Object data) {
        try {
            JSONObject messageData = (JSONObject) data;
            String messageId = messageData.getString("message_id");
            Log.d(TAG, "Message deleted: " + messageId);

            for (MessageListener listener : messageListeners) {
                listener.onMessageDeleted(messageId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling message deletion", e);
        }
    }

    private void handleTypingStart(Object data) {
        try {
            JSONObject typingData = (JSONObject) data;
            String userId = typingData.getString("user_id");
            String conversationId = typingData.getString("conversation_id");

            for (MessageListener listener : messageListeners) {
                listener.onTypingStart(userId, conversationId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling typing start", e);
        }
    }

    private void handleTypingStop(Object data) {
        try {
            JSONObject typingData = (JSONObject) data;
            String userId = typingData.getString("user_id");
            String conversationId = typingData.getString("conversation_id");

            for (MessageListener listener : messageListeners) {
                listener.onTypingStop(userId, conversationId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling typing stop", e);
        }
    }

    private void handleNewNotification(Object data) {
        try {
            JSONObject notificationData = (JSONObject) data;
            Log.d(TAG, "New notification: " + notificationData.toString());

            for (NotificationListener listener : notificationListeners) {
                // Notification notification = parseNotificationFromJson(notificationData);
                // listener.onNewNotification(notification);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling new notification", e);
        }
    }

    private void handleNotificationRead(Object data) {
        try {
            JSONObject notificationData = (JSONObject) data;
            String notificationId = notificationData.getString("notification_id");

            for (NotificationListener listener : notificationListeners) {
                listener.onNotificationRead(notificationId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling notification read", e);
        }
    }

    private void notifyConnectionListeners(boolean connected) {
        for (ConnectionListener listener : connectionListeners) {
            if (connected) {
                listener.onConnected();
            } else {
                listener.onDisconnected();
            }
        }
    }

    private void notifyConnectionError(String error) {
        for (ConnectionListener listener : connectionListeners) {
            listener.onError(error);
        }
    }

    // Cleanup
    public void destroy() {
        if (socket != null) {
            socket.disconnect();
            socket.off();
        }
        messageListeners.clear();
        notificationListeners.clear();
        connectionListeners.clear();
        instance = null;
    }
}
