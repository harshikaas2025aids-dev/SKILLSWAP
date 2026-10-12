package com.skillswap.dao;

import com.skillswap.DBConnection;
import com.skillswap.model.Skill;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for SKILL entities using JDBC PreparedStatement.
 */
public class SkillDAO {

    /**
     * Inserts a new skill and returns the generated skill_id.
     */
    public int create(Skill skill) throws SQLException {
        String sql = "INSERT INTO skills (student_id, skill_name, skill_type, proficiency_level, description) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, skill.getStudentId());
            ps.setString(2, skill.getSkillName().trim());
            ps.setString(3, skill.getSkillType().trim().toUpperCase());
            ps.setString(4, skill.getProficiencyLevel().trim().toUpperCase());
            ps.setString(5, skill.getDescription() != null ? skill.getDescription().trim() : null);

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating skill failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    skill.setSkillId(id);
                    return id;
                } else {
                    throw new SQLException("Creating skill failed, no generated ID obtained.");
                }
            }
        }
    }

    /**
     * Finds a skill by its ID, joining student details.
     */
    public Optional<Skill> findById(int skillId) throws SQLException {
        String sql = "SELECT sk.skill_id, sk.student_id, sk.skill_name, sk.skill_type, sk.proficiency_level, " +
                     "sk.description, sk.created_at, st.student_name, st.email " +
                     "FROM skills sk " +
                     "JOIN students st ON sk.student_id = st.student_id " +
                     "WHERE sk.skill_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, skillId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToSkill(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Finds all skills registered by a particular student.
     */
    public List<Skill> findByStudentId(int studentId) throws SQLException {
        List<Skill> list = new ArrayList<>();
        String sql = "SELECT sk.skill_id, sk.student_id, sk.skill_name, sk.skill_type, sk.proficiency_level, " +
                     "sk.description, sk.created_at, st.student_name, st.email " +
                     "FROM skills sk " +
                     "JOIN students st ON sk.student_id = st.student_id " +
                     "WHERE sk.student_id = ? " +
                     "ORDER BY sk.skill_type DESC, sk.skill_name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToSkill(rs));
                }
            }
        }
        return list;
    }

    /**
     * Returns all registered skills with student details.
     */
    public List<Skill> findAll() throws SQLException {
        List<Skill> list = new ArrayList<>();
        String sql = "SELECT sk.skill_id, sk.student_id, sk.skill_name, sk.skill_type, sk.proficiency_level, " +
                     "sk.description, sk.created_at, st.student_name, st.email " +
                     "FROM skills sk " +
                     "JOIN students st ON sk.student_id = st.student_id " +
                     "ORDER BY sk.skill_id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToSkill(rs));
            }
        }
        return list;
    }

    /**
     * Searches skills by name (case-insensitive substring match).
     */
    public List<Skill> searchByName(String keyword) throws SQLException {
        List<Skill> list = new ArrayList<>();
        String sql = "SELECT sk.skill_id, sk.student_id, sk.skill_name, sk.skill_type, sk.proficiency_level, " +
                     "sk.description, sk.created_at, st.student_name, st.email " +
                     "FROM skills sk " +
                     "JOIN students st ON sk.student_id = st.student_id " +
                     "WHERE LOWER(sk.skill_name) LIKE LOWER(?) " +
                     "ORDER BY sk.skill_name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + keyword.trim() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToSkill(rs));
                }
            }
        }
        return list;
    }

    /**
     * Filters skills by type: TEACH or LEARN.
     */
    public List<Skill> filterByType(String skillType) throws SQLException {
        List<Skill> list = new ArrayList<>();
        String sql = "SELECT sk.skill_id, sk.student_id, sk.skill_name, sk.skill_type, sk.proficiency_level, " +
                     "sk.description, sk.created_at, st.student_name, st.email " +
                     "FROM skills sk " +
                     "JOIN students st ON sk.student_id = st.student_id " +
                     "WHERE sk.skill_type = ? " +
                     "ORDER BY sk.skill_name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, skillType.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToSkill(rs));
                }
            }
        }
        return list;
    }

    /**
     * Filters skills by proficiency level: BEGINNER, INTERMEDIATE, or ADVANCED.
     */
    public List<Skill> filterByProficiency(String proficiencyLevel) throws SQLException {
        List<Skill> list = new ArrayList<>();
        String sql = "SELECT sk.skill_id, sk.student_id, sk.skill_name, sk.skill_type, sk.proficiency_level, " +
                     "sk.description, sk.created_at, st.student_name, st.email " +
                     "FROM skills sk " +
                     "JOIN students st ON sk.student_id = st.student_id " +
                     "WHERE sk.proficiency_level = ? " +
                     "ORDER BY sk.skill_name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, proficiencyLevel.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToSkill(rs));
                }
            }
        }
        return list;
    }

    /**
     * Updates an existing skill.
     */
    public boolean update(Skill skill) throws SQLException {
        String sql = "UPDATE skills SET skill_name = ?, skill_type = ?, proficiency_level = ?, description = ? " +
                     "WHERE skill_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, skill.getSkillName().trim());
            ps.setString(2, skill.getSkillType().trim().toUpperCase());
            ps.setString(3, skill.getProficiencyLevel().trim().toUpperCase());
            ps.setString(4, skill.getDescription() != null ? skill.getDescription().trim() : null);
            ps.setInt(5, skill.getSkillId());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a skill record.
     */
    public boolean delete(int skillId) throws SQLException {
        String sql = "DELETE FROM skills WHERE skill_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, skillId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Checks if a skill is currently referenced by any exchange requests.
     */
    public boolean isReferencedInRequests(int skillId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM skill_exchange_requests WHERE offered_skill_id = ? OR wanted_skill_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, skillId);
            ps.setInt(2, skillId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Checks if the student already registered the exact same skill name under the specified type.
     */
    public boolean existsDuplicate(int studentId, String skillName, String skillType, int excludeSkillId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM skills " +
                     "WHERE student_id = ? AND LOWER(TRIM(skill_name)) = LOWER(TRIM(?)) " +
                     "AND skill_type = ? AND skill_id != ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ps.setString(2, skillName);
            ps.setString(3, skillType.trim().toUpperCase());
            ps.setInt(4, excludeSkillId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private Skill mapRowToSkill(ResultSet rs) throws SQLException {
        Skill skill = new Skill(
                rs.getInt("skill_id"),
                rs.getInt("student_id"),
                rs.getString("skill_name"),
                rs.getString("skill_type"),
                rs.getString("proficiency_level"),
                rs.getString("description"),
                rs.getTimestamp("created_at")
        );
        skill.setStudentName(rs.getString("student_name"));
        skill.setStudentEmail(rs.getString("email"));
        return skill;
    }
}
