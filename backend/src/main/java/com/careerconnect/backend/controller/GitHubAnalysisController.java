
package com.careerconnect.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.careerconnect.backend.dto.GitHubAnalysisDTO;
import com.careerconnect.backend.service.GitHubAnalysisService;

@RestController
public class GitHubAnalysisController {

    private final GitHubAnalysisService gitHubAnalysisService;

    public GitHubAnalysisController(
            GitHubAnalysisService gitHubAnalysisService) {
        this.gitHubAnalysisService = gitHubAnalysisService;
    }

    @GetMapping("/github-analysis/{studentId}")
    public ResponseEntity<?> analyzeGitHub(
            @PathVariable int studentId) {

        try {
            GitHubAnalysisDTO analysis =
                    gitHubAnalysisService.analyzeStudent(studentId);

            return ResponseEntity.ok(analysis);

        } catch (RuntimeException e) {

            HttpStatus status = e.getMessage() != null
                    && (e.getMessage().contains("not found")
                    || e.getMessage().contains("Please add"))
                    ? HttpStatus.NOT_FOUND
                    : HttpStatus.BAD_REQUEST;

            return ResponseEntity.status(status)
                    .body(e.getMessage());
        }
    }
}

