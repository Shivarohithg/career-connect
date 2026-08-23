package com.careerconnect.backend.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.careerconnect.backend.model.StudentProfile;
import com.careerconnect.backend.service.StudentProfileService;

@RestController
public class ResumeController {

    private final String uploadDirectory = "uploads/resumes/";

    @Autowired
    private StudentProfileService studentProfileService;

    @PostMapping("/student-profiles/{studentId}/resume")
    public ResponseEntity<String> uploadResume(
            @PathVariable int studentId,
            @RequestParam("resume") MultipartFile file) {

        try {

            if (file.isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body("Please select a resume file.");
            }

            String fileName = file.getOriginalFilename();

            Path directory = Paths.get(uploadDirectory);

            Files.createDirectories(directory);

            Path filePath = directory.resolve(fileName);

            Files.write(filePath, file.getBytes());

            // Save uploaded resume path in StudentProfile
            StudentProfile profile =
                    studentProfileService.updateResumePath(
                            studentId,
                            filePath.toString()
                    );

            return ResponseEntity.ok(
                    "Resume uploaded successfully: "
                            + profile.getResumePath()
            );

        } catch (IOException e) {

            return ResponseEntity
                    .internalServerError()
                    .body("Failed to upload resume.");

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
}