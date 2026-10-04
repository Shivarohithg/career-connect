package com.careerconnect.backend.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

    public CareerAnalysis getAnalysis(int studentId) {

        Student student =
                studentRepository.findById(studentId)
                        .orElseThrow(() ->
                                new RuntimeException("Student not found"));

        return careerAnalysisRepository.findByStudent(student)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Career analysis not found"));
    }

    public CareerAnalysis createAnalysis(int studentId) {

        Student student =
                studentRepository.findById(studentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Student not found"));

        StudentProfile profile =
                studentProfileRepository.findByStudent(student)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Profile not found"));

        CareerAnalysis analysis =
                careerAnalysisRepository
                        .findByStudent(student)
                        .orElse(new CareerAnalysis());

        Set<String> studentSkills =
                normalizeSkills(profile.getSkills());

        Set<String> resumeSkills =
                normalizeSkills(profile.getResumeSkills());

        studentSkills.addAll(resumeSkills);

        List<JobRole> jobRoles =
                jobRoleRepository.findAll();

        List<RoleMatch> roleMatches =
                new ArrayList<>();

        for (JobRole jobRole : jobRoles) {

            List<String> requiredSkills =
                    parseRequiredSkills(
                            jobRole.getRequiredSkills());

            int totalWeight = 0;
            int matchedWeight = 0;

            List<String> matchedSkills =
                    new ArrayList<>();

            List<String> missingSkills =
                    new ArrayList<>();

            for (String skill : requiredSkills) {

                int weight =
                        getSkillWeight(
                                jobRole.getRoleName(),
                                skill);

                totalWeight += weight;

                if (studentHasSkill(
                        studentSkills,
                        skill)) {

                    matchedWeight += weight;
                    matchedSkills.add(skill);

                } else {

                    missingSkills.add(skill);
                }
            }

            int matchPercentage = 0;

            if (totalWeight > 0) {

                matchPercentage =
                        Math.round(
                                (matchedWeight * 100f)
                                        / totalWeight);
            }

            roleMatches.add(
                    new RoleMatch(
                            jobRole.getRoleName(),
                            matchPercentage,
                            matchedSkills,
                            missingSkills
                    )
            );
        }

        roleMatches.sort(
                Comparator.comparingInt(
                        RoleMatch::getMatchPercentage
                ).reversed()
        );

        List<String> recommendedRoles =
                new ArrayList<>();

        int roleLimit =
                Math.min(roleMatches.size(), 5);

        for (int i = 0; i < roleLimit; i++) {

            RoleMatch match =
                    roleMatches.get(i);

            recommendedRoles.add(
                    match.getRoleName()
                            + " ("
                            + match.getMatchPercentage()
                            + "% Match)"
            );
        }

        RoleMatch bestRole =
                roleMatches.isEmpty()
                        ? null
                        : roleMatches.get(0);

        List<String> skillGaps =
                new ArrayList<>();

        if (bestRole != null) {

            skillGaps.addAll(
                    bestRole.getMissingSkills()
            );
        }

        Set<String> recommendedSkillSet =
                new HashSet<>();

        int topRoleLimit =
                Math.min(roleMatches.size(), 3);

        for (int i = 0; i < topRoleLimit; i++) {

            recommendedSkillSet.addAll(
                    roleMatches
                            .get(i)
                            .getMatchedSkills()
            );

            recommendedSkillSet.addAll(
                    roleMatches
                            .get(i)
                            .getMissingSkills()
            );
        }

        List<String> recommendedSkills =
                new ArrayList<>(
                        recommendedSkillSet
                );

        analysis.setStudent(student);

        analysis.setRecommendedRoles(
                String.join(
                        ", ",
                        recommendedRoles
                )
        );

        analysis.setRecommendedSkills(
                String.join(
                        ", ",
                        recommendedSkills
                )
        );

        analysis.setSkillGaps(
                String.join(
                        ", ",
                        skillGaps
                )
        );

        return careerAnalysisRepository.save(
                analysis
        );
    }

    private Set<String> normalizeSkills(
            String skills) {

        Set<String> normalized =
                new HashSet<>();

        if (skills == null ||
                skills.trim().isEmpty()) {

            return normalized;
        }

        String[] values =
                skills.split(",");

        for (String value : values) {

            String skill =
                    normalizeSkill(value);

            if (!skill.isEmpty()) {
                normalized.add(skill);
            }
        }

        return normalized;
    }

    private List<String> parseRequiredSkills(
            String requiredSkills) {

        List<String> result =
                new ArrayList<>();

        if (requiredSkills == null ||
                requiredSkills.trim().isEmpty()) {

            return result;
        }

        String[] values =
                requiredSkills.split(",");

        for (String value : values) {

            String skill =
                    value.trim();

            if (!skill.isEmpty()) {
                result.add(skill);
            }
        }

        return result;
    }

    private boolean studentHasSkill(
            Set<String> studentSkills,
            String requiredSkill) {

        String normalizedRequired =
                normalizeSkill(requiredSkill);

        if (studentSkills.contains(
                normalizedRequired)) {

            return true;
        }

        Map<String, String> aliases =
                new HashMap<>();

        aliases.put(
                "spring",
                "spring boot"
        );

        aliases.put(
                "springboot",
                "spring boot"
        );

        aliases.put(
                "js",
                "javascript"
        );

        aliases.put(
                "reactjs",
                "react"
        );

        aliases.put(
                "rest",
                "rest apis"
        );

        String canonical =
                aliases.getOrDefault(
                        normalizedRequired,
                        normalizedRequired
                );

        for (String studentSkill :
                studentSkills) {

            String studentCanonical =
                    aliases.getOrDefault(
                            studentSkill,
                            studentSkill
                    );

            if (studentCanonical.equals(
                    canonical)) {

                return true;
            }
        }

        return false;
    }

    private String normalizeSkill(
            String skill) {

        return skill
                .trim()
                .toLowerCase()
                .replaceAll(
                        "\\s+",
                        " "
                );
    }

    private int getSkillWeight(
            String roleName,
            String skill) {

        String role =
                roleName.toLowerCase();

        String normalized =
                normalizeSkill(skill);

        if (role.contains("java backend")) {

            if (normalized.equals("java") ||
                    normalized.equals("spring boot") ||
                    normalized.equals("sql")) {

                return 3;
            }

            return 1;
        }

        if (role.contains("full stack")) {

            if (normalized.equals("javascript") ||
                    normalized.equals("react") ||
                    normalized.equals("node.js")) {

                return 3;
            }

            return 1;
        }

        if (role.contains("frontend")) {

            if (normalized.equals("javascript") ||
                    normalized.equals("react")) {

                return 3;
            }

            return 1;
        }

        if (role.contains("python")) {

            if (normalized.equals("python") ||
                    normalized.equals("sql")) {

                return 3;
            }

            return 1;
        }

        if (role.contains("software engineer")) {

            if (normalized.equals("java") ||
                    normalized.equals("dsa") ||
                    normalized.equals("oop")) {

                return 3;
            }

            return 1;
        }

        if (role.contains("data analyst")) {

            if (normalized.equals("python") ||
                    normalized.equals("sql") ||
                    normalized.equals("statistics")) {

                return 3;
            }

            return 1;
        }

        return 1;
    }

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
            this.matchPercentage =
                    matchPercentage;
            this.matchedSkills =
                    matchedSkills;
            this.missingSkills =
                    missingSkills;
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