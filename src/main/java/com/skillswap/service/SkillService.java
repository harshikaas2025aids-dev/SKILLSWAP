package com.skillswap.service;

import com.skillswap.dao.SkillDAO;
import com.skillswap.dao.StudentDAO;
import com.skillswap.model.Skill;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for Skill management including business validation rules.
 */
public class SkillService {

    private final SkillDAO skillDAO;
    private final StudentDAO studentDAO;

    public SkillService() {
        this.skillDAO = new SkillDAO();
        this.studentDAO = new StudentDAO();
    }

    public SkillService(SkillDAO skillDAO, StudentDAO studentDAO) {
        this.skillDAO = skillDAO;
        this.studentDAO = studentDAO;
    }

    /**
     * Validates skill parameters.
     */
    public void validateSkill(int studentId, String skillName, String skillType, String proficiencyLevel) throws SQLException {
        if (studentId <= 0) {
            throw new IllegalArgumentException("Student ID must be a positive integer.");
        }
        if (studentDAO.findById(studentId).isEmpty()) {
            throw new IllegalArgumentException("Student with ID " + studentId + " does not exist.");
        }
        if (skillName == null || skillName.trim().isEmpty()) {
            throw new IllegalArgumentException("Skill name cannot be blank.");
        }
        if (skillType == null || (!skillType.equalsIgnoreCase(Skill.TYPE_TEACH) && !skillType.equalsIgnoreCase(Skill.TYPE_LEARN))) {
            throw new IllegalArgumentException("Invalid skill type: '" + skillType + "'. Must be TEACH or LEARN.");
        }
        if (proficiencyLevel == null || (
                !proficiencyLevel.equalsIgnoreCase(Skill.LEVEL_BEGINNER) &&
                !proficiencyLevel.equalsIgnoreCase(Skill.LEVEL_INTERMEDIATE) &&
                !proficiencyLevel.equalsIgnoreCase(Skill.LEVEL_ADVANCED))) {
            throw new IllegalArgumentException("Invalid proficiency level: '" + proficiencyLevel + "'. Must be BEGINNER, INTERMEDIATE, or ADVANCED.");
        }
    }

    /**
     * Adds a skill (TEACH or LEARN) for a student.
     */
    public Skill addSkill(int studentId, String skillName, String skillType, String proficiencyLevel, String description) throws SQLException {
        validateSkill(studentId, skillName, skillType, proficiencyLevel);

        String normalizedType = skillType.trim().toUpperCase();
        String normalizedProficiency = proficiencyLevel.trim().toUpperCase();

        if (skillDAO.existsDuplicate(studentId, skillName.trim(), normalizedType, 0)) {
            throw new IllegalArgumentException(String.format(
                "Student #%d already has registered '%s' under %s skills.", studentId, skillName.trim(), normalizedType
            ));
        }

        Skill skill = new Skill(studentId, skillName.trim(), normalizedType, normalizedProficiency, description);
        int generatedId = skillDAO.create(skill);
        skill.setSkillId(generatedId);
        return skill;
    }

    /**
     * Shortcut to add a TEACH skill.
     */
    public Skill addTeachSkill(int studentId, String skillName, String proficiencyLevel, String description) throws SQLException {
        return addSkill(studentId, skillName, Skill.TYPE_TEACH, proficiencyLevel, description);
    }

    /**
     * Shortcut to add a LEARN skill.
     */
    public Skill addLearnSkill(int studentId, String skillName, String proficiencyLevel, String description) throws SQLException {
        return addSkill(studentId, skillName, Skill.TYPE_LEARN, proficiencyLevel, description);
    }

    /**
     * Retrieves a skill by ID.
     */
    public Optional<Skill> getSkillById(int skillId) throws SQLException {
        if (skillId <= 0) {
            throw new IllegalArgumentException("Skill ID must be a positive integer.");
        }
        return skillDAO.findById(skillId);
    }

    /**
     * Retrieves all skills for a specific student.
     */
    public List<Skill> getSkillsByStudentId(int studentId) throws SQLException {
        if (studentId <= 0) {
            throw new IllegalArgumentException("Student ID must be a positive integer.");
        }
        return skillDAO.findByStudentId(studentId);
    }

    /**
     * Retrieves all registered skills.
     */
    public List<Skill> getAllSkills() throws SQLException {
        return skillDAO.findAll();
    }

    /**
     * Searches skills by name.
     */
    public List<Skill> searchSkillsByName(String keyword) throws SQLException {
        if (keyword == null || keyword.trim().isEmpty()) {
            return skillDAO.findAll();
        }
        return skillDAO.searchByName(keyword.trim());
    }

    /**
     * Filters skills by type (TEACH or LEARN).
     */
    public List<Skill> filterSkillsByType(String skillType) throws SQLException {
        if (skillType == null || (!skillType.equalsIgnoreCase(Skill.TYPE_TEACH) && !skillType.equalsIgnoreCase(Skill.TYPE_LEARN))) {
            throw new IllegalArgumentException("Filter type must be either TEACH or LEARN.");
        }
        return skillDAO.filterByType(skillType.trim().toUpperCase());
    }

    /**
     * Filters skills by proficiency level.
     */
    public List<Skill> filterSkillsByProficiency(String proficiencyLevel) throws SQLException {
        if (proficiencyLevel == null || (
                !proficiencyLevel.equalsIgnoreCase(Skill.LEVEL_BEGINNER) &&
                !proficiencyLevel.equalsIgnoreCase(Skill.LEVEL_INTERMEDIATE) &&
                !proficiencyLevel.equalsIgnoreCase(Skill.LEVEL_ADVANCED))) {
            throw new IllegalArgumentException("Proficiency must be BEGINNER, INTERMEDIATE, or ADVANCED.");
        }
        return skillDAO.filterByProficiency(proficiencyLevel.trim().toUpperCase());
    }

    /**
     * Updates an existing skill.
     */
    public boolean updateSkill(int skillId, int studentId, String skillName, String skillType, 
                               String proficiencyLevel, String description) throws SQLException {
        if (skillId <= 0) {
            throw new IllegalArgumentException("Skill ID must be a positive integer.");
        }
        Optional<Skill> existingOpt = skillDAO.findById(skillId);
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("Skill with ID " + skillId + " does not exist.");
        }

        Skill existing = existingOpt.get();
        if (existing.getStudentId() != studentId) {
            throw new IllegalArgumentException("Ownership validation failed: Skill #" + skillId + " does not belong to Student #" + studentId);
        }

        validateSkill(studentId, skillName, skillType, proficiencyLevel);

        String normalizedType = skillType.trim().toUpperCase();
        String normalizedProficiency = proficiencyLevel.trim().toUpperCase();

        if (skillDAO.existsDuplicate(studentId, skillName.trim(), normalizedType, skillId)) {
            throw new IllegalArgumentException("Another skill with name '" + skillName.trim() + "' and type " + normalizedType + " already exists for this student.");
        }

        existing.setSkillName(skillName.trim());
        existing.setSkillType(normalizedType);
        existing.setProficiencyLevel(normalizedProficiency);
        existing.setDescription(description != null ? description.trim() : null);

        return skillDAO.update(existing);
    }

    /**
     * Deletes a skill when it is not referenced by an exchange request.
     */
    public boolean deleteSkill(int skillId, int studentId) throws SQLException {
        if (skillId <= 0) {
            throw new IllegalArgumentException("Skill ID must be a positive integer.");
        }

        Optional<Skill> existingOpt = skillDAO.findById(skillId);
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("Skill with ID " + skillId + " not found.");
        }

        Skill existing = existingOpt.get();
        if (studentId > 0 && existing.getStudentId() != studentId) {
            throw new IllegalArgumentException("Ownership validation failed: Skill #" + skillId + " does not belong to Student #" + studentId);
        }

        // Check if skill is referenced in any exchange request
        if (skillDAO.isReferencedInRequests(skillId)) {
            throw new IllegalStateException("Cannot delete skill: It is currently referenced in one or more exchange requests.");
        }

        return skillDAO.delete(skillId);
    }
}
