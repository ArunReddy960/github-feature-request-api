package com.example.mySpringProject.service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.example.mySpringProject.model.dto.FeatureRequestDTO;
import com.example.mySpringProject.repository.FeatureRequestRepository;
import com.example.mySpringProject.model.dto.GitHubIssueResponseDTO;
import com.example.mySpringProject.model.entity.FeatureRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for feature request processing
 *
 * Business Logic:
 * 1. Validate customer request
 * 2. Save to database
 * 3. Format feature request data
 * 4. Build GitHub issue
 * 5. Call GitHub API via GitHubApiClientService
 * 6. Update database with GitHub details
 * 7. Return response to controller
 */
@Slf4j
@Service
public class FeatureRequestService {

    @Autowired
    private GitHubApiClientService githubApiClient;

    @Autowired
    private FeatureRequestRepository repository;

    /**
     * Process a feature request and create GitHub issue
     *
     * NEW Flow with Database:
     * 1. Validate input
     * 2. Create Entity from DTO
     * 3. SAVE to database (get ID)
     * 4. Build GitHub issue
     * 5. Create GitHub issue
     * 6. UPDATE database with GitHub issue details
     * 7. Return response
     */
    public GitHubIssueResponseDTO processFeatureRequest(FeatureRequestDTO request) {
        try {
            log.info("📨 Processing feature request from: {}", request.getCustomerName());

            // Step 1: Validate


            // Step 2: Create Entity from DTO
            FeatureRequest entity = convertDtoToEntity(request);
            log.debug("✓ Created entity from DTO");

            // Step 3: Save to database FIRST (before GitHub)
            // Why? Get database ID and have audit trail even if GitHub fails
            FeatureRequest savedEntity = repository.save(entity);
            log.info("💾 Saved to database with ID: {}", savedEntity.getId());

            // Step 4: Build GitHub issue content
            String title = buildTitle(request);
            String body = buildBody(request);
            List<String> labels = assignLabels(request);
            List<String> assignees = assignAssignees(request);

            log.debug("📋 Built GitHub issue content");

            // Step 5: Create GitHub issue
            GitHubApiClientService.GitHubIssueResponse githubResponse =
                    githubApiClient.createIssue(title, body, labels, assignees);

            log.info("✅ GitHub issue created: #{}", githubResponse.getNumber());

            // Step 6: Update entity with GitHub details
            savedEntity.setGithubIssueNumber(githubResponse.getNumber());
            savedEntity.setGithubIssueUrl(githubResponse.getHtmlUrl());
            savedEntity.setGithubIssueId((long) githubResponse.getId());

            // Save updated entity
            repository.save(savedEntity);
            log.info("🔄 Updated database with GitHub issue #{}",  githubResponse.getNumber());

            // Step 7: Return success response
            return GitHubIssueResponseDTO.success(
                    githubResponse.getNumber(),
                    githubResponse.getId(),
                    githubResponse.getTitle(),
                    githubResponse.getState(),
                    githubResponse.getHtmlUrl(),
                    githubResponse.getCreatedAt()
            );

        } catch (IllegalArgumentException e) {
            log.error("❌ Validation error: {}", e.getMessage());
            return GitHubIssueResponseDTO.error("Validation Error: " + e.getMessage());

        } catch (GitHubApiClientService.GitHubApiException e) {
            log.error("❌ GitHub API error: {}", e.getMessage());

            // Note: Request is already saved in database!
            // We could implement retry logic here later

            return GitHubIssueResponseDTO.error("GitHub API Error: " + e.getMessage());

        } catch (Exception e) {
            log.error("❌ Unexpected error: {}", e.getMessage(), e);
            return GitHubIssueResponseDTO.error("Unexpected Error: " + e.getMessage());
        }
    }

    /**
     * Build GitHub issue title
     * Format: "Feature Request: [Customer Title]"
     */
    private String buildTitle(FeatureRequestDTO request) {
        return "Feature Request: " + request.getFeatureTitle();
    }

    /**
     * Build GitHub issue body
     * Includes customer details and formatted description
     * Uses markdown for GitHub display
     */
    private String buildBody(FeatureRequestDTO request) {
        StringBuilder body = new StringBuilder();

        // Customer Information
        body.append("## 👤 Customer Information\n\n");
        body.append("| Field | Value |\n");
        body.append("| --- | --- |\n");
        body.append("| Name | ").append(request.getCustomerName()).append(" |\n");
        body.append("| Email | ").append(request.getCustomerEmail()).append(" |\n");
        body.append("| Submission Date | ").append(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append(" |\n\n");

        // Feature Details
        body.append("## 📋 Feature Details\n\n");
        body.append("| Field | Value |\n");
        body.append("| --- | --- |\n");
        body.append("| Product | ").append(request.getTargetedProduct()).append(" |\n");
        body.append("| Category | ").append(request.getCategory()).append(" |\n");
        body.append("| Priority | ").append(request.getPriority()).append(" |\n\n");

        // Feature Description
        body.append("## 💡 Feature Description\n\n");
        body.append(request.getFeatureDescription()).append("\n\n");

        // Footer
        body.append("---\n");
        body.append("*This issue was automatically created from a customer feature request.*\n");
        body.append("*Please review, prioritize, and update as needed.*\n");

        return body.toString();
    }

    /**
     * Assign labels based on request details
     *
     * Labels include:
     * - feature-request (always)
     * - category-{category}
     * - product-{product}
     * - priority-{priority}
     */
    private List<String> assignLabels(FeatureRequestDTO request) {
        String categoryLabel = "category-" + request.getCategory()
                .toLowerCase()
                .replace(" ", "-")
                .replace("/", "-");

        String productLabel = "product-" + request.getTargetedProduct()
                .toLowerCase()
                .replace(" ", "-");

        String priorityLabel = "priority-" + request.getPriority()
                .toLowerCase();

        return Arrays.asList(
                "feature-request",
                categoryLabel,
                productLabel,
                priorityLabel
        );
    }

    /**
     * Assign issue to appropriate product manager
     * Routes based on targeted product
     */
    private List<String> assignAssignees(FeatureRequestDTO request) {
        // Don't assign to anyone
        return Arrays.asList();
    }

    /**
     * Convert DTO to Entity
     *
     * DTO = Data Transfer Object (what comes from API request)
     * Entity = Database object (what gets saved)
     *
     * We need to map fields from DTO to Entity
     */
    private FeatureRequest convertDtoToEntity(FeatureRequestDTO dto) {
        FeatureRequest entity = new FeatureRequest();

        // Map all fields from DTO to Entity
        entity.setCustomerName(dto.getCustomerName());
        entity.setCustomerEmail(dto.getCustomerEmail());
        entity.setFeatureTitle(dto.getFeatureTitle());
        entity.setFeatureDescription(dto.getFeatureDescription());
        entity.setCategory(dto.getCategory());
        entity.setPriority(dto.getPriority());
        entity.setTargetedProduct(dto.getTargetedProduct());

        // GitHub fields are null initially - will be set after GitHub API call
        // Timestamps are auto-set by @PrePersist in Entity

        return entity;
    }

    /**
     * Get all feature requests from database
     *
     * WHY: Allow users to see all saved feature requests via API
     * WHEN: Called by GET /api/feature-requests endpoint
     * RETURNS: List of all feature requests (could be empty)
     */
    public List<FeatureRequest> getAllFeatureRequests() {
        log.info("📋 Fetching all feature requests from database");

        // repository.findAll() - Spring Data JPA gives us this for FREE!
        List<FeatureRequest> requests = repository.findAll();

        log.info("✅ Found {} feature requests", requests.size());
        return requests;
    }

    /**
     * Get a specific feature request by ID
     *
     * WHY: Allow users to retrieve a single feature request
     * WHEN: Called by GET /api/feature-requests/{id} endpoint
     * RETURNS: The feature request if found
     * THROWS: RuntimeException if not found (we'll improve this later)
     */
    // NEW (easier to see each step):
    public FeatureRequest getFeatureRequestById(Long id) {
        log.info("🔍 Fetching feature request with ID: {}", id);

        // Step 1: Call database
        Optional<FeatureRequest> optionalRequest = repository.findById(id);  // ← BREAKPOINT HERE! 🔴

        // Step 2: Check if found
        if (optionalRequest.isEmpty()) {  // ← BREAKPOINT HERE TOO! 🔴
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Feature request not found: " + id);
        }

        // Step 3: Get the value
        FeatureRequest request = optionalRequest.get();  // ← BREAKPOINT HERE TOO! 🔴
        return request;
    }
    public FeatureRequest updateFeatureRequest(Long id, FeatureRequestDTO request) {
        log.info("✏️ Updating feature request with ID: {}", id);

        // Step 1: Find existing record
        Optional<FeatureRequest> optionalRequest = repository.findById(id);

        // Step 2: Check if found
        if (optionalRequest.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Feature request not found: " + id);
        }

        // Step 3: Get existing record
        FeatureRequest existing = optionalRequest.get();

        // Step 4: Update allowed fields from DTO
        existing.setCustomerName(request.getCustomerName());
        existing.setCustomerEmail(request.getCustomerEmail());
        existing.setFeatureTitle(request.getFeatureTitle());
        existing.setFeatureDescription(request.getFeatureDescription());
        existing.setCategory(request.getCategory());
        existing.setPriority(request.getPriority());
        existing.setTargetedProduct(request.getTargetedProduct());

        // Step 5: Save and return
        return repository.save(existing);
    }

    public void deleteFeatureRequest(Long id) {
        log.info("🗑️ Deleting feature request with ID: {}", id);

        // Step 1: Check if exists
        Optional<FeatureRequest> optionalRequest = repository.findById(id);

        // Step 2: If not found, throw 404
        if (optionalRequest.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Feature request not found: " + id);
        }

        // Step 3: Delete it
        repository.deleteById(id);
        log.info("✅ Deleted feature request with ID: {}", id);
    }
}