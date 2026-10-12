package com.skillswap.controller.api;

import com.skillswap.dto.ApiResponse;
import com.skillswap.dto.SkillRequest;
import com.skillswap.model.Skill;
import com.skillswap.service.SkillService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class SkillApiController {

    private final SkillService skillService;

    public SkillApiController(SkillService skillService) {
        this.skillService = skillService;
    }

    @GetMapping("/skills")
    public ResponseEntity<ApiResponse<List<Skill>>> getAllSkills(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String proficiency) {
        try {
            List<Skill> skills;
            if (type != null && !type.trim().isEmpty()) {
                skills = skillService.filterSkillsByType(type);
            } else if (proficiency != null && !proficiency.trim().isEmpty()) {
                skills = skillService.filterSkillsByProficiency(proficiency);
            } else {
                skills = skillService.getAllSkills();
            }
            return ResponseEntity.ok(ApiResponse.ok(skills));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Database error: " + e.getMessage()));
        }
    }

    @GetMapping("/students/{id}/skills")
    public ResponseEntity<ApiResponse<List<Skill>>> getSkillsByStudentId(@PathVariable int id) {
        try {
            List<Skill> skills = skillService.getSkillsByStudentId(id);
            return ResponseEntity.ok(ApiResponse.ok(skills));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Database error: " + e.getMessage()));
        }
    }

    @GetMapping("/skills/search")
    public ResponseEntity<ApiResponse<List<Skill>>> searchSkills(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String proficiency) {
        try {
            List<Skill> results;
            if (keyword != null && !keyword.trim().isEmpty()) {
                results = skillService.searchSkillsByName(keyword);
            } else if (type != null && !type.trim().isEmpty()) {
                results = skillService.filterSkillsByType(type);
            } else if (proficiency != null && !proficiency.trim().isEmpty()) {
                results = skillService.filterSkillsByProficiency(proficiency);
            } else {
                results = skillService.getAllSkills();
            }

            // Further filter in-memory if multiple criteria provided
            if (type != null && !type.trim().isEmpty()) {
                results = results.stream()
                        .filter(s -> s.getSkillType().equalsIgnoreCase(type.trim()))
                        .toList();
            }
            if (proficiency != null && !proficiency.trim().isEmpty()) {
                results = results.stream()
                        .filter(s -> s.getProficiencyLevel().equalsIgnoreCase(proficiency.trim()))
                        .toList();
            }

            return ResponseEntity.ok(ApiResponse.ok(results));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Database error: " + e.getMessage()));
        }
    }

    @GetMapping("/skills/{id}")
    public ResponseEntity<ApiResponse<Skill>> getSkillById(@PathVariable int id) {
        try {
            Optional<Skill> skill = skillService.getSkillById(id);
            if (skill.isPresent()) {
                return ResponseEntity.ok(ApiResponse.ok(skill.get()));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Skill #" + id + " not found."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Database error: " + e.getMessage()));
        }
    }

    @PostMapping("/skills")
    public ResponseEntity<ApiResponse<Skill>> createSkill(@RequestBody SkillRequest request) {
        try {
            if (request == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Request body cannot be null."));
            }
            Skill created = skillService.addSkill(
                    request.getStudentId(),
                    request.getSkillName(),
                    request.getSkillType(),
                    request.getProficiencyLevel(),
                    request.getDescription()
            );
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok("Skill registered successfully!", created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Database error: " + e.getMessage()));
        }
    }

    @PutMapping("/skills/{id}")
    public ResponseEntity<ApiResponse<Void>> updateSkill(@PathVariable int id, @RequestBody SkillRequest request) {
        try {
            if (request == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Request body cannot be null."));
            }
            boolean updated = skillService.updateSkill(
                    id,
                    request.getStudentId(),
                    request.getSkillName(),
                    request.getSkillType(),
                    request.getProficiencyLevel(),
                    request.getDescription()
            );
            if (updated) {
                return ResponseEntity.ok(ApiResponse.ok("Skill updated successfully.", null));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Skill #" + id + " not found."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Database error: " + e.getMessage()));
        }
    }

    @DeleteMapping("/skills/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSkill(
            @PathVariable int id,
            @RequestParam(required = false, defaultValue = "0") int studentId) {
        try {
            boolean deleted = skillService.deleteSkill(id, studentId);
            if (deleted) {
                return ResponseEntity.ok(ApiResponse.ok("Skill #" + id + " deleted successfully.", null));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Skill #" + id + " not found."));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Database error: " + e.getMessage()));
        }
    }
}
