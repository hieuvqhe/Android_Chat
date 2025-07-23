package com.example.chat;

import android.app.Application;
import android.util.Log;
import com.example.chat.network.NetworkManager;
import com.example.chat.services.NotificationService;
import com.example.chat.services.SocketService;

public class ChatApplication extends Application {
    private static final String TAG = "ChatApplication";
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        try {
            // Initialize NetworkManager early to catch any initialization issues
            NetworkManager.getInstance(this);
            Log.d(TAG, "NetworkManager initialized successfully");

            // Initialize NotificationService
            NotificationService.getInstance(this);
            Log.d(TAG, "NotificationService initialized successfully");

            // Initialize SocketService
            SocketService.getInstance(this);
            Log.d(TAG, "SocketService initialized successfully");

        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize services", e);
            // Don't crash the app, just log the error
            // The app will handle this gracefully in MainActivity
        }
        
        // Set up global exception handler
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                Log.e(TAG, "Uncaught exception in thread " + thread.getName(), ex);
                
                // Log the exception details
                Log.e(TAG, "Exception class: " + ex.getClass().getSimpleName());
                Log.e(TAG, "Exception message: " + ex.getMessage());
                
                // Print stack trace to logcat
                ex.printStackTrace();
                
                // Call the default handler to crash the app
                // This ensures the system handles the crash properly
                Thread.getDefaultUncaughtExceptionHandler().uncaughtException(thread, ex);
            }
        });
    }
}
