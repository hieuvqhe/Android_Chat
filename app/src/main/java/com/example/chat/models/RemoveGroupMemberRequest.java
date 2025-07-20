package com.example.chat.models;

import com.google.gson.annotations.SerializedName;

public class RemoveGroupMemberRequest {
    @SerializedName("member_id")
    private String memberId;

    public RemoveGroupMemberRequest() {}

    public RemoveGroupMemberRequest(String memberId) {
        this.memberId = memberId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }
}
