package com.example.mySpringProject.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Spring Service for GitHub API integration
 *
 * Handles:
 * - Building HTTP requests with proper headers
 * - Sending requests to GitHub API
 * - Parsing JSON responses
 * - Error handling
 *
 * Uses @Service annotation for Spring dependency injection
 */
@Slf4j
@Service
public class GitHubApiClientService {

    @Value("${github.token}")
    private String token;

    @Value("${github.owner}")
    private String owner;

    @Value("${github.repo}")
    private String repo;

    @Value("${github.api.url:https://api.github.com}")
    private String apiUrl;

    @Value("${github.api.version:2022-11-28}")
    private String apiVersion;

    @Value("${github.user.agent:FeatureRequestProcessor/1.0}")
    private String userAgent;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    /**
     * Constructor - Initialize HTTP client and JSON mapper
     */
    public GitHubApiClientService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Create a GitHub issue
     *
     * @param title Issue title
     * @param body Issue body (markdown)
     * @param labels List of labels
     * @param assignees List of assignees
     * @return GitHubIssueResponse object with issue details
     * @throws GitHubApiException if API call fails
     */
    public GitHubIssueResponse createIssue(String title, String body,
                                           java.util.List<String> labels,
                                           java.util.List<String> assignees) throws Exception {

        log.info("Creating GitHub issue: {}", title);

        // Validate
        validateRequest(title);

        // Build request body
        String requestBody = buildRequestBody(title, body, labels, assignees);
        log.debug("Request body: {}", requestBody);

        // Build HTTP request
        HttpRequest httpRequest = buildHttpRequest(requestBody);
        log.debug("HTTP Request URL: {}", httpRequest.uri());

        // Send request
        log.info("📤 Sending POST request to GitHub API...");
        HttpResponse<String> httpResponse = httpClient.send(httpRequest,
                HttpResponse.BodyHandlers.ofString());

        // Log response
        log.info("📥 Response Status: {}", httpResponse.statusCode());
        logResponseHeaders(httpResponse);

        // Handle response
        return handleResponse(httpResponse);
    }

    /**
     * Validate request before sending
     */
    private void validateRequest(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Issue title cannot be empty");
        }
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("GitHub token not configured");
        }
        if (owner == null || owner.isEmpty()) {
            throw new IllegalArgumentException("GitHub owner not configured");
        }
        if (repo == null || repo.isEmpty()) {
            throw new IllegalArgumentException("GitHub repo not configured");
        }
        log.debug("✓ Validation passed");
    }

    /**
     * Build JSON request body using ObjectMapper
     */
    private String buildRequestBody(String title, String body,
                                    java.util.List<String> labels,
                                    java.util.List<String> assignees) throws Exception {
        var issueMap = new java.util.HashMap<String, Object>();
        issueMap.put("title", title);
        if (body != null && !body.isEmpty()) {
            issueMap.put("body", body);
        }
        if (labels != null && !labels.isEmpty()) {
            issueMap.put("labels", labels);
        }
        if (assignees != null && !assignees.isEmpty()) {
            issueMap.put("assignees", assignees);
        }

        return objectMapper.writeValueAsString(issueMap);
    }

    /**
     * Build HTTP request with all required GitHub headers
     */
    private HttpRequest buildHttpRequest(String requestBody) {
        String url = apiUrl + "/repos/" + owner + "/" + repo + "/issues";

        log.debug("Building HTTP request for: {}", url);

        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", apiVersion)
                .header("User-Agent", userAgent)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Log important response headers
     */
    private void logResponseHeaders(HttpResponse<String> response) {
        response.headers().firstValue("X-RateLimit-Remaining").ifPresent(remaining ->
                log.debug("  X-RateLimit-Remaining: {}", remaining)
        );

        response.headers().firstValue("X-RateLimit-Reset").ifPresent(reset ->
                log.debug("  X-RateLimit-Reset: {}", reset)
        );
    }

    /**
     * Handle response based on status code
     */
    private GitHubIssueResponse handleResponse(HttpResponse<String> response) throws Exception {
        int statusCode = response.statusCode();
        String body = response.body();

        log.debug("Response body: {}", body);

        switch (statusCode) {
            case 201:
                return parseSuccessResponse(body);

            case 401:
                log.error("❌ 401 Unauthorized: Invalid GitHub token");
                throw new GitHubApiException(
                        "401 Unauthorized: Check your GitHub token is valid and not expired"
                );

            case 403:
                log.error("❌ 403 Forbidden: Check User-Agent header or rate limit");
                throw new GitHubApiException(
                        "403 Forbidden: Check User-Agent header or you're rate limited"
                );

            case 422:
                log.error("❌ 422 Unprocessable Entity: Validation error");
                log.error("Error details: {}", body);
                throw new GitHubApiException(
                        "422 Unprocessable Entity: " + body
                );

            case 404:
                log.error("❌ 404 Not Found: Repository not found");
                throw new GitHubApiException(
                        "404 Not Found: Repository " + owner + "/" + repo + " not found"
                );

            default:
                log.error("❌ HTTP {}: {}", statusCode, body);
                throw new GitHubApiException(
                        "HTTP " + statusCode + ": " + body
                );
        }
    }

    /**
     * Parse successful 201 response
     */
    private GitHubIssueResponse parseSuccessResponse(String body) throws Exception {
        JsonNode node = objectMapper.readTree(body);

        GitHubIssueResponse response = new GitHubIssueResponse(
                node.get("number").asInt(),
                node.get("id").asInt(),
                node.get("title").asText(),
                node.get("state").asText(),
                node.get("html_url").asText(),
                LocalDateTime.parse(
                        node.get("created_at").asText(),
                        DateTimeFormatter.ISO_DATE_TIME
                )
        );

        log.info("✅ Issue created successfully!");
        log.info("   Issue #: {}", response.getNumber());
        log.info("   URL: {}", response.getHtmlUrl());

        return response;
    }

    /**
     * Inner class for GitHub Issue Response
     */
    public static class GitHubIssueResponse {
        private final int number;
        private final int id;
        private final String title;
        private final String state;
        private final String htmlUrl;
        private final LocalDateTime createdAt;

        public GitHubIssueResponse(int number, int id, String title, String state,
                                   String htmlUrl, LocalDateTime createdAt) {
            this.number = number;
            this.id = id;
            this.title = title;
            this.state = state;
            this.htmlUrl = htmlUrl;
            this.createdAt = createdAt;
        }

        public int getNumber() { return number; }
        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getState() { return state; }
        public String getHtmlUrl() { return htmlUrl; }
        public LocalDateTime getCreatedAt() { return createdAt; }
    }

    /**
     * Custom exception for GitHub API errors
     */
    public static class GitHubApiException extends Exception {
        public GitHubApiException(String message) {
            super(message);
        }
    }
}