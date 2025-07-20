package com.example.chat.models;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class MuteConversationRequest {
    @SerializedName("mute_until")
    private Date muteUntil;

    public MuteConversationRequest() {}

    public MuteConversationRequest(Date muteUntil) {
        this.muteUntil = muteUntil;
    }

    public Date getMuteUntil() {
        return muteUntil;
    }

    public void setMuteUntil(Date muteUntil) {
        this.muteUntil = muteUntil;
    }
}
