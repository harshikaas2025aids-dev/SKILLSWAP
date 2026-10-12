package com.skillswap.dto;

import com.skillswap.model.ExchangeRequest;
import com.skillswap.model.SkillMatch;

import java.util.List;

public class DashboardStats {
    private int currentStudentId;
    private String currentStudentName;
    private int totalStudents;
    private int skillsOffered;
    private int skillsWanted;
    private int pendingRequests;
    private int acceptedExchanges;
    private int completedExchanges;
    private int mutualMatchesCount;
    private List<SkillMatch> recommendedPartners;
    private List<ExchangeRequest> recentActivity;

    public DashboardStats() {
    }

    public int getCurrentStudentId() {
        return currentStudentId;
    }

    public void setCurrentStudentId(int currentStudentId) {
        this.currentStudentId = currentStudentId;
    }

    public String getCurrentStudentName() {
        return currentStudentName;
    }

    public void setCurrentStudentName(String currentStudentName) {
        this.currentStudentName = currentStudentName;
    }

    public int getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(int totalStudents) {
        this.totalStudents = totalStudents;
    }

    public int getSkillsOffered() {
        return skillsOffered;
    }

    public void setSkillsOffered(int skillsOffered) {
        this.skillsOffered = skillsOffered;
    }

    public int getSkillsWanted() {
        return skillsWanted;
    }

    public void setSkillsWanted(int skillsWanted) {
        this.skillsWanted = skillsWanted;
    }

    public int getPendingRequests() {
        return pendingRequests;
    }

    public void setPendingRequests(int pendingRequests) {
        this.pendingRequests = pendingRequests;
    }

    public int getAcceptedExchanges() {
        return acceptedExchanges;
    }

    public void setAcceptedExchanges(int acceptedExchanges) {
        this.acceptedExchanges = acceptedExchanges;
    }

    public int getCompletedExchanges() {
        return completedExchanges;
    }

    public void setCompletedExchanges(int completedExchanges) {
        this.completedExchanges = completedExchanges;
    }

    public int getMutualMatchesCount() {
        return mutualMatchesCount;
    }

    public void setMutualMatchesCount(int mutualMatchesCount) {
        this.mutualMatchesCount = mutualMatchesCount;
    }

    public List<SkillMatch> getRecommendedPartners() {
        return recommendedPartners;
    }

    public void setRecommendedPartners(List<SkillMatch> recommendedPartners) {
        this.recommendedPartners = recommendedPartners;
    }

    public List<ExchangeRequest> getRecentActivity() {
        return recentActivity;
    }

    public void setRecentActivity(List<ExchangeRequest> recentActivity) {
        this.recentActivity = recentActivity;
    }
}
