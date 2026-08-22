package com.careerconnect.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.careerconnect.backend.model.StudentProfile;
import com.careerconnect.backend.service.StudentProfileService;

@RestController
public class StudentProfileController {

    @Autowired
    private StudentProfileService studentProfileService;

    @GetMapping("/student-profiles/{studentId}")
    public ResponseEntity<StudentProfile> getProfile(
            @PathVariable int studentId) {

        StudentProfile profile =
                studentProfileService.getProfile(studentId);

        return ResponseEntity.ok(profile);
    }

    @PostMapping("/student-profiles/{studentId}")
    public ResponseEntity<StudentProfile> createProfile(
            @PathVariable int studentId,
            @RequestParam String skills,
            @RequestParam String projects,
            @RequestParam String githubUsername,
            @RequestParam String leetcodeUsername,
            @RequestParam String hackerrankUsername,
            @RequestParam String resumePath) {

        StudentProfile profile =
                studentProfileService.createProfile(
                        studentId,
                        skills,
                        projects,
                        githubUsername,
                        leetcodeUsername,
                        hackerrankUsername,
                        resumePath
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(profile);
    }
}