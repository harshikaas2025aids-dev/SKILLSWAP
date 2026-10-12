package com.skillswap.model;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * Model class representing a skill exchange request between two students.
 */
public class ExchangeRequest {
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_ACCEPTED = "ACCEPTED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    private int requestId;
    private int requesterId;
    private int receiverId;
    private int offeredSkillId;
    private int wantedSkillId;
    private Timestamp requestDate;
    private String status; // PENDING, ACCEPTED, REJECTED, COMPLETED, CANCELLED

    // Enriched fields from SQL JOINs for human-readable display
    private String requesterName;
    private String requesterEmail;
    private String receiverName;
    private String receiverEmail;
    private String offeredSkillName;
    private String offeredSkillProficiency;
    private String wantedSkillName;
    private String wantedSkillProficiency;

    public ExchangeRequest() {
    }

    public ExchangeRequest(int requesterId, int receiverId, int offeredSkillId, int wantedSkillId) {
        this.requesterId = requesterId;
        this.receiverId = receiverId;
        this.offeredSkillId = offeredSkillId;
        this.wantedSkillId = wantedSkillId;
        this.status = STATUS_PENDING;
    }

    public ExchangeRequest(int requestId, int requesterId, int receiverId, int offeredSkillId, 
                           int wantedSkillId, Timestamp requestDate, String status) {
        this.requestId = requestId;
        this.requesterId = requesterId;
        this.receiverId = receiverId;
        this.offeredSkillId = offeredSkillId;
        this.wantedSkillId = wantedSkillId;
        this.requestDate = requestDate;
        this.status = (status != null) ? status.toUpperCase().trim() : STATUS_PENDING;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
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

    public Timestamp getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Timestamp requestDate) {
        this.requestDate = requestDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = (status != null) ? status.toUpperCase().trim() : null;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    public String getRequesterEmail() {
        return requesterEmail;
    }

    public void setRequesterEmail(String requesterEmail) {
        this.requesterEmail = requesterEmail;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverEmail() {
        return receiverEmail;
    }

    public void setReceiverEmail(String receiverEmail) {
        this.receiverEmail = receiverEmail;
    }

    public String getOfferedSkillName() {
        return offeredSkillName;
    }

    public void setOfferedSkillName(String offeredSkillName) {
        this.offeredSkillName = offeredSkillName;
    }

    public String getOfferedSkillProficiency() {
        return offeredSkillProficiency;
    }

    public void setOfferedSkillProficiency(String offeredSkillProficiency) {
        this.offeredSkillProficiency = offeredSkillProficiency;
    }

    public String getWantedSkillName() {
        return wantedSkillName;
    }

    public void setWantedSkillName(String wantedSkillName) {
        this.wantedSkillName = wantedSkillName;
    }

    public String getWantedSkillProficiency() {
        return wantedSkillProficiency;
    }

    public void setWantedSkillProficiency(String wantedSkillProficiency) {
        this.wantedSkillProficiency = wantedSkillProficiency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExchangeRequest that = (ExchangeRequest) o;
        return requestId == that.requestId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestId);
    }

    @Override
    public String toString() {
        return String.format("Request #%d [%s -> %s | Offered: %s | Wanted: %s | Status: %s | Date: %s]",
                requestId,
                (requesterName != null ? requesterName : "ID " + requesterId),
                (receiverName != null ? receiverName : "ID " + receiverId),
                (offeredSkillName != null ? offeredSkillName : "Skill " + offeredSkillId),
                (wantedSkillName != null ? wantedSkillName : "Skill " + wantedSkillId),
                status,
                (requestDate != null ? requestDate.toString() : "N/A"));
    }
}
