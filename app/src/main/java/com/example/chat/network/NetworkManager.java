package com.example.chat.network;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.chat.api.ApiService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class NetworkManager {
    private static final String BASE_URL = "https://realtime-chat-app-tam7.onrender.com/";
    private static final String PREF_NAME = "ChatAppPrefs";
    private static final String ACCESS_TOKEN_KEY = "access_token";
    private static final String REFRESH_TOKEN_KEY = "refresh_token";
    private static final String USER_ID_KEY = "user_id";
    private static final String EMAIL_VERIFY_TOKEN_KEY = "email_verify_token";

    private static NetworkManager instance;
    private ApiService apiService;
    private SharedPreferences sharedPreferences;

    private NetworkManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        setupRetrofit();
    }

    public static NetworkManager getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkManager(context.getApplicationContext());
        }
        return instance;
    }

    private void setupRetrofit() {
        // Create logging interceptor for debugging
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Create OkHttpClient with longer timeouts for Render.com
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)  // Tăng từ 30s lên 60s
                .readTimeout(60, TimeUnit.SECONDS)     // Tăng từ 30s lên 60s
                .writeTimeout(60, TimeUnit.SECONDS)    // Tăng từ 30s lên 60s
                .addInterceptor(loggingInterceptor)
                // Thêm interceptor để tự động thêm headers
                .addInterceptor(chain -> {
                    okhttp3.Request original = chain.request();
                    okhttp3.Request.Builder requestBuilder = original.newBuilder()
                            .header("Accept", "application/json")
                            .header("Content-Type", "application/json");

                    okhttp3.Request request = requestBuilder.build();
                    return chain.proceed(request);
                })
                .build();

        // Create custom Gson instance
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        // Create Retrofit instance
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    public ApiService getApiService() {
        return apiService;
    }

    // Token management methods
    public void saveTokens(String accessToken, String refreshToken) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(ACCESS_TOKEN_KEY, accessToken);
        editor.putString(REFRESH_TOKEN_KEY, refreshToken);
        editor.apply();
    }

    public String getAccessToken() {
        return sharedPreferences.getString(ACCESS_TOKEN_KEY, null);
    }

    public String getRefreshToken() {
        return sharedPreferences.getString(REFRESH_TOKEN_KEY, null);
    }

    public void saveUserId(String userId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(USER_ID_KEY, userId);
        editor.apply();
    }

    public String getUserId() {
        return sharedPreferences.getString(USER_ID_KEY, null);
    }

    public void saveEmailVerifyToken(String token) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(EMAIL_VERIFY_TOKEN_KEY, token);
        editor.apply();
    }

    public String getEmailVerifyToken() {
        return sharedPreferences.getString(EMAIL_VERIFY_TOKEN_KEY, null);
    }

    public void clearTokens() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(ACCESS_TOKEN_KEY);
        editor.remove(REFRESH_TOKEN_KEY);
        editor.remove(USER_ID_KEY);
        editor.remove(EMAIL_VERIFY_TOKEN_KEY);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return getAccessToken() != null && !getAccessToken().isEmpty();
    }

    public String getAuthorizationHeader() {
        String token = getAccessToken();
        return token != null ? "Bearer " + token : null;
    }
}