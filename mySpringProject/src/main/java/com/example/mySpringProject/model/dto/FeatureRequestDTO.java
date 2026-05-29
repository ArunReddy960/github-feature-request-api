package com.example.mySpringProject.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeatureRequestDTO {

    @JsonProperty("customerName")
    private String customerName;

    @JsonProperty("customerEmail")
    private String customerEmail;

    @JsonProperty("featureTitle")
    private String featureTitle;

    @JsonProperty("featureDescription")
    private String featureDescription;

    @JsonProperty("category")
    private String category;

    @JsonProperty("priority")
    private String priority;

    @JsonProperty("targetedProduct")
    private String targetedProduct;

    public void validate() {
        if (customerName == null || customerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name is required");
        }
        if (customerEmail == null || customerEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer email is required");
        }
        if (featureTitle == null || featureTitle.trim().isEmpty()) {
            throw new IllegalArgumentException("Feature title is required");
        }
        if (featureDescription == null || featureDescription.trim().isEmpty()) {
            throw new IllegalArgumentException("Feature description is required");
        }
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Category is required");
        }
        if (priority == null || priority.trim().isEmpty()) {
            throw new IllegalArgumentException("Priority is required");
        }
        if (targetedProduct == null || targetedProduct.trim().isEmpty()) {
            throw new IllegalArgumentException("Targeted product is required");
        }
    }
}