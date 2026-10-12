package com.skillswap.dto;

public class CreateExchangeRequest {
    private int requesterId;
    private int receiverId;
    private int offeredSkillId;
    private int wantedSkillId;

    public CreateExchangeRequest() {
    }

    public CreateExchangeRequest(int requesterId, int receiverId, int offeredSkillId, int wantedSkillId) {
        this.requesterId = requesterId;
        this.receiverId = receiverId;
        this.offeredSkillId = offeredSkillId;
        this.wantedSkillId = wantedSkillId;
    }

    public int getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(int requesterId) {
        this.requesterId = requesterId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public int getOfferedSkillId() {
        return offeredSkillId;
    }

    public void setOfferedSkillId(int offeredSkillId) {
        this.offeredSkillId = offeredSkillId;
    }

    public int getWantedSkillId() {
        return wantedSkillId;
    }

    public void setWantedSkillId(int wantedSkillId) {
        this.wantedSkillId = wantedSkillId;
    }
}
