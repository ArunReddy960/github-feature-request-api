package com.example.mySpringProject.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "feature_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeatureRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_name", nullable = false, length = 255)
    private String customerName;

    @Column(name = "customer_email", nullable = false, length = 255)
    private String customerEmail;

    @Column(name = "feature_title", nullable = false, length = 500)
    private String featureTitle;

    @Column(name = "feature_description", nullable = false, columnDefinition = "TEXT")
    private String featureDescription;

    @Column(name = "category", nullable = false, length = 100)
    private String category;

    @Column(name = "priority", nullable = false, length = 50)
    private String priority;

    @Column(name = "targeted_product", nullable = false, length = 100)
    private String targetedProduct;

    @Column(name = "github_issue_number")
    private Integer githubIssueNumber;

    @Column(name = "github_issue_url", length = 500)
    private String githubIssueUrl;

    @Column(name = "github_issue_id")
    private Long githubIssueId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}