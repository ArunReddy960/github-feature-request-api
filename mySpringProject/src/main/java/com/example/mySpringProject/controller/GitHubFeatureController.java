package com.example.mySpringProject.controller;

import com.example.mySpringProject.model.dto.FeatureRequestDTO;
import com.example.mySpringProject.model.dto.GitHubIssueResponseDTO;
import com.example.mySpringProject.service.FeatureRequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.mySpringProject.model.entity.FeatureRequest;
import java.util.List;
import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/feature-requests")
@CrossOrigin(origins = "*", maxAge = 3600)
public class GitHubFeatureController {

    @Autowired
    private FeatureRequestService featureRequestService;

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        log.info("Health check requested");
        return ResponseEntity.ok("Feature Request Processor is running ✅");
    }

    @PostMapping
    public ResponseEntity<GitHubIssueResponseDTO> createFeatureRequest(
            @Valid @RequestBody FeatureRequestDTO request) {

        log.info("📨 Received feature request from: {}", request.getCustomerName());
        log.debug("Request details: title={}, product={}, priority={}",
                request.getFeatureTitle(),
                request.getTargetedProduct(),
                request.getPriority());

        try {
            GitHubIssueResponseDTO response = featureRequestService.processFeatureRequest(request);

            if (response.isSuccess()) {
                log.info("✅ Feature request processed successfully. Issue #: {}",
                        response.getIssueNumber());
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                log.error("❌ Feature request processing failed: {}", response.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

        } catch (Exception e) {
            log.error("❌ Unexpected error processing feature request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GitHubIssueResponseDTO.error("Unexpected error: " + e.getMessage()));
        }
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<GitHubIssueResponseDTO> handleBadRequest(Exception e) {
        log.error("Invalid request format: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(GitHubIssueResponseDTO.error("Invalid request format: " + e.getMessage()));
    }
    /**
     * GET all feature requests
     *
     * Endpoint: GET /api/feature-requests
     * Returns: List of all feature requests in database
     * Status: 200 OK
     *
     * Example: GET http://localhost:8080/api/feature-requests
     */
    @GetMapping  // ✅ CORRECT! No path, uses base from @RequestMapping
    public ResponseEntity<List<FeatureRequest>> getAllFeatureRequests()  {
        log.info("📋 GET request: Fetch all feature requests");

        // Call service to get data from database
        List<FeatureRequest> requests = featureRequestService.getAllFeatureRequests();

        // Return 200 OK with the data
        return ResponseEntity.ok(requests);
    }

    /**
     * GET a specific feature request by ID
     *
     * Endpoint: GET /api/feature-requests/{id}
     * Parameter: id - The database ID of the feature request
     * Returns: Single feature request
     * Status: 200 OK if found, 500 if not found (we'll improve this later)
     *
     * Example: GET http://localhost:8080/api/feature-requests/3
     */
    @GetMapping("/{id}")  // ✅ CORRECT! Just the ID part
    public ResponseEntity<FeatureRequest> getFeatureRequestById(@PathVariable Long id) {
        log.info("🔍 GET request: Fetch feature request with ID: {}", id);

        // Call service to find the specific request
        FeatureRequest request = featureRequestService.getFeatureRequestById(id);

        // Return 200 OK with the data
        return ResponseEntity.ok(request);
    }
    @PutMapping("/{id}")
    public ResponseEntity<FeatureRequest> updateFeatureRequest(@Valid
            @PathVariable Long id,
            @RequestBody FeatureRequestDTO request) {

        log.info("✏️ PUT request: Update feature request with ID: {}", id);

        FeatureRequest updated = featureRequestService.updateFeatureRequest(id, request);

        return ResponseEntity.ok(updated);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFeatureRequest(@PathVariable Long id) {
        log.info("🗑️ DELETE request: Delete feature request with ID: {}", id);

        // Call service to delete
        featureRequestService.deleteFeatureRequest(id);

        // Return 204 No Content
        return ResponseEntity.noContent().build();
    }
}