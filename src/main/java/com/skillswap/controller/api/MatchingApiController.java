package com.skillswap.controller.api;

import com.skillswap.dto.ApiResponse;
import com.skillswap.model.SkillMatch;
import com.skillswap.service.SkillMatchingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class MatchingApiController {

    private final SkillMatchingService matchingService;

    public MatchingApiController(SkillMatchingService matchingService) {
        this.matchingService = matchingService;
    }

    @GetMapping("/students/{id}/matches")
    public ResponseEntity<ApiResponse<List<SkillMatch>>> getMatchesForStudent(@PathVariable int id) {
        try {
            List<SkillMatch> matches = matchingService.findMatchesForStudent(id);
            return ResponseEntity.ok(ApiResponse.ok(matches));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Database error: " + e.getMessage()));
        }
    }

    @GetMapping("/matches")
    public ResponseEntity<ApiResponse<List<SkillMatch>>> getAllMatches() {
        try {
            List<SkillMatch> matches = matchingService.findAllMatches();
            return ResponseEntity.ok(ApiResponse.ok(matches));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Database error: " + e.getMessage()));
        }
    }
}
