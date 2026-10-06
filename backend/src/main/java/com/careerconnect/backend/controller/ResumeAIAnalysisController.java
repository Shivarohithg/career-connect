package com.careerconnect.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.careerconnect.backend.service.ResumeAIAnalysisService;

@RestController
public class ResumeAIAnalysisController {

    @Autowired
    private ResumeAIAnalysisService resumeAIAnalysisService;

    @GetMapping("/ai/resume-analysis/{studentId}")
    public ResponseEntity<?> analyzeResume(
            @PathVariable int studentId) {

        try {

            String analysis =
                    resumeAIAnalysisService
                            .analyzeResume(studentId);

            return ResponseEntity.ok(analysis);

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }
}
