package com.example.mySpringProject.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeatureRequestDTO {

    @NotBlank(message = "Customer name is required")
    @JsonProperty("customerName")
    private String customerName;

    @NotBlank(message = "Customer email is required")
    @Email(message = "Customer email must be valid")
    @JsonProperty("customerEmail")
    private String customerEmail;

    @NotBlank(message = "Feature title is required")
    @JsonProperty("featureTitle")
    private String featureTitle;

    @NotBlank(message = "Feature description is required")
    @JsonProperty("featureDescription")
    private String featureDescription;

    @NotBlank(message = "Category is required")
    @JsonProperty("category")
    private String category;

    @NotBlank(message = "Priority is required")
    @JsonProperty("priority")
    private String priority;

    @NotBlank(message = "Targeted product is required")
    @JsonProperty("targetedProduct")
    private String targetedProduct;
}