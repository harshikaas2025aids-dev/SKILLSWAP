package com.skillswap.model;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * Model class representing a skill registered by a student (either TEACH or LEARN).
 */
public class Skill {
    public static final String TYPE_TEACH = "TEACH";
    public static final String TYPE_LEARN = "LEARN";

    public static final String LEVEL_BEGINNER = "BEGINNER";
    public static final String LEVEL_INTERMEDIATE = "INTERMEDIATE";
    public static final String LEVEL_ADVANCED = "ADVANCED";

    private int skillId;
    private int studentId;
    private String skillName;
    private String skillType; // TEACH or LEARN
    private String proficiencyLevel; // BEGINNER, INTERMEDIATE, ADVANCED
    private String description;
    private Timestamp createdAt;

    // Additional fields populated via SQL JOINs
    private String studentName;
    private String studentEmail;

    public Skill() {
    }

    public Skill(int studentId, String skillName, String skillType, String proficiencyLevel, String description) {
        this.studentId = studentId;
        this.skillName = skillName;
        this.skillType = (skillType != null) ? skillType.toUpperCase().trim() : null;
        this.proficiencyLevel = (proficiencyLevel != null) ? proficiencyLevel.toUpperCase().trim() : null;
        this.description = description;
    }

    public Skill(int skillId, int studentId, String skillName, String skillType, String proficiencyLevel, String description) {
        this.skillId = skillId;
        this.studentId = studentId;
        this.skillName = skillName;
        this.skillType = (skillType != null) ? skillType.toUpperCase().trim() : null;
        this.proficiencyLevel = (proficiencyLevel != null) ? proficiencyLevel.toUpperCase().trim() : null;
        this.description = description;
    }

    public Skill(int skillId, int studentId, String skillName, String skillType, String proficiencyLevel, 
                 String description, Timestamp createdAt) {
        this(skillId, studentId, skillName, skillType, proficiencyLevel, description);
        this.createdAt = createdAt;
    }

    public int getSkillId() {
        return skillId;
    }

    public void setSkillId(int skillId) {
        this.skillId = skillId;
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
        this.skillType = (skillType != null) ? skillType.toUpperCase().trim() : null;
    }

    public String getProficiencyLevel() {
        return proficiencyLevel;
    }

    public void setProficiencyLevel(String proficiencyLevel) {
        this.proficiencyLevel = (proficiencyLevel != null) ? proficiencyLevel.toUpperCase().trim() : null;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Skill skill = (Skill) o;
        return skillId == skill.skillId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(skillId);
    }

    @Override
    public String toString() {
        return String.format("Skill [ID: %d | Student ID: %d | Name: %s | Type: %s | Level: %s | Desc: %s]",
                skillId, studentId, skillName, skillType, proficiencyLevel, 
                (description != null ? description : "None"));
    }
}
