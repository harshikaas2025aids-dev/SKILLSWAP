package com.skillswap.controller.api;

import com.skillswap.dto.ApiResponse;
import com.skillswap.dto.DashboardStats;
import com.skillswap.model.ExchangeRequest;
import com.skillswap.model.Skill;
import com.skillswap.model.SkillMatch;
import com.skillswap.model.Student;
import com.skillswap.service.ExchangeRequestService;
import com.skillswap.service.SkillMatchingService;
import com.skillswap.service.SkillService;
import com.skillswap.service.StudentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardApiController {

    private final StudentService studentService;
    private final SkillService skillService;
    private final SkillMatchingService matchingService;
    private final ExchangeRequestService requestService;

    public DashboardApiController(StudentService studentService, 
                                  SkillService skillService, 
                                  SkillMatchingService matchingService, 
                                  ExchangeRequestService requestService) {
        this.studentService = studentService;
        this.skillService = skillService;
        this.matchingService = matchingService;
        this.requestService = requestService;
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStats>> getDashboardStats(
            @RequestParam(required = false, defaultValue = "1") int studentId) {
        try {
            DashboardStats stats = new DashboardStats();
            stats.setCurrentStudentId(studentId);

            // Fetch student name if student exists
            Optional<Student> studentOpt = studentService.getStudentById(studentId);
            if (studentOpt.isPresent()) {
                stats.setCurrentStudentName(studentOpt.get().getStudentName());
            } else {
                stats.setCurrentStudentName("Student #" + studentId);
            }

            // Global stats
            List<Student> allStudents = studentService.getAllStudents();
            stats.setTotalStudents(allStudents.size());

            List<Skill> allSkills = skillService.getAllSkills();
            long offered = allSkills.stream().filter(s -> Skill.TYPE_TEACH.equalsIgnoreCase(s.getSkillType())).count();
            long wanted = allSkills.stream().filter(s -> Skill.TYPE_LEARN.equalsIgnoreCase(s.getSkillType())).count();
            stats.setSkillsOffered((int) offered);
            stats.setSkillsWanted((int) wanted);

            // Student-specific stats
            List<ExchangeRequest> sent = requestService.getSentRequests(studentId);
            List<ExchangeRequest> received = requestService.getReceivedRequests(studentId);

            List<ExchangeRequest> allForStudent = new ArrayList<>(sent);
            allForStudent.addAll(received);

            long pending = allForStudent.stream()
                    .filter(r -> ExchangeRequest.STATUS_PENDING.equalsIgnoreCase(r.getStatus()))
                    .count();
            long accepted = allForStudent.stream()
                    .filter(r -> ExchangeRequest.STATUS_ACCEPTED.equalsIgnoreCase(r.getStatus()))
                    .count();
            long completed = allForStudent.stream()
                    .filter(r -> ExchangeRequest.STATUS_COMPLETED.equalsIgnoreCase(r.getStatus()))
                    .count();

            stats.setPendingRequests((int) pending);
            stats.setAcceptedExchanges((int) accepted);
            stats.setCompletedExchanges((int) completed);

            // Mutual smart matches
            List<SkillMatch> matches = matchingService.findMatchesForStudent(studentId);
            stats.setMutualMatchesCount(matches.size());
            stats.setRecommendedPartners(matches.stream().limit(5).toList());

            // Recent activity sorted by requestDate descending
            allForStudent.sort(Comparator.comparing(ExchangeRequest::getRequestDate, Comparator.nullsLast(Comparator.reverseOrder())));
            stats.setRecentActivity(allForStudent.stream().limit(6).toList());

            return ResponseEntity.ok(ApiResponse.ok(stats));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Database error: " + e.getMessage()));
        }
    }
}
