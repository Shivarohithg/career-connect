package com.careerconnect.backend.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.careerconnect.backend.model.Job;
import com.careerconnect.backend.model.JobRole;
import com.careerconnect.backend.model.Student;
import com.careerconnect.backend.model.StudentProfile;
import com.careerconnect.backend.repository.JobRepository;
import com.careerconnect.backend.repository.JobRoleRepository;
import com.careerconnect.backend.repository.StudentProfileRepository;
import com.careerconnect.backend.repository.StudentRepository;

@Service
public class AIJobRecommendationService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobRoleRepository jobRoleRepository;

    public List<JobRecommendation> recommendJobs(int studentId) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        StudentProfile profile = studentProfileRepository.findByStudent(student)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        Set<String> studentSkills = normalizeSkills(profile.getSkills());
        studentSkills.addAll(normalizeSkills(profile.getResumeSkills()));

        List<Job> jobs = jobRepository.findAll();
        List<JobRole> roles = jobRoleRepository.findAll();
        List<JobRecommendation> recommendations = new ArrayList<>();

        for (Job job : jobs) {

            JobRole matchedRole = findMatchingRole(job, roles);

            if (matchedRole == null) {
                continue;
            }

            List<String> requiredSkills =
                    parseSkills(matchedRole.getRequiredSkills());

            List<String> matchedSkills = new ArrayList<>();
            List<String> missingSkills = new ArrayList<>();

            int totalWeight = 0;
            int matchedWeight = 0;

            for (String requiredSkill : requiredSkills) {

                int weight = getSkillWeight(
                        matchedRole.getRoleName(),
                        requiredSkill);

                totalWeight += weight;

                if (studentHasSkill(studentSkills, requiredSkill)) {
                    matchedSkills.add(requiredSkill);
                    matchedWeight += weight;
                } else {
                    missingSkills.add(requiredSkill);
                }
            }

            int matchPercentage = 0;

            if (totalWeight > 0) {
                matchPercentage = Math.round(
                        matchedWeight * 100f / totalWeight);
            }

            if (matchPercentage <= 0) {
                continue;
            }

            String explanation = generateExplanation(
                    matchPercentage,
                    matchedSkills,
                    missingSkills,
                    matchedRole.getRoleName());

            recommendations.add(
                    new JobRecommendation(
                            job,
                            matchedRole.getRoleName(),
                            matchPercentage,
                            matchedSkills,
                            missingSkills,
                            explanation));
        }

        recommendations.sort(
                Comparator.comparingInt(
                        JobRecommendation::getMatchPercentage)
                        .reversed());

        return recommendations;
    }

    private JobRole findMatchingRole(
            Job job,
            List<JobRole> roles) {

        if (job.getTitle() == null) {
            return null;
        }

        String jobTitle = job.getTitle().toLowerCase();
        JobRole bestRole = null;
        int bestScore = 0;

        for (JobRole role : roles) {

            if (role.getRoleName() == null) {
                continue;
            }

            String roleName = role.getRoleName().toLowerCase();
            String[] words = roleName.split(" ");
            int score = 0;

            for (String word : words) {

                if (word.length() < 3 ||
                        word.equals("developer") ||
                        word.equals("engineer")) {
                    continue;
                }

                if (jobTitle.contains(word)) {
                    score++;
                }
            }

            if (score > bestScore) {
                bestScore = score;
                bestRole = role;
            }
        }

        return bestRole;
    }

    private int getSkillWeight(
            String roleName,
            String skill) {

        String role = roleName.toLowerCase();
        String normalizedSkill = normalizeSkill(skill);

        if (role.contains("java backend")) {
            if (normalizedSkill.equals("java") ||
                    normalizedSkill.equals("spring boot") ||
                    normalizedSkill.equals("sql")) {
                return 3;
            }
            return 1;
        }

        if (role.contains("full stack")) {
            if (normalizedSkill.equals("javascript") ||
                    normalizedSkill.equals("react") ||
                    normalizedSkill.equals("node.js")) {
                return 3;
            }
            return 1;
        }

        if (role.contains("frontend")) {
            if (normalizedSkill.equals("javascript") ||
                    normalizedSkill.equals("react")) {
                return 3;
            }
            return 1;
        }

        if (role.contains("python")) {
            if (normalizedSkill.equals("python") ||
                    normalizedSkill.equals("sql")) {
                return 3;
            }
            return 1;
        }

        if (role.contains("software engineer")) {
            if (normalizedSkill.equals("java") ||
                    normalizedSkill.equals("dsa") ||
                    normalizedSkill.equals("oop")) {
                return 3;
            }
            return 1;
        }

        if (role.contains("data analyst")) {
            if (normalizedSkill.equals("python") ||
                    normalizedSkill.equals("sql") ||
                    normalizedSkill.equals("statistics")) {
                return 3;
            }
            return 1;
        }

        return 1;
    }

    private String generateExplanation(
            int matchPercentage,
            List<String> matchedSkills,
            List<String> missingSkills,
            String roleName) {

        StringBuilder explanation = new StringBuilder();

        if (matchPercentage >= 80) {
            explanation.append(
                    "Excellent match for the "
                            + roleName
                            + " career direction.");
        } else if (matchPercentage >= 60) {
            explanation.append(
                    "Strong potential match for the "
                            + roleName
                            + " career direction.");
        } else if (matchPercentage >= 40) {
            explanation.append(
                    "Good potential match for the "
                            + roleName
                            + " career direction.");
        } else {
            explanation.append(
                    "This job can help you build "
                            + "skills toward the "
                            + roleName
                            + " career direction.");
        }

        if (!matchedSkills.isEmpty()) {
            explanation.append(" Your profile matches: ");
            explanation.append(String.join(", ", matchedSkills));
        }

        if (!missingSkills.isEmpty()) {
            explanation.append(". Focus on improving ");
            explanation.append(missingSkills.get(0));
            explanation.append(
                    " to become a stronger candidate.");
        }

        return explanation.toString();
    }

    private Set<String> normalizeSkills(String skills) {

        Set<String> result = new HashSet<>();

        if (skills == null || skills.trim().isEmpty()) {
            return result;
        }

        String[] values = skills.split(",");

        for (String value : values) {

            String skill = normalizeSkill(value);

            if (!skill.isEmpty()) {
                result.add(skill);
            }
        }

        return result;
    }

    private List<String> parseSkills(String skills) {

        List<String> result = new ArrayList<>();

        if (skills == null || skills.trim().isEmpty()) {
            return result;
        }

        String[] values = skills.split(",");

        for (String value : values) {

            String skill = value.trim();

            if (!skill.isEmpty()) {
                result.add(skill);
            }
        }

        return result;
    }

    private boolean studentHasSkill(
            Set<String> studentSkills,
            String requiredSkill) {

        String required = normalizeSkill(requiredSkill);

        if (studentSkills.contains(required)) {
            return true;
        }

        if (required.equals("spring") &&
                studentSkills.contains("spring boot")) {
            return true;
        }

        if (required.equals("spring boot") &&
                studentSkills.contains("spring")) {
            return true;
        }

        if (required.equals("javascript") &&
                studentSkills.contains("js")) {
            return true;
        }

        if (required.equals("react") &&
                studentSkills.contains("reactjs")) {
            return true;
        }

        if (required.equals("rest apis") &&
                studentSkills.contains("rest")) {
            return true;
        }

        if (required.equals("node.js") &&
                studentSkills.contains("nodejs")) {
            return true;
        }

        return false;
    }

    private String normalizeSkill(String skill) {

        return skill
                .trim()
                .toLowerCase()
                .replaceAll("\\s+", " ");
    }

    public static class JobRecommendation {

        private Job job;
        private String matchedRole;
        private int matchPercentage;
        private List<String> matchedSkills;
        private List<String> missingSkills;
        private String explanation;

        public JobRecommendation(
                Job job,
                String matchedRole,
                int matchPercentage,
                List<String> matchedSkills,
                List<String> missingSkills,
                String explanation) {

            this.job = job;
            this.matchedRole = matchedRole;
            this.matchPercentage = matchPercentage;
            this.matchedSkills = matchedSkills;
            this.missingSkills = missingSkills;
            this.explanation = explanation;
        }

        public Job getJob() {
            return job;
        }

        public String getMatchedRole() {
            return matchedRole;
        }

        public int getMatchPercentage() {
            return matchPercentage;
        }

        public List<String> getMatchedSkills() {
            return matchedSkills;
        }

        public List<String> getMissingSkills() {
            return missingSkills;
        }

        public String getExplanation() {
            return explanation;
        }
    }
}