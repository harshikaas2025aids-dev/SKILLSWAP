package com.skillswap.dao;

import com.skillswap.DBConnection;
import com.skillswap.model.Student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for STUDENT entities using JDBC PreparedStatement.
 */
public class StudentDAO {

    /**
     * Inserts a new student and returns the generated student_id.
     */
    public int create(Student student) throws SQLException {
        String sql = "INSERT INTO students (student_name, email, contact_info) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, student.getStudentName().trim());
            ps.setString(2, student.getEmail().trim().toLowerCase());
            ps.setString(3, student.getContactInfo() != null ? student.getContactInfo().trim() : null);

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating student failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    student.setStudentId(id);
                    return id;
                } else {
                    throw new SQLException("Creating student failed, no generated ID obtained.");
                }
            }
        }
    }

    /**
     * Retrieves a student by their primary key.
     */
    public Optional<Student> findById(int studentId) throws SQLException {
        String sql = "SELECT student_id, student_name, email, contact_info, created_at FROM students WHERE student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToStudent(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Retrieves a student by their unique email.
     */
    public Optional<Student> findByEmail(String email) throws SQLException {
        String sql = "SELECT student_id, student_name, email, contact_info, created_at FROM students WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToStudent(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Checks if another student already uses the given email address.
     */
    public boolean existsByEmail(String email, int excludeStudentId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM students WHERE LOWER(email) = LOWER(?) AND student_id != ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email.trim());
            ps.setInt(2, excludeStudentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Returns a list of all registered students.
     */
    public List<Student> findAll() throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT student_id, student_name, email, contact_info, created_at FROM students ORDER BY student_id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                students.add(mapRowToStudent(rs));
            }
        }
        return students;
    }

    /**
     * Updates an existing student record.
     */
    public boolean update(Student student) throws SQLException {
        String sql = "UPDATE students SET student_name = ?, email = ?, contact_info = ? WHERE student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, student.getStudentName().trim());
            ps.setString(2, student.getEmail().trim().toLowerCase());
            ps.setString(3, student.getContactInfo() != null ? student.getContactInfo().trim() : null);
            ps.setInt(4, student.getStudentId());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a student while respecting foreign key relationships within a transaction.
     * Deletes associated exchange requests, associated skills, and then the student.
     */
    public boolean delete(int studentId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);

                // 1. Delete exchange requests involving this student
                String deleteRequestsSql = "DELETE FROM skill_exchange_requests WHERE requester_id = ? OR receiver_id = ?";
                try (PreparedStatement psReq = conn.prepareStatement(deleteRequestsSql)) {
                    psReq.setInt(1, studentId);
                    psReq.setInt(2, studentId);
                    psReq.executeUpdate();
                }

                // 2. Delete skills belonging to this student
                String deleteSkillsSql = "DELETE FROM skills WHERE student_id = ?";
                try (PreparedStatement psSkill = conn.prepareStatement(deleteSkillsSql)) {
                    psSkill.setInt(1, studentId);
                    psSkill.executeUpdate();
                }

                // 3. Delete the student
                String deleteStudentSql = "DELETE FROM students WHERE student_id = ?";
                int rowsDeleted;
                try (PreparedStatement psStud = conn.prepareStatement(deleteStudentSql)) {
                    psStud.setInt(1, studentId);
                    rowsDeleted = psStud.executeUpdate();
                }

                conn.commit();
                return rowsDeleted > 0;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        }
    }

    private Student mapRowToStudent(ResultSet rs) throws SQLException {
        return new Student(
                rs.getInt("student_id"),
                rs.getString("student_name"),
                rs.getString("email"),
                rs.getString("contact_info"),
                rs.getTimestamp("created_at")
        );
    }
}
