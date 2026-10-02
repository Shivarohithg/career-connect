package com.careerconnect.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.careerconnect.backend.model.Job;
import com.careerconnect.backend.model.Student;
import com.careerconnect.backend.model.StudentProfile;
import com.careerconnect.backend.repository.JobRepository;
import com.careerconnect.backend.repository.StudentProfileRepository;
import com.careerconnect.backend.repository.StudentRepository;
import com.careerconnect.backend.service.AIJobRecommendationService;
import com.careerconnect.backend.service.JobAIInsightService;

@RestController
public class JobAIInsightController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private AIJobRecommendationService aiJobRecommendationService;

    @Autowired
    private JobAIInsightService jobAIInsightService;

    @GetMapping("/ai/job-insight/{studentId}/{jobId}")
    public ResponseEntity<?> getJobInsight(
            @PathVariable int studentId,
            @PathVariable int jobId) {

        try {

            Student student = studentRepository
                    .findById(studentId)
                    .orElseThrow(() ->
                            new RuntimeException("Student not found"));

            StudentProfile profile =
                    studentProfileRepository
                            .findByStudent(student)
                            .orElseThrow(() ->
                                    new RuntimeException("Profile not found"));

            Job job = jobRepository
                    .findById(jobId)
                    .orElseThrow(() ->
                            new RuntimeException("Job not found"));

            /*
             * Get the student's recommendations.
             * We reuse the existing deterministic matching system
             * instead of creating a second matching algorithm.
             */
            var recommendations =
                    aiJobRecommendationService
                            .recommendJobs(studentId);

            var recommendation = recommendations.stream()
                    .filter(item ->
                            item.getJob().getId() == jobId)
                    .findFirst()
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "No recommendation found for this job"));

            String insight =
                    jobAIInsightService.generateJobInsight(
                            studentId,
                            jobId,
                            recommendation.getMatchedRole(),
                            recommendation.getMatchPercentage(),
                            recommendation.getMatchedSkills(),
                            recommendation.getMissingSkills()
                    );

            return ResponseEntity.ok(insight);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
}
