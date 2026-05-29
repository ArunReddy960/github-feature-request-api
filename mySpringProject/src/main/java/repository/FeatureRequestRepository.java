package com.example.mySpringProject.repository;

import com.example.mySpringProject.model.entity.FeatureRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeatureRequestRepository extends JpaRepository<FeatureRequest, Long> {

    List<FeatureRequest> findByCustomerEmail(String email);

    List<FeatureRequest> findByPriority(String priority);

    List<FeatureRequest> findByCategory(String category);

    FeatureRequest findByGithubIssueNumber(Integer issueNumber);

    List<FeatureRequest> findByTargetedProduct(String product);

    List<FeatureRequest> findByPriorityAndCategory(String priority, String category);

    List<FeatureRequest> findByCustomerNameContaining(String namePart);
}