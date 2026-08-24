package com.careerconnect.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.careerconnect.backend.model.CareerAnalysis;
import com.careerconnect.backend.service.CareerAnalysisService;

@RestController
public class CareerAnalysisController {

    @Autowired
    private CareerAnalysisService careerAnalysisService;

    @GetMapping("/career-analysis/{studentId}")
    public ResponseEntity<?> getAnalysis(
            @PathVariable int studentId) {

        try {

            CareerAnalysis analysis =
                    careerAnalysisService.getAnalysis(studentId);

            return ResponseEntity.ok(analysis);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/career-analysis/{studentId}")
    public ResponseEntity<?> createAnalysis(
            @PathVariable int studentId) {

        try {

            CareerAnalysis analysis =
                    careerAnalysisService.createAnalysis(studentId);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(analysis);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(e.getMessage());
        }
    }
}