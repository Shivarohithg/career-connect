package com.careerconnect.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.careerconnect.backend.model.Application;
import com.careerconnect.backend.service.ApplicationService;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    @PostMapping("/applications/{studentId}/{jobId}")
    public ResponseEntity<?> applyForJob(
            @PathVariable int studentId,
            @PathVariable int jobId) {

        try {

            Application application =
                    applicationService.applyForJob(studentId, jobId);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(application);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(e.getMessage());
        }
    }

    @GetMapping("/applications/student/{studentId}")
    public ResponseEntity<?> getApplicationsByStudent(
            @PathVariable int studentId) {

        try {

            List<Application> applications =
                    applicationService.getApplicationsByStudent(studentId);

            return ResponseEntity.ok(applications);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/applications/{applicationId}/status")
    public ResponseEntity<?> updateApplicationStatus(
            @PathVariable int applicationId,
            @RequestParam String status) {

        try {

            Application application =
                    applicationService.updateApplicationStatus(
                            applicationId, status);

            return ResponseEntity.ok(application);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }
}