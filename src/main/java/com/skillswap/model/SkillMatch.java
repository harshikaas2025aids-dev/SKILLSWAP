package com.skillswap.model;

/**
 * Data Transfer Object representing a reciprocal smart skill match between two students.
 */
public class SkillMatch {
    private int currentStudentId;
    private String currentStudentName;
    private int partnerStudentId;
    private String partnerStudentName;
    private String partnerEmail;

    // What current student teaches (and partner wants to learn)
    private int currentOfferedSkillId;
    private String currentOfferedSkillName;
    private String currentOfferedProficiency;

    // What partner teaches (and current student wants to learn)
    private int partnerOfferedSkillId;
    private String partnerOfferedSkillName;
    private String partnerOfferedProficiency;

    public SkillMatch() {
    }

    public SkillMatch(int currentStudentId, String currentStudentName, 
                      int partnerStudentId, String partnerStudentName, String partnerEmail,
                      int currentOfferedSkillId, String currentOfferedSkillName, String currentOfferedProficiency,
                      int partnerOfferedSkillId, String partnerOfferedSkillName, String partnerOfferedProficiency) {
        this.currentStudentId = currentStudentId;
        this.currentStudentName = currentStudentName;
        this.partnerStudentId = partnerStudentId;
        this.partnerStudentName = partnerStudentName;
        this.partnerEmail = partnerEmail;
        this.currentOfferedSkillId = currentOfferedSkillId;
        this.currentOfferedSkillName = currentOfferedSkillName;
        this.currentOfferedProficiency = currentOfferedProficiency;
        this.partnerOfferedSkillId = partnerOfferedSkillId;
        this.partnerOfferedSkillName = partnerOfferedSkillName;
        this.partnerOfferedProficiency = partnerOfferedProficiency;
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

    public int getPartnerStudentId() {
        return partnerStudentId;
    }

    public void setPartnerStudentId(int partnerStudentId) {
        this.partnerStudentId = partnerStudentId;
    }

    public String getPartnerStudentName() {
        return partnerStudentName;
    }

    public void setPartnerStudentName(String partnerStudentName) {
        this.partnerStudentName = partnerStudentName;
    }

    public String getPartnerEmail() {
        return partnerEmail;
    }

    public void setPartnerEmail(String partnerEmail) {
        this.partnerEmail = partnerEmail;
    }

    public int getCurrentOfferedSkillId() {
        return currentOfferedSkillId;
    }

    public void setCurrentOfferedSkillId(int currentOfferedSkillId) {
        this.currentOfferedSkillId = currentOfferedSkillId;
    }

    public String getCurrentOfferedSkillName() {
        return currentOfferedSkillName;
    }

    public void setCurrentOfferedSkillName(String currentOfferedSkillName) {
        this.currentOfferedSkillName = currentOfferedSkillName;
    }

    public String getCurrentOfferedProficiency() {
        return currentOfferedProficiency;
    }

    public void setCurrentOfferedProficiency(String currentOfferedProficiency) {
        this.currentOfferedProficiency = currentOfferedProficiency;
    }

    public int getPartnerOfferedSkillId() {
        return partnerOfferedSkillId;
    }

    public void setPartnerOfferedSkillId(int partnerOfferedSkillId) {
        this.partnerOfferedSkillId = partnerOfferedSkillId;
    }

    public String getPartnerOfferedSkillName() {
        return partnerOfferedSkillName;
    }

    public void setPartnerOfferedSkillName(String partnerOfferedSkillName) {
        this.partnerOfferedSkillName = partnerOfferedSkillName;
    }

    public String getPartnerOfferedProficiency() {
        return partnerOfferedProficiency;
    }

    public void setPartnerOfferedProficiency(String partnerOfferedProficiency) {
        this.partnerOfferedProficiency = partnerOfferedProficiency;
    }

    @Override
    public String toString() {
        return String.format("Match with Student #%d (%s) | You teach: %s [%s] -> They teach: %s [%s]",
                partnerStudentId, partnerStudentName,
                currentOfferedSkillName, currentOfferedProficiency,
                partnerOfferedSkillName, partnerOfferedProficiency);
    }
}
