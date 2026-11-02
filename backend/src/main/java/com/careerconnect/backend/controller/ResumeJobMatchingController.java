
package com.careerconnect.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.careerconnect.backend.service.ResumeJobMatchingService;

@RestController
@RequestMapping("/ai/resume-job-match")
public class ResumeJobMatchingController {

    private final ResumeJobMatchingService matchingService;

    public ResumeJobMatchingController(
            ResumeJobMatchingService matchingService) {
        this.matchingService = matchingService;
    }

    // Day 69: Match resume with one job.
    @GetMapping("/{studentId}/{jobId}")
    public Map<String, Object> getResumeJobMatch(
            @PathVariable int studentId,
            @PathVariable int jobId) {

        return matchingService.analyze(studentId, jobId);
    }

    // Day 70: Match resume with all available jobs.
    @GetMapping("/all/{studentId}")
    public List<Map<String, Object>> getAllResumeJobMatches(
            @PathVariable int studentId) {

        return matchingService.analyzeAll(studentId);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleInvalidRequest(
            IllegalArgumentException exception) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", exception.getMessage()));
    }
}


