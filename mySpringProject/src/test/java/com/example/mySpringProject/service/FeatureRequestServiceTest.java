package com.example.mySpringProject.service;

import com.example.mySpringProject.model.entity.FeatureRequest;
import com.example.mySpringProject.repository.FeatureRequestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeatureRequestServiceTest {
    @Test
    void shouldReturnFeatureRequestWhenIdExists() {shouldReturnFeatureRequestWhenIdExists();
        // ARRANGE - create fake data
        FeatureRequest fakeRequest = new FeatureRequest();
        fakeRequest.setId(1L);
        fakeRequest.setCustomerName("Arun Reddy");
        fakeRequest.setCustomerEmail("arun@gmail.com");

        // Tell fake repository what to return
        when(repository.findById(1L)).thenReturn(Optional.of(fakeRequest));

        // ACT - call the real method
        FeatureRequest result = featureRequestService.getFeatureRequestById(1L);

        // ASSERT - verify the result
        assertNotNull(result);
        assertEquals("Arun Reddy", result.getCustomerName());
    }
    @Test
    void shouldThrow404WhenIdNotFound() {
        // ARRANGE
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThrows(ResponseStatusException.class, () -> {
            featureRequestService.getFeatureRequestById(999L);
        });
    }
    @Test
    void shouldReturnAllFeatureRequests() {
        // ARRANGE
        List<FeatureRequest> fakeList = Arrays.asList(new FeatureRequest(), new FeatureRequest());
        when(repository.findAll()).thenReturn(fakeList);

        // ACT
        List<FeatureRequest> result = featureRequestService.getAllFeatureRequests();

        // ASSERT
        assertEquals(2, result.size());
    }
    @Mock
    private FeatureRequestRepository repository;

    @Mock
    private GitHubApiClientService githubApiClient;

    @InjectMocks
    private FeatureRequestService featureRequestService;

}