
package com.careerconnect.backend.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.careerconnect.backend.dto.EligibilityDTO;
import com.careerconnect.backend.model.Job;
import com.careerconnect.backend.model.Student;
import com.careerconnect.backend.model.StudentProfile;
import com.careerconnect.backend.repository.JobRepository;
import com.careerconnect.backend.repository.StudentProfileRepository;
import com.careerconnect.backend.repository.StudentRepository;

@Service
public class EligibilityService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    public EligibilityDTO checkEligibility(int studentId, int jobId) {

        // 1. Fetch student
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() ->
                        new RuntimeException("Student not found"));

        // 2. Fetch job
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() ->
                        new RuntimeException("Job not found"));

        // 3. Fetch student profile
        Optional<StudentProfile> profileOptional =
                studentProfileRepository.findByStudent(student);

        StudentProfile profile = profileOptional.orElse(null);

        // 4. Check CGPA eligibility
        boolean cgpaEligible =
                student.getCgpa() >= job.getMinCgpa();

        // 5. Check branch eligibility
        List<String> eligibleBranches =
                splitValues(job.getEligibleBranches());

        boolean branchEligible;

        if (eligibleBranches.isEmpty()) {
            // Empty branch requirement means all branches are accepted
            branchEligible = true;
        } else {
            branchEligible = eligibleBranches.stream()
                    .anyMatch(branch ->
                            normalize(branch).equals(
                                    normalize(student.getBranch())));
        }

        // 6. Collect student skills from profile and resume
        Set<String> studentSkills = new LinkedHashSet<>();

        if (profile != null) {

            studentSkills.addAll(
                    splitValues(profile.getSkills())
            );

            studentSkills.addAll(
                    splitValues(profile.getResumeSkills())
            );
        }

        Set<String> normalizedStudentSkills = studentSkills.stream()
                .map(this::normalize)
                .collect(Collectors.toSet());

        // 7. Get job-required skills
        List<String> requiredSkills =
                splitValues(job.getRequiredSkills());

        List<String> matchedSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();

        for (String skill : requiredSkills) {

            if (normalizedStudentSkills.contains(normalize(skill))) {
                matchedSkills.add(skill);
            } else {
                missingSkills.add(skill);
            }
        }

        // 8. Calculate skill match percentage
        double skillMatchPercentage;

        if (requiredSkills.isEmpty()) {
            skillMatchPercentage = 100.0;
        } else {
            skillMatchPercentage =
                    (matchedSkills.size() * 100.0)
                            / requiredSkills.size();

            skillMatchPercentage =
                    Math.round(skillMatchPercentage * 100.0) / 100.0;
        }

        // 9. Overall academic eligibility
        // Missing skills do not automatically make a student
        // academically ineligible.
        boolean eligible = cgpaEligible && branchEligible;

        // 10. Prepare result message
        String message;

        if (eligible) {
            message = "You meet the academic eligibility requirements.";
        } else if (!cgpaEligible && !branchEligible) {
            message = "You do not meet the CGPA and branch requirements.";
        } else if (!cgpaEligible) {
            message = "Your CGPA is below the minimum required CGPA.";
        } else {
            message = "Your branch is not eligible for this job.";
        }

        // 11. Return eligibility result
        return new EligibilityDTO(
                studentId,
                jobId,
                eligible,
                cgpaEligible,
                branchEligible,
                student.getCgpa(),
                job.getMinCgpa(),
                student.getBranch(),
                eligibleBranches,
                matchedSkills,
                missingSkills,
                skillMatchPercentage,
                message
        );
    }

    // Split comma-separated, semicolon-separated or newline-separated values
    private List<String> splitValues(String values) {

        if (values == null || values.isBlank()) {
            return new ArrayList<>();
        }

        return Arrays.stream(values.split("[,;\\n\\r]+"))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toList());
    }

    // Normalize values for case-insensitive comparison
    private String normalize(String value) {

        if (value == null) {
            return "";
        }

        return value.trim().toLowerCase(Locale.ROOT);
    }
}

