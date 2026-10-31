
package com.careerconnect.backend.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.careerconnect.backend.service.ResumeQualityAnalysisService;

@RestController
public class ResumeQualityAnalysisController {

    @Autowired
    private ResumeQualityAnalysisService resumeQualityAnalysisService;

    @GetMapping("/ai/resume-quality/{studentId}")
    public ResponseEntity<?> analyzeResume(
            @PathVariable int studentId) {

        try {
            Map<String, Object> result =
                    resumeQualityAnalysisService.analyzeResume(studentId);

            return ResponseEntity.ok(result);

        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }
}

