package com.skillswap.controller.api;

import com.skillswap.dto.ApiResponse;
import com.skillswap.dto.StudentRequest;
import com.skillswap.model.Student;
import com.skillswap.service.StudentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/students")
public class StudentApiController {

    private final StudentService studentService;

    public StudentApiController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Student>> loginStudent(@RequestBody Map<String, String> credentials) {
        try {
            if (credentials == null || !credentials.containsKey("email") || credentials.get("email").trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Email is required for login."));
            }
            String email = credentials.get("email").trim();
            Optional<Student> studentOpt = studentService.getStudentByEmail(email);
            if (studentOpt.isPresent()) {
                return ResponseEntity.ok(ApiResponse.ok("Login successful! Welcome back, " + studentOpt.get().getStudentName(), studentOpt.get()));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("No student account found with email '" + email + "'. Please check your email or register a new profile."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Database error: " + e.getMessage()));
        }
    }

    @GetMapping("/by-email")
    public ResponseEntity<ApiResponse<Student>> getStudentByEmail(@RequestParam String email) {
        try {
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Email parameter is required."));
            }
            Optional<Student> studentOpt = studentService.getStudentByEmail(email.trim());
            if (studentOpt.isPresent()) {
                return ResponseEntity.ok(ApiResponse.ok(studentOpt.get()));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Student with email '" + email + "' not found."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Database error: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Student>>> getAllStudents() {
        try {
            List<Student> students = studentService.getAllStudents();
            return ResponseEntity.ok(ApiResponse.ok(students));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Database error: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Student>> getStudentById(@PathVariable int id) {
        try {
            Optional<Student> student = studentService.getStudentById(id);
            if (student.isPresent()) {
                return ResponseEntity.ok(ApiResponse.ok(student.get()));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Student with ID " + id + " not found."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Database error: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Student>> registerStudent(@RequestBody StudentRequest request) {
        try {
            if (request == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Request body cannot be null."));
            }
            Student student = studentService.registerStudent(
                    request.getStudentName(),
                    request.getEmail(),
                    request.getContactInfo()
            );
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok("Student registered successfully!", student));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Database error: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateStudent(@PathVariable int id, @RequestBody StudentRequest request) {
        try {
            if (request == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Request body cannot be null."));
            }
            boolean updated = studentService.updateStudent(
                    id,
                    request.getStudentName(),
                    request.getEmail(),
                    request.getContactInfo()
            );
            if (updated) {
                return ResponseEntity.ok(ApiResponse.ok("Student details updated successfully.", null));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Student with ID " + id + " not found."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Database error: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(@PathVariable int id) {
        try {
            boolean deleted = studentService.deleteStudent(id);
            if (deleted) {
                return ResponseEntity.ok(ApiResponse.ok("Student #" + id + " deleted successfully.", null));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Student with ID " + id + " not found."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Database error: " + e.getMessage()));
        }
    }
}
