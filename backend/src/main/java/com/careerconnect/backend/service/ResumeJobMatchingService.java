
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

    // ==========================================
    // DAY 69: Match resume with one selected job
    // ==========================================

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

        JobRole matchedRole = findMatchingRole(job.getTitle());

        if (matchedRole == null) {
            throw new IllegalArgumentException(
                    "No matching job role found for: " + job.getTitle());
        }

        List<String> requiredSkills =
                parseRequiredSkills(matchedRole.getRequiredSkills());

        if (requiredSkills.isEmpty()) {
            throw new IllegalArgumentException(
                    "No required skills configured for this job role");
        }

        Set<String> detectedSkills = extractDetectedSkills(resumeText);

        List<String> matchedSkills =
                getMatchedSkills(requiredSkills, detectedSkills);

        List<String> missingSkills =
                getMissingSkills(requiredSkills, detectedSkills);

        int matchPercentage = calculatePercentage(
                matchedSkills.size(), requiredSkills.size());

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

    // ==========================================
    // DAY 70: Match resume against ALL jobs
    // ==========================================

    public List<Map<String, Object>> analyzeAll(int studentId) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Student not found"));

        StudentProfile profile = profileRepository.findByStudent(student)
                .orElseThrow(() ->
                        new IllegalArgumentException("Student profile not found"));

        String resumeText = profile.getResumeText();

        if (resumeText == null || resumeText.isBlank()) {
            throw new IllegalArgumentException(
                    "Please upload a resume before checking job compatibility");
        }

        // Extract resume skills only once for all jobs.
        Set<String> detectedSkills = extractDetectedSkills(resumeText);

        // Fetch roles once instead of querying for every job.
        Map<String, JobRole> roleMap = new LinkedHashMap<>();

        for (JobRole role : jobRoleRepository.findAll()) {

            if (role.getRoleName() != null) {
                roleMap.putIfAbsent(
                        normalize(role.getRoleName()),
                        role);
            }
        }

        // Fetch all available jobs once.
        List<Job> jobs = jobRepository.findAll();

        List<Map<String, Object>> results = new ArrayList<>();

        for (Job job : jobs) {

            Map<String, Object> result = new LinkedHashMap<>();

            result.put("jobId", job.getId());
            result.put("jobTitle", job.getTitle());
            result.put("company", job.getCompany());
            result.put("location", job.getLocation());
            result.put("salary", job.getSalary());

            JobRole matchedRole = roleMap.get(
                    normalize(job.getTitle()));

            // Do not invent requirements for unmatched job titles.
            if (matchedRole == null) {

                result.put("matchAvailable", false);
                result.put("matchPercentage", null);
                result.put("matchedSkills", List.of());
                result.put("missingSkills", List.of());
                result.put("requiredSkills", List.of());
                result.put("message",
                        "No matching job role is configured.");

                results.add(result);
                continue;
            }

            List<String> requiredSkills =
                    parseRequiredSkills(matchedRole.getRequiredSkills());

            if (requiredSkills.isEmpty()) {

                result.put("matchAvailable", false);
                result.put("matchPercentage", null);
                result.put("matchedSkills", List.of());
                result.put("missingSkills", List.of());
                result.put("requiredSkills", List.of());
                result.put("message",
                        "Required skills are not configured.");

                results.add(result);
                continue;
            }

            List<String> matchedSkills =
                    getMatchedSkills(requiredSkills, detectedSkills);

            List<String> missingSkills =
                    getMissingSkills(requiredSkills, detectedSkills);

            int percentage = calculatePercentage(
                    matchedSkills.size(), requiredSkills.size());

            result.put("matchAvailable", true);
            result.put("matchedRole", matchedRole.getRoleName());
            result.put("matchPercentage", percentage);
            result.put("requiredSkills", requiredSkills);
            result.put("matchedSkills", matchedSkills);
            result.put("missingSkills", missingSkills);

            results.add(result);
        }

        return results;
    }

    // ==========================================
    // Common helper methods
    // ==========================================

    private JobRole findMatchingRole(String jobTitle) {

        return jobRoleRepository.findAll().stream()
                .filter(role -> role.getRoleName() != null)
                .filter(role -> normalize(role.getRoleName())
                        .equals(normalize(jobTitle)))
                .findFirst()
                .orElse(null);
    }

    private Set<String> extractDetectedSkills(String resumeText) {

        String extractedSkills =
                skillExtractionService.extractSkills(resumeText);

        if (extractedSkills == null || extractedSkills.isBlank()) {
            return new LinkedHashSet<>();
        }

        return Arrays.stream(extractedSkills.split(","))
                .map(String::trim)
                .filter(skill -> !skill.isEmpty())
                .map(String::toLowerCase)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private List<String> parseRequiredSkills(String skillsText) {

        if (skillsText == null || skillsText.isBlank()) {
            return new ArrayList<>();
        }

        return Arrays.stream(skillsText.split(","))
                .map(String::trim)
                .filter(skill -> !skill.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    private List<String> getMatchedSkills(
            List<String> requiredSkills,
            Set<String> detectedSkills) {

        List<String> matched = new ArrayList<>();

        for (String skill : requiredSkills) {
            if (detectedSkills.contains(skill.toLowerCase())) {
                matched.add(skill);
            }
        }

        return matched;
    }

    private List<String> getMissingSkills(
            List<String> requiredSkills,
            Set<String> detectedSkills) {

        List<String> missing = new ArrayList<>();

        for (String skill : requiredSkills) {
            if (!detectedSkills.contains(skill.toLowerCase())) {
                missing.add(skill);
            }
        }

        return missing;
    }

    private int calculatePercentage(int matched, int total) {

        if (total == 0) {
            return 0;
        }

        return (int) Math.round((matched * 100.0) / total);
    }

    private String normalize(String value) {

        return value == null
                ? ""
                : value.trim().toLowerCase();
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


