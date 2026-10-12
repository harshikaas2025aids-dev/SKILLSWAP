package com.skillswap.service;

import com.skillswap.dao.StudentDAO;
import com.skillswap.model.Student;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Service layer for Student management including business rules and validations.
 */
public class StudentService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final StudentDAO studentDAO;

    public StudentService() {
        this.studentDAO = new StudentDAO();
    }

    public StudentService(StudentDAO studentDAO) {
        this.studentDAO = studentDAO;
    }

    /**
     * Validates student fields and email format.
     */
    public void validateStudent(String name, String email) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Student name is required and cannot be blank.");
        }
        if (name.trim().length() < 2 || name.trim().length() > 100) {
            throw new IllegalArgumentException("Student name must be between 2 and 100 characters.");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Student email is required and cannot be blank.");
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("Invalid email format: '" + email + "'. Example: student@university.edu");
        }
    }

    /**
     * Registers a new student after validating data and duplicate email checks.
     */
    public Student registerStudent(String name, String email, String contactInfo) throws SQLException {
        validateStudent(name, email);

        if (studentDAO.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("A student with email '" + email.trim() + "' is already registered.");
        }

        Student student = new Student(name.trim(), email.trim().toLowerCase(), contactInfo != null ? contactInfo.trim() : null);
        int generatedId = studentDAO.create(student);
        student.setStudentId(generatedId);
        return student;
    }

    /**
     * Finds a student by their ID.
     */
    public Optional<Student> getStudentById(int studentId) throws SQLException {
        if (studentId <= 0) {
            throw new IllegalArgumentException("Student ID must be a positive integer.");
        }
        return studentDAO.findById(studentId);
    }

    /**
     * Finds a student by their email address.
     */
    public Optional<Student> getStudentByEmail(String email) throws SQLException {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be blank.");
        }
        return studentDAO.findByEmail(email.trim());
    }

    /**
     * Returns all registered students.
     */
    public List<Student> getAllStudents() throws SQLException {
        return studentDAO.findAll();
    }

    /**
     * Updates an existing student record.
     */
    public boolean updateStudent(int studentId, String name, String email, String contactInfo) throws SQLException {
        if (studentId <= 0) {
            throw new IllegalArgumentException("Student ID must be a positive integer.");
        }
        validateStudent(name, email);

        Optional<Student> existingOpt = studentDAO.findById(studentId);
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("Student with ID " + studentId + " does not exist.");
        }

        if (studentDAO.existsByEmail(email, studentId)) {
            throw new IllegalArgumentException("The email '" + email.trim() + "' is already used by another student.");
        }

        Student student = existingOpt.get();
        student.setStudentName(name.trim());
        student.setEmail(email.trim().toLowerCase());
        student.setContactInfo(contactInfo != null ? contactInfo.trim() : null);

        return studentDAO.update(student);
    }

    /**
     * Deletes a student by ID, cleaning up any dependent skills and exchange requests.
     */
    public boolean deleteStudent(int studentId) throws SQLException {
        if (studentId <= 0) {
            throw new IllegalArgumentException("Student ID must be a positive integer.");
        }

        Optional<Student> existing = studentDAO.findById(studentId);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Student with ID " + studentId + " not found.");
        }

        return studentDAO.delete(studentId);
    }
}
