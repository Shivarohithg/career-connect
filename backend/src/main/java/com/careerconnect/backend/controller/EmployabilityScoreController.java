
package com.careerconnect.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.careerconnect.backend.dto.EmployabilityScoreDTO;
import com.careerconnect.backend.service.EmployabilityScoreService;

@RestController
public class EmployabilityScoreController {

    @Autowired
    private EmployabilityScoreService employabilityScoreService;

    @GetMapping("/employability-score/{studentId}")
    public ResponseEntity<?> getEmployabilityScore(
            @PathVariable int studentId) {

        try {

            EmployabilityScoreDTO score =
                    employabilityScoreService.calculateScore(studentId);

            return ResponseEntity.ok(score);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }
}

