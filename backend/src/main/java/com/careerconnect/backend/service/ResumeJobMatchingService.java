
package com.careerconnect.backend.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.careerconnect.backend.model.Job;
import com.careerconnect.backend.model.JobRole;
import com.careerconnect.backend.model.Student;
import com.careerconnect.backend.model.StudentProfile;
import com.careerconnect.backend.repository.JobRepository;
import com.careerconnect.backend.repository.JobRoleRepository;
import com.careerconnect.backend.repository.StudentRepository;
import com.careerconnect.backend.repository.StudentProfileRepository;

@Service
public class ResumeJobMatchingService {

    private final JobRepository jobRepository;
    private final JobRoleRepository jobRoleRepository;
    private final StudentRepository studentRepository;
    private final StudentProfileRepository profileRepository;
    private final ResumeSkillExtractionService skillExtractionService;
    private final OllamaService ollamaService;

    public ResumeJobMatchingService(
            JobRepository jobRepository,
            JobRoleRepository jobRoleRepository,
            StudentRepository studentRepository,
            StudentProfileRepository profileRepository,
            ResumeSkillExtractionService skillExtractionService,
            OllamaService ollamaService) {

        this.jobRepository = jobRepository;
        this.jobRoleRepository = jobRoleRepository;
        this.studentRepository = studentRepository;
        this.profileRepository = profileRepository;
        this.skillExtractionService = skillExtractionService;
        this.ollamaService = ollamaService;
    }

    public Map<String, Object> analyze(int studentId, int jobId) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Student not found"));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Job not found"));

        StudentProfile profile = profileRepository.findByStudent(student)
                .orElseThrow(() ->
                        new IllegalArgumentException("Student profile not found"));

        String resumeText = profile.getResumeText();

        if (resumeText == null || resumeText.isBlank()) {
            throw new IllegalArgumentException(
                    "Please upload a resume before checking job compatibility");
        }

        // Match the selected job title to a stored job role.
        // Do not guess requirements when no matching role exists.
        JobRole matchedRole = jobRoleRepository.findAll().stream()
                .filter(role -> role.getRoleName() != null)
                .filter(role -> role.getRoleName()
                        .trim()
                        .equalsIgnoreCase(job.getTitle().trim()))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No matching job role found for: " + job.getTitle()));

        String requiredSkillsText = matchedRole.getRequiredSkills();

        if (requiredSkillsText == null || requiredSkillsText.isBlank()) {
            throw new IllegalArgumentException(
                    "No required skills configured for this job role");
        }

        List<String> requiredSkills = Arrays.stream(
                        requiredSkillsText.split(","))
                .map(String::trim)
                .filter(skill -> !skill.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        if (requiredSkills.isEmpty()) {
            throw new IllegalArgumentException(
                    "No valid required skills found");
        }

        // Resume-only evidence: do not use profile.getSkills().
        String extractedSkills =
                skillExtractionService.extractSkills(resumeText);

        Set<String> detectedSkills = Arrays.stream(
                        extractedSkills.split(","))
                .map(String::trim)
                .filter(skill -> !skill.isEmpty())
                .map(String::toLowerCase)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<String> matchedSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();

        for (String requiredSkill : requiredSkills) {
            if (detectedSkills.contains(requiredSkill.toLowerCase())) {
                matchedSkills.add(requiredSkill);
            } else {
                missingSkills.add(requiredSkill);
            }
        }

        int matchPercentage = (int) Math.round(
                (matchedSkills.size() * 100.0) / requiredSkills.size());

        String fallback = buildFallback(missingSkills);

        String prompt = """
                You are a resume improvement assistant.

                Target job: %s
                Required skills: %s
                Skills found in uploaded resume: %s
                Missing required skills: %s

                Provide 3 short, practical recommendations
                to improve the resume for this job.

                Do not invent experience, projects or skills.
                Do not recommend adding a skill unless the
                candidate actually possesses it.
                """
                .formatted(
                        job.getTitle(),
                        requiredSkills,
                        matchedSkills,
                        missingSkills);

        String suggestions = ollamaService.generateJobInsight(
                prompt, fallback);

        if (suggestions == null || suggestions.isBlank()) {
            suggestions = fallback;
        }

        Map<String, Object> result = new LinkedHashMap<>();

        result.put("studentId", studentId);
        result.put("jobId", jobId);
        result.put("jobTitle", job.getTitle());
        result.put("company", job.getCompany());
        result.put("matchedRole", matchedRole.getRoleName());
        result.put("matchPercentage", matchPercentage);
        result.put("requiredSkills", requiredSkills);
        result.put("matchedSkills", matchedSkills);
        result.put("missingSkills", missingSkills);
        result.put("improvementSuggestions", suggestions);

        return result;
    }

    private String buildFallback(List<String> missingSkills) {

        if (missingSkills.isEmpty()) {
            return "Your resume demonstrates all configured required skills. "
                    + "Highlight relevant projects and measurable achievements.";
        }

        return "Review these missing skills: "
                + String.join(", ", missingSkills)
                + ". Learn or strengthen them where relevant, "
                + "and add only skills you can demonstrate. "
                + "Highlight relevant projects and measurable results.";
    }
}

