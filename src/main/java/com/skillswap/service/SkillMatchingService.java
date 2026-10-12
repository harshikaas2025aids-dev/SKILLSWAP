package com.skillswap.service;

import com.skillswap.DBConnection;
import com.skillswap.dao.StudentDAO;
import com.skillswap.model.SkillMatch;
import com.skillswap.model.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service providing smart skill matching between students based on mutual needs and offerings.
 */
public class SkillMatchingService {

    private final StudentDAO studentDAO;

    public SkillMatchingService() {
        this.studentDAO = new StudentDAO();
    }

    public SkillMatchingService(StudentDAO studentDAO) {
        this.studentDAO = studentDAO;
    }

    /**
     * Finds all two-way mutual skill exchange matches for a specific student.
     * Compares skill names case-insensitively and ignores leading/trailing whitespace.
     * Excludes the student from their own matches.
     */
    public List<SkillMatch> findMatchesForStudent(int studentId) throws SQLException {
        if (studentId <= 0) {
            throw new IllegalArgumentException("Student ID must be a positive integer.");
        }

        Optional<Student> studentOpt = studentDAO.findById(studentId);
        if (studentOpt.isEmpty()) {
            throw new IllegalArgumentException("Student with ID " + studentId + " does not exist.");
        }

        String sql = 
            "SELECT " +
            "    cur_s.student_id AS cur_id, " +
            "    cur_s.student_name AS cur_name, " +
            "    cur_teach.skill_id AS cur_teach_id, " +
            "    cur_teach.skill_name AS cur_teach_name, " +
            "    cur_teach.proficiency_level AS cur_teach_prof, " +
            "    part_s.student_id AS part_id, " +
            "    part_s.student_name AS part_name, " +
            "    part_s.email AS part_email, " +
            "    part_teach.skill_id AS part_teach_id, " +
            "    part_teach.skill_name AS part_teach_name, " +
            "    part_teach.proficiency_level AS part_teach_prof " +
            "FROM skills cur_learn " +
            "JOIN students cur_s ON cur_learn.student_id = cur_s.student_id " +
            "JOIN skills part_teach " +
            "    ON LOWER(TRIM(cur_learn.skill_name)) = LOWER(TRIM(part_teach.skill_name)) " +
            "   AND part_teach.skill_type = 'TEACH' " +
            "JOIN students part_s ON part_teach.student_id = part_s.student_id " +
            "JOIN skills part_learn " +
            "    ON part_learn.student_id = part_s.student_id " +
            "   AND part_learn.skill_type = 'LEARN' " +
            "JOIN skills cur_teach " +
            "    ON cur_teach.student_id = cur_s.student_id " +
            "   AND cur_teach.skill_type = 'TEACH' " +
            "   AND LOWER(TRIM(part_learn.skill_name)) = LOWER(TRIM(cur_teach.skill_name)) " +
            "WHERE cur_s.student_id = ? " +
            "  AND part_s.student_id <> ? " +
            "  AND cur_learn.skill_type = 'LEARN' " +
            "ORDER BY part_s.student_name ASC, part_teach.skill_name ASC";

        List<SkillMatch> matches = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ps.setInt(2, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SkillMatch match = new SkillMatch(
                        rs.getInt("cur_id"),
                        rs.getString("cur_name"),
                        rs.getInt("part_id"),
                        rs.getString("part_name"),
                        rs.getString("part_email"),
                        rs.getInt("cur_teach_id"),
                        rs.getString("cur_teach_name"),
                        rs.getString("cur_teach_prof"),
                        rs.getInt("part_teach_id"),
                        rs.getString("part_teach_name"),
                        rs.getString("part_teach_prof")
                    );
                    matches.add(match);
                }
            }
        }

        return matches;
    }

    /**
     * Finds all mutual skill matches across the entire platform.
     * Prevents duplicate reversed pairs by ordering student IDs.
     */
    public List<SkillMatch> findAllMatches() throws SQLException {
        String sql = 
            "SELECT " +
            "    s1.student_id AS cur_id, " +
            "    s1.student_name AS cur_name, " +
            "    sk1_teach.skill_id AS cur_teach_id, " +
            "    sk1_teach.skill_name AS cur_teach_name, " +
            "    sk1_teach.proficiency_level AS cur_teach_prof, " +
            "    s2.student_id AS part_id, " +
            "    s2.student_name AS part_name, " +
            "    s2.email AS part_email, " +
            "    sk2_teach.skill_id AS part_teach_id, " +
            "    sk2_teach.skill_name AS part_teach_name, " +
            "    sk2_teach.proficiency_level AS part_teach_prof " +
            "FROM skills sk1_learn " +
            "JOIN students s1 ON sk1_learn.student_id = s1.student_id " +
            "JOIN skills sk2_teach " +
            "    ON LOWER(TRIM(sk1_learn.skill_name)) = LOWER(TRIM(sk2_teach.skill_name)) " +
            "   AND sk2_teach.skill_type = 'TEACH' " +
            "JOIN students s2 ON sk2_teach.student_id = s2.student_id " +
            "JOIN skills sk2_learn " +
            "    ON sk2_learn.student_id = s2.student_id " +
            "   AND sk2_learn.skill_type = 'LEARN' " +
            "JOIN skills sk1_teach " +
            "    ON sk1_teach.student_id = s1.student_id " +
            "   AND sk1_teach.skill_type = 'TEACH' " +
            "   AND LOWER(TRIM(sk2_learn.skill_name)) = LOWER(TRIM(sk1_teach.skill_name)) " +
            "WHERE s1.student_id < s2.student_id " +
            "  AND sk1_learn.skill_type = 'LEARN' " +
            "ORDER BY s1.student_name ASC, s2.student_name ASC";

        List<SkillMatch> matches = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                SkillMatch match = new SkillMatch(
                    rs.getInt("cur_id"),
                    rs.getString("cur_name"),
                    rs.getInt("part_id"),
                    rs.getString("part_name"),
                    rs.getString("part_email"),
                    rs.getInt("cur_teach_id"),
                    rs.getString("cur_teach_name"),
                    rs.getString("cur_teach_prof"),
                    rs.getInt("part_teach_id"),
                    rs.getString("part_teach_name"),
                    rs.getString("part_teach_prof")
                );
                matches.add(match);
            }
        }

        return matches;
    }

    /**
     * Pure business logic method to test reciprocity matching between two sets of skills in-memory.
     * Useful for isolated unit testing.
     */
    public static boolean isReciprocalMatch(String teachSkillA, String learnSkillA, 
                                           String teachSkillB, String learnSkillB) {
        if (teachSkillA == null || learnSkillA == null || teachSkillB == null || learnSkillB == null) {
            return false;
        }
        boolean aTeachesWhatBWants = teachSkillA.trim().equalsIgnoreCase(learnSkillB.trim());
        boolean bTeachesWhatAWants = teachSkillB.trim().equalsIgnoreCase(learnSkillA.trim());
        return aTeachesWhatBWants && bTeachesWhatAWants;
    }
}
