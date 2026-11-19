
package com.careerconnect.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.careerconnect.backend.dto.LeetCodeAnalysisDTO;
import com.careerconnect.backend.service.LeetCodeAnalysisService;

@RestController
public class LeetCodeAnalysisController {

    private final LeetCodeAnalysisService leetCodeAnalysisService;

    public LeetCodeAnalysisController(
            LeetCodeAnalysisService leetCodeAnalysisService) {

        this.leetCodeAnalysisService = leetCodeAnalysisService;
    }

    @GetMapping("/leetcode-analysis/{studentId}")
    public ResponseEntity<?> analyzeLeetCode(
            @PathVariable int studentId) {

        try {

            LeetCodeAnalysisDTO analysis =
                    leetCodeAnalysisService.analyzeStudent(studentId);

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

