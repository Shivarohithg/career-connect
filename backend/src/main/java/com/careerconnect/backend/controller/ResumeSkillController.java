package com.careerconnect.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.careerconnect.backend.model.StudentProfile;
import com.careerconnect.backend.service.ResumeSkillExtractionService;

@RestController
public class ResumeSkillController {

    @Autowired
    private ResumeSkillExtractionService resumeSkillExtractionService;

    @PostMapping("/student-profiles/{studentId}/extract-resume-skills")
    public ResponseEntity<String> extractResumeSkills(
            @PathVariable int studentId) {

        try {

            StudentProfile profile =
                    resumeSkillExtractionService
                            .extractAndSaveSkills(studentId);

            String skills = profile.getResumeSkills();

            if (skills == null || skills.trim().isEmpty()) {
                return ResponseEntity.ok(
                        "No known technical skills found in resume."
                );
            }

            return ResponseEntity.ok(
                    "Resume skills extracted successfully: "
                            + skills
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
}
