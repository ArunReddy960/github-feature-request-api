package com.example.mySpringProject.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for GitHub issue response
 *
 * Sent back to the client after issue is created on GitHub
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GitHubIssueResponseDTO {

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("issueNumber")
    private int issueNumber;

    @JsonProperty("issueId")
    private int issueId;

    @JsonProperty("title")
    private String title;

    @JsonProperty("state")
    private String state;

    @JsonProperty("issueUrl")
    private String issueUrl;

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @JsonProperty("message")
    private String message;

    /**
     * Create a success response
     */
    public static GitHubIssueResponseDTO success(int issueNumber, int issueId,
                                                 String title, String state,
                                                 String issueUrl, LocalDateTime createdAt) {
        return new GitHubIssueResponseDTO(
                true,
                issueNumber,
                issueId,
                title,
                state,
                issueUrl,
                createdAt,
                "Feature request created successfully"
        );
    }

    /**
     * Create an error response
     */
    public static GitHubIssueResponseDTO error(String message) {
        return new GitHubIssueResponseDTO(
                false,
                0,
                0,
                null,
                null,
                null,
                null,
                message
        );
    }
}