package com.example.chat.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AddGroupMembersRequest {
    @SerializedName("member_ids")
    private List<String> memberIds;

    public AddGroupMembersRequest() {}

    public AddGroupMembersRequest(List<String> memberIds) {
        this.memberIds = memberIds;
    }

    public List<String> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<String> memberIds) {
        this.memberIds = memberIds;
    }
}
