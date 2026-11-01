
package com.careerconnect.backend.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.careerconnect.backend.model.StudentProfile;
import com.careerconnect.backend.service.ResumeSkillExtractionService;
import com.careerconnect.backend.service.StudentProfileService;

@RestController
public class ResumeController {

    private final String uploadDirectory = "uploads/resumes/";

    @Autowired
    private StudentProfileService studentProfileService;

    @Autowired
    private ResumeSkillExtractionService resumeSkillExtractionService;

    @PostMapping("/student-profiles/{studentId}/resume")
    public ResponseEntity<String> uploadResume(
            @PathVariable int studentId,
            @RequestParam("resume") MultipartFile file) {

        try {

            // 1. Validate uploaded file
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Please select a resume file.");
            }

            String originalFileName = file.getOriginalFilename();

            if (originalFileName == null ||
                    !originalFileName.toLowerCase().endsWith(".pdf")) {

                return ResponseEntity.badRequest()
                        .body("Only PDF resumes are supported.");
            }

            // Avoid using directory information supplied by the filename.
            String fileName = Paths.get(originalFileName)
                    .getFileName()
                    .toString();

            if (fileName.isBlank()) {
                return ResponseEntity.badRequest()
                        .body("Invalid resume filename.");
            }

            // 2. Create upload directory
            Path directory = Paths.get(uploadDirectory)
                    .toAbsolutePath()
                    .normalize();

            Files.createDirectories(directory);

            // 3. Save PDF
            Path filePath = directory.resolve(fileName)
                    .normalize();

            if (!filePath.startsWith(directory)) {
                return ResponseEntity.badRequest()
                        .body("Invalid resume file path.");
            }

            byte[] fileBytes = file.getBytes();

            Files.write(filePath, fileBytes);

            System.out.println("========== RESUME UPLOAD ==========");
            System.out.println("Student ID: " + studentId);
            System.out.println("Resume file: " + fileName);
            System.out.println("Saved path: " + filePath);
            System.out.println("File size: " + fileBytes.length);

            // 4. Extract text using PDFBox
            String resumeText;

            try (PDDocument document = Loader.loadPDF(fileBytes)) {

                PDFTextStripper stripper = new PDFTextStripper();

                resumeText = stripper.getText(document).trim();
            }

            System.out.println("Extracted text length: "
                    + resumeText.length());

            if (resumeText.isEmpty()) {

                Files.deleteIfExists(filePath);

                return ResponseEntity.badRequest()
                        .body(
                            "Could not extract text from the resume. "
                            + "Please upload a text-based PDF."
                        );
            }

            System.out.println("Extracted text preview: "
                    + resumeText.substring(
                            0,
                            Math.min(200, resumeText.length())
                    ));

            // 5. Save resume path and extracted text
            StudentProfile savedProfile =
                    studentProfileService.updateResume(
                            studentId,
                            filePath.toString(),
                            resumeText
                    );

            // 6. Verify that extracted text was saved
            if (savedProfile.getResumeText() == null ||
                    savedProfile.getResumeText().isBlank()) {

                return ResponseEntity.internalServerError()
                        .body("Resume text could not be saved in the database.");
            }

            System.out.println("Resume text saved successfully.");
            System.out.println("Saved text length: "
                    + savedProfile.getResumeText().length());

            // 7. Extract and save technical skills
            StudentProfile profile =
                    resumeSkillExtractionService
                            .extractAndSaveSkills(studentId);

            String skills = profile.getResumeSkills();

            System.out.println("Extracted resume skills: " + skills);
            System.out.println("===================================");

            // 8. Return success response
            if (skills == null || skills.trim().isEmpty()) {

                return ResponseEntity.ok(
                        "Resume uploaded and text extracted successfully, "
                        + "but no known technical skills were detected."
                );
            }

            return ResponseEntity.ok(
                    "Resume uploaded successfully. "
                    + "Text extracted and saved. "
                    + "Detected skills: " + skills
            );

        } catch (IOException e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload or read the resume: "
                            + e.getMessage());

        } catch (RuntimeException e) {

            e.printStackTrace();

            return ResponseEntity
                    .badRequest()
                    .body("Resume upload failed: " + e.getMessage());
        }
    }
}
