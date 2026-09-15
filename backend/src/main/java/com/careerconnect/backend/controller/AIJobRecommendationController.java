package com.careerconnect.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.careerconnect.backend.service.AIJobRecommendationService;
import com.careerconnect.backend.service.AIJobRecommendationService.JobRecommendation;

@RestController
public class AIJobRecommendationController {

    @Autowired
    private AIJobRecommendationService recommendationService;

    @GetMapping("/ai/job-recommendations/{studentId}")
    public List<JobRecommendation> getRecommendations(
            @PathVariable int studentId) {

        return recommendationService
                .recommendJobs(studentId);
    }
}