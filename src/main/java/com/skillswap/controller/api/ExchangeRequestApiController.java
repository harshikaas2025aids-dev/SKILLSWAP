package com.skillswap.controller.api;

import com.skillswap.dto.ApiResponse;
import com.skillswap.dto.CreateExchangeRequest;
import com.skillswap.dto.ExchangeActionRequest;
import com.skillswap.model.ExchangeRequest;
import com.skillswap.service.ExchangeRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/requests")
public class ExchangeRequestApiController {

    private final ExchangeRequestService requestService;

    public ExchangeRequestApiController(ExchangeRequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ExchangeRequest>> createRequest(@RequestBody CreateExchangeRequest body) {
        try {
            if (body == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Request body cannot be null."));
            }
            ExchangeRequest created = requestService.createExchangeRequest(
                    body.getRequesterId(),
                    body.getReceiverId(),
                    body.getOfferedSkillId(),
                    body.getWantedSkillId()
            );
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok("Exchange request created successfully!", created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(e.getMessage()));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Database error: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExchangeRequest>> getRequestById(@PathVariable int id) {
        try {
            Optional<ExchangeRequest> req = requestService.getRequestById(id);
            if (req.isPresent()) {
                return ResponseEntity.ok(ApiResponse.ok(req.get()));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Exchange request #" + id + " not found."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Database error: " + e.getMessage()));
        }
    }

    @GetMapping("/sent/{studentId}")
    public ResponseEntity<ApiResponse<List<ExchangeRequest>>> getSentRequests(@PathVariable int studentId) {
        try {
            List<ExchangeRequest> list = requestService.getSentRequests(studentId);
            return ResponseEntity.ok(ApiResponse.ok(list));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Database error: " + e.getMessage()));
        }
    }

    @GetMapping("/received/{studentId}")
    public ResponseEntity<ApiResponse<List<ExchangeRequest>>> getReceivedRequests(@PathVariable int studentId) {
        try {
            List<ExchangeRequest> list = requestService.getReceivedRequests(studentId);
            return ResponseEntity.ok(ApiResponse.ok(list));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Database error: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<ApiResponse<Void>> acceptRequest(
            @PathVariable int id, 
            @RequestBody(required = false) ExchangeActionRequest action) {
        try {
            int studentId = (action != null) ? action.getStudentId() : 0;
            boolean ok = requestService.acceptRequest(id, studentId);
            if (ok) {
                return ResponseEntity.ok(ApiResponse.ok("Request #" + id + " ACCEPTED successfully!", null));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Request #" + id + " not found."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(e.getMessage()));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Database error: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectRequest(
            @PathVariable int id, 
            @RequestBody(required = false) ExchangeActionRequest action) {
        try {
            int studentId = (action != null) ? action.getStudentId() : 0;
            boolean ok = requestService.rejectRequest(id, studentId);
            if (ok) {
                return ResponseEntity.ok(ApiResponse.ok("Request #" + id + " REJECTED.", null));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Request #" + id + " not found."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(e.getMessage()));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Database error: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelRequest(
            @PathVariable int id, 
            @RequestBody(required = false) ExchangeActionRequest action) {
        try {
            int studentId = (action != null) ? action.getStudentId() : 0;
            boolean ok = requestService.cancelRequest(id, studentId);
            if (ok) {
                return ResponseEntity.ok(ApiResponse.ok("Request #" + id + " CANCELLED.", null));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Request #" + id + " not found."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(e.getMessage()));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Database error: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<Void>> completeRequest(
            @PathVariable int id, 
            @RequestBody(required = false) ExchangeActionRequest action) {
        try {
            int studentId = (action != null) ? action.getStudentId() : 0;
            boolean ok = requestService.completeRequest(id, studentId);
            if (ok) {
                return ResponseEntity.ok(ApiResponse.ok("Exchange #" + id + " marked as COMPLETED!", null));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Request #" + id + " not found."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(e.getMessage()));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Database error: " + e.getMessage()));
        }
    }
}
