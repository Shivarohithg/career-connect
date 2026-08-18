package com.careerconnect.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.careerconnect.backend.model.Application;
import com.careerconnect.backend.service.ApplicationService;

@RestController
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    @PostMapping("/applications/{studentId}/{jobId}")
    public ResponseEntity<Application> applyForJob(
            @PathVariable int studentId,
            @PathVariable int jobId) {

        Application application =
                applicationService.applyForJob(studentId, jobId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(application);
    }
}
