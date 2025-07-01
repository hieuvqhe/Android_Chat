package com.example.chat.models;

import com.google.gson.annotations.SerializedName;

public class AddFriendRequest {
    @SerializedName("friend_id")
    private String friendId;

    public AddFriendRequest() {}

    public AddFriendRequest(String friendId) {
        this.friendId = friendId;
    }

    public String getFriendId() {
        return friendId;
    }

    public void setFriendId(String friendId) {
        this.friendId = friendId;
    }
}