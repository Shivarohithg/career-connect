
package com.careerconnect.backend.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.careerconnect.backend.model.CareerAnalysis;
import com.careerconnect.backend.model.JobRole;
import com.careerconnect.backend.model.Student;
import com.careerconnect.backend.model.StudentProfile;
import com.careerconnect.backend.repository.CareerAnalysisRepository;
import com.careerconnect.backend.repository.JobRoleRepository;
import com.careerconnect.backend.repository.StudentProfileRepository;
import com.careerconnect.backend.repository.StudentRepository;

@Service
public class CareerAnalysisService {

    @Autowired
    private CareerAnalysisRepository careerAnalysisRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private JobRoleRepository jobRoleRepository;

    // Get existing career analysis
    public CareerAnalysis getAnalysis(int studentId) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() ->
                        new RuntimeException("Student not found"));

        return careerAnalysisRepository.findByStudent(student)
                .orElseThrow(() ->
                        new RuntimeException("Career analysis not found"));
    }

    // Create or regenerate career analysis
    public CareerAnalysis createAnalysis(int studentId) {

        System.out.println("\n========== CAREER ANALYSIS START ==========");
        System.out.println("Student ID: " + studentId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() ->
                        new RuntimeException("Student not found"));

        StudentProfile profile = studentProfileRepository
                .findByStudent(student)
                .orElseThrow(() ->
                        new RuntimeException("Profile not found"));

        CareerAnalysis analysis = careerAnalysisRepository
                .findByStudent(student)
                .orElse(new CareerAnalysis());

        // Get skills from student profile
        Set<String> studentSkills = normalizeSkills(
                profile.getSkills());

        // Get skills extracted from resume
        Set<String> resumeSkills = normalizeSkills(
                profile.getResumeSkills());

        // Combine both skill sources
        studentSkills.addAll(resumeSkills);

        System.out.println("Profile Skills: " + profile.getSkills());
        System.out.println("Resume Skills: " + profile.getResumeSkills());
        System.out.println("Normalized Student Skills: " + studentSkills);

        // Get all job roles
        List<JobRole> jobRoles = jobRoleRepository.findAll();

        System.out.println("Total Job Roles Found: " + jobRoles.size());

        if (jobRoles.isEmpty()) {
            System.out.println("WARNING: No job roles found in database.");
        }

        List<RoleMatch> roleMatches = new ArrayList<>();

        // Calculate match for every role
        for (JobRole jobRole : jobRoles) {

            String roleName = jobRole.getRoleName();

            List<String> requiredSkills = parseRequiredSkills(
                    jobRole.getRequiredSkills());

            System.out.println("\n----------------------------------------");
            System.out.println("Role: " + roleName);
            System.out.println("Database Required Skills: "
                    + jobRole.getRequiredSkills());
            System.out.println("Parsed Required Skills: "
                    + requiredSkills);

            int totalWeight = 0;
            int matchedWeight = 0;

            List<String> matchedSkills = new ArrayList<>();
            List<String> missingSkills = new ArrayList<>();

            // Compare student skills with role requirements
            for (String skill : requiredSkills) {

                int weight = getSkillWeight(roleName, skill);

                totalWeight += weight;

                boolean matched = studentHasSkill(
                        studentSkills, skill);

                System.out.println(
                        "Required Skill: " + skill
                        + " | Normalized: " + normalizeSkill(skill)
                        + " | Matched: " + matched
                        + " | Weight: " + weight);

                if (matched) {

                    matchedWeight += weight;
                    matchedSkills.add(skill);

                } else {

                    missingSkills.add(skill);
                }
            }

            // Calculate percentage
            int matchPercentage = 0;

            if (totalWeight > 0) {

                matchPercentage = Math.round(
                        (matchedWeight * 100f) / totalWeight);
            }

            System.out.println("Matched Skills: " + matchedSkills);
            System.out.println("Missing Skills: " + missingSkills);
            System.out.println("Matched Weight: " + matchedWeight);
            System.out.println("Total Weight: " + totalWeight);
            System.out.println("Match Percentage: "
                    + matchPercentage + "%");

            roleMatches.add(
                    new RoleMatch(
                            roleName,
                            matchPercentage,
                            matchedSkills,
                            missingSkills));
        }

        // Sort roles by highest match percentage
        roleMatches.sort(
                Comparator.comparingInt(
                        RoleMatch::getMatchPercentage)
                        .reversed());

        // Store top 5 recommended roles
        List<String> recommendedRoles = new ArrayList<>();

        int roleLimit = Math.min(roleMatches.size(), 5);

        for (int i = 0; i < roleLimit; i++) {

            RoleMatch match = roleMatches.get(i);

            recommendedRoles.add(
                    match.getRoleName()
                            + " ("
                            + match.getMatchPercentage()
                            + "% Match)");
        }

        // Get best matching role
        RoleMatch bestRole = roleMatches.isEmpty()
                ? null
                : roleMatches.get(0);

        // Skill gaps for the best role
        List<String> skillGaps = new ArrayList<>();

        if (bestRole != null) {

            skillGaps.addAll(
                    bestRole.getMissingSkills());
        }

        // Recommended skills from top 3 roles
        Set<String> recommendedSkillSet = new HashSet<>();

        int topRoleLimit = Math.min(roleMatches.size(), 3);

        for (int i = 0; i < topRoleLimit; i++) {

            recommendedSkillSet.addAll(
                    roleMatches.get(i).getMatchedSkills());

            recommendedSkillSet.addAll(
                    roleMatches.get(i).getMissingSkills());
        }

        List<String> recommendedSkills =
                new ArrayList<>(recommendedSkillSet);

        // Update analysis entity
        analysis.setStudent(student);

        analysis.setRecommendedRoles(
                String.join(", ", recommendedRoles));

        analysis.setRecommendedSkills(
                String.join(", ", recommendedSkills));

        analysis.setSkillGaps(
                String.join(", ", skillGaps));

        // Save analysis
        CareerAnalysis savedAnalysis =
                careerAnalysisRepository.save(analysis);

        System.out.println("\n========== CAREER ANALYSIS RESULT ==========");
        System.out.println("Recommended Roles: "
                + savedAnalysis.getRecommendedRoles());
        System.out.println("Recommended Skills: "
                + savedAnalysis.getRecommendedSkills());
        System.out.println("Skill Gaps: "
                + savedAnalysis.getSkillGaps());
        System.out.println("Career Analysis Saved Successfully");
        System.out.println("==========================================\n");

        return savedAnalysis;
    }

    // Normalize and split skills
    private Set<String> normalizeSkills(String skills) {

        Set<String> normalized = new HashSet<>();

        if (skills == null || skills.trim().isEmpty()) {
            return normalized;
        }

        // Supports comma and semicolon separated skills
        String[] values = skills.split("[,;]");

        for (String value : values) {

            String skill = normalizeSkill(value);

            if (!skill.isEmpty()) {
                normalized.add(skill);
            }
        }

        return normalized;
    }

    // Parse skills required by a job role
    private List<String> parseRequiredSkills(String requiredSkills) {

        List<String> result = new ArrayList<>();

        if (requiredSkills == null
                || requiredSkills.trim().isEmpty()) {

            return result;
        }

        // Supports comma and semicolon separated requirements
        String[] values = requiredSkills.split("[,;]");

        for (String value : values) {

            String skill = normalizeSkill(value);

            if (!skill.isEmpty()) {
                result.add(skill);
            }
        }

        return result;
    }

    // Check whether student has a required skill
    private boolean studentHasSkill(
            Set<String> studentSkills,
            String requiredSkill) {

        String normalizedRequired =
                normalizeSkill(requiredSkill);

        String requiredCanonical =
                canonicalSkill(normalizedRequired);

        for (String studentSkill : studentSkills) {

            String studentCanonical =
                    canonicalSkill(studentSkill);

            if (studentCanonical.equals(requiredCanonical)) {
                return true;
            }
        }

        return false;
    }

    // Convert common skill aliases to standard names
    private String canonicalSkill(String skill) {

        String normalized = normalizeSkill(skill);

        switch (normalized) {

            case "spring":
            case "springboot":
            case "spring boot":
                return "spring boot";

            case "js":
            case "javascript":
                return "javascript";

            case "reactjs":
                return "react";

            case "node":
                return "node.js";

            case "rest":
            case "rest api":
            case "restapi":
                return "rest apis";

            case "c++":
            case "cpp":
                return "c++";

            case "mysql":
                return "mysql";

            case "html5":
                return "html";

            case "css3":
                return "css";

            default:
                return normalized;
        }
    }

    // Normalize skill text
    private String normalizeSkill(String skill) {

        if (skill == null) {
            return "";
        }

        return skill.trim()
                .toLowerCase()
                .replaceAll("\\s+", " ");
    }

    // Assign weights based on role and skill
    private int getSkillWeight(
            String roleName,
            String skill) {

        String role = roleName == null
                ? ""
                : roleName.toLowerCase();

        String normalized = canonicalSkill(skill);

        if (role.contains("java backend")) {

            if (normalized.equals("java")
                    || normalized.equals("spring boot")
                    || normalized.equals("sql")) {

                return 3;
            }

            return 1;
        }

        if (role.contains("full stack")) {

            if (normalized.equals("javascript")
                    || normalized.equals("react")
                    || normalized.equals("node.js")) {

                return 3;
            }

            return 1;
        }

        if (role.contains("frontend")
                || role.contains("front end")) {

            if (normalized.equals("javascript")
                    || normalized.equals("react")) {

                return 3;
            }

            return 1;
        }

        if (role.contains("python")) {

            if (normalized.equals("python")
                    || normalized.equals("sql")) {

                return 3;
            }

            return 1;
        }

        if (role.contains("software engineer")) {

            if (normalized.equals("java")
                    || normalized.equals("dsa")
                    || normalized.equals("oop")) {

                return 3;
            }

            return 1;
        }

        if (role.contains("data analyst")) {

            if (normalized.equals("python")
                    || normalized.equals("sql")
                    || normalized.equals("statistics")) {

                return 3;
            }

            return 1;
        }

        return 1;
    }

    // Role match result
    private static class RoleMatch {

        private String roleName;
        private int matchPercentage;
        private List<String> matchedSkills;
        private List<String> missingSkills;

        public RoleMatch(
                String roleName,
                int matchPercentage,
                List<String> matchedSkills,
                List<String> missingSkills) {

            this.roleName = roleName;
            this.matchPercentage = matchPercentage;
            this.matchedSkills = matchedSkills;
            this.missingSkills = missingSkills;
        }

        public String getRoleName() {
            return roleName;
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
    }
}
