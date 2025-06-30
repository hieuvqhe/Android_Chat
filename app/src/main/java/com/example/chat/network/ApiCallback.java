package com.example.chat.network;

import android.util.Log;
import com.example.chat.models.ApiResponse;
import com.example.chat.models.ErrorResponse;
import com.google.gson.Gson;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.io.IOException;

public abstract class ApiCallback<T> implements Callback<ApiResponse<T>> {
    private static final String TAG = "ApiCallback";

    @Override
    public void onResponse(Call<ApiResponse<T>> call, Response<ApiResponse<T>> response) {
        Log.d(TAG, "Response code: " + response.code());

        if (response.isSuccessful() && response.body() != null) {
            ApiResponse<T> apiResponse = response.body();
            Log.d(TAG, "Success response: " + apiResponse.getMessage());
            onSuccess(apiResponse.getResult(), apiResponse.getMessage());
        } else {
            // Handle error response
            String errorMessage = "An error occurred";
            int statusCode = response.code();

            try {
                if (response.errorBody() != null) {
                    String errorBody = response.errorBody().string();
                    Log.e(TAG, "Error body: " + errorBody);
                    Log.e(TAG, "Status code: " + statusCode);

                    if (!errorBody.isEmpty()) {
                        Gson gson = new Gson();

                        try {
                            // Try to parse as ErrorResponse first
                            ErrorResponse errorResponse = gson.fromJson(errorBody, ErrorResponse.class);
                            if (errorResponse != null && errorResponse.getMessage() != null) {
                                errorMessage = errorResponse.getMessage();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to parse as ErrorResponse, trying as ApiResponse", e);

                            try {
                                // Try to parse as ApiResponse in case of different format
                                ApiResponse<?> apiErrorResponse = gson.fromJson(errorBody, ApiResponse.class);
                                if (apiErrorResponse != null && apiErrorResponse.getMessage() != null) {
                                    errorMessage = apiErrorResponse.getMessage();
                                }
                            } catch (Exception e2) {
                                Log.e(TAG, "Failed to parse error response in any format", e2);
                                // Use raw error body if parsing fails
                                if (errorBody.length() > 0) {
                                    errorMessage = errorBody;
                                }
                            }
                        }
                    }
                } else {
                    Log.e(TAG, "Error body is null");
                }
            } catch (IOException e) {
                Log.e(TAG, "Error reading error response", e);
            }

            Log.e(TAG, "Final error message: " + errorMessage);
            onError(statusCode, errorMessage);
        }
    }

    @Override
    public void onFailure(Call<ApiResponse<T>> call, Throwable t) {
        Log.e(TAG, "API call failed", t);
        String errorMessage = "Network error. Please check your connection.";

        if (t instanceof IOException) {
            errorMessage = "Network error. Please check your internet connection.";
        } else {
            errorMessage = "An unexpected error occurred. Please try again.";
        }

        Log.e(TAG, "Network error: " + errorMessage);
        onNetworkError(errorMessage);
    }

    public abstract void onSuccess(T result, String message);
    public abstract void onError(int statusCode, String message);
    public abstract void onNetworkError(String message);
}