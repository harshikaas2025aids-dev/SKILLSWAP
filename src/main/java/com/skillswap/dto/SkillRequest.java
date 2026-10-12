package com.skillswap.dto;

public class SkillRequest {
    private int studentId;
    private String skillName;
    private String skillType; // TEACH or LEARN
    private String proficiencyLevel; // BEGINNER, INTERMEDIATE, ADVANCED
    private String description;

    public SkillRequest() {
    }

    public SkillRequest(int studentId, String skillName, String skillType, String proficiencyLevel, String description) {
        this.studentId = studentId;
        this.skillName = skillName;
        this.skillType = skillType;
        this.proficiencyLevel = proficiencyLevel;
        this.description = description;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public String getSkillType() {
        return skillType;
    }

    public void setSkillType(String skillType) {
        this.skillType = skillType;
    }

    public String getProficiencyLevel() {
        return proficiencyLevel;
    }

    public void setProficiencyLevel(String proficiencyLevel) {
        this.proficiencyLevel = proficiencyLevel;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
