package com.skillswap;

import com.skillswap.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StudentServiceTest {

    private StudentService studentService;

    @BeforeEach
    void setUp() {
        studentService = new StudentService();
    }

    @Test
    @DisplayName("Valid student name and email should pass validation")
    void testValidateStudentValid() {
        assertDoesNotThrow(() -> {
            studentService.validateStudent("Arun Kumar", "arun.kumar@example.com");
        });
    }

    @Test
    @DisplayName("Blank student name should throw IllegalArgumentException")
    void testValidateStudentBlankName() {
        Exception e1 = assertThrows(IllegalArgumentException.class, () -> {
            studentService.validateStudent("", "arun@example.com");
        });
        assertTrue(e1.getMessage().contains("Student name is required"));

        Exception e2 = assertThrows(IllegalArgumentException.class, () -> {
            studentService.validateStudent("   ", "arun@example.com");
        });
        assertTrue(e2.getMessage().contains("Student name is required"));
    }

    @Test
    @DisplayName("Invalid email formats should be rejected")
    void testValidateStudentInvalidEmail() {
        assertThrows(IllegalArgumentException.class, () -> {
            studentService.validateStudent("Arun", "invalid-email");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            studentService.validateStudent("Arun", "arun@");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            studentService.validateStudent("Arun", "@example.com");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            studentService.validateStudent("Arun", "");
        });
    }

    @Test
    @DisplayName("Negative or zero student ID should throw IllegalArgumentException")
    void testInvalidStudentId() {
        assertThrows(IllegalArgumentException.class, () -> {
            studentService.getStudentById(-1);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            studentService.deleteStudent(0);
        });
    }

    @Test
    @DisplayName("Blank email for getStudentByEmail should throw IllegalArgumentException")
    void testBlankEmailLookup() {
        assertThrows(IllegalArgumentException.class, () -> {
            studentService.getStudentByEmail("");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            studentService.getStudentByEmail("   ");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            studentService.getStudentByEmail(null);
        });
    }
}
