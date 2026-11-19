
package com.careerconnect.backend.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.careerconnect.backend.dto.EmployabilityScoreDTO;
import com.careerconnect.backend.dto.GitHubAnalysisDTO;
import com.careerconnect.backend.model.MockInterviewAttempt;
import com.careerconnect.backend.model.Student;
import com.careerconnect.backend.model.StudentProfile;
import com.careerconnect.backend.repository.MockInterviewAttemptRepository;
import com.careerconnect.backend.repository.StudentProfileRepository;
import com.careerconnect.backend.repository.StudentRepository;

@Service
public class EmployabilityScoreService {

    private static final Logger logger =
            LoggerFactory.getLogger(EmployabilityScoreService.class);

    private final StudentRepository studentRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final MockInterviewAttemptRepository mockInterviewAttemptRepository;
    private final GitHubAnalysisService gitHubAnalysisService;

    public EmployabilityScoreService(
            StudentRepository studentRepository,
            StudentProfileRepository studentProfileRepository,
            MockInterviewAttemptRepository mockInterviewAttemptRepository,
            GitHubAnalysisService gitHubAnalysisService) {

        this.studentRepository = studentRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.mockInterviewAttemptRepository = mockInterviewAttemptRepository;
        this.gitHubAnalysisService = gitHubAnalysisService;
    }

    public EmployabilityScoreDTO calculateScore(int studentId) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() ->
                        new RuntimeException("Student not found"));

        StudentProfile profile = studentProfileRepository
                .findByStudent(student)
                .orElseThrow(() ->
                        new RuntimeException("Student profile not found"));

        List<EmployabilityScoreDTO.ComponentScore> components =
                new ArrayList<>();

        List<String> recommendations = new ArrayList<>();

        int totalScore = 0;
        int assessedComponents = 0;

        // =========================================
        // 1. RESUME STRENGTH
        // =========================================

        Integer resumeScore = calculateResumeScore(profile);

        if (resumeScore != null) {

            totalScore += resumeScore;
            assessedComponents++;

            components.add(new EmployabilityScoreDTO.ComponentScore(
                    "Resume Strength",
                    resumeScore,
                    20,
                    "ASSESSED",
                    "Based on resume availability, extracted content, "
                            + "detected skills and section completeness."
            ));

            if (resumeScore < 12) {
                recommendations.add(
                        "Improve your resume by adding clear sections, "
                                + "technical skills and relevant details."
                );
            }

        } else {

            components.add(new EmployabilityScoreDTO.ComponentScore(
                    "Resume Strength",
                    0,
                    20,
                    "NOT_ASSESSED",
                    "Upload and process a resume to assess this component."
            ));

            recommendations.add(
                    "Upload your resume to begin resume assessment."
            );
        }

        // =========================================
        // 2. TECHNICAL SKILLS
        // =========================================

        Set<String> skills = new HashSet<>();

        addSkills(skills, profile.getSkills());
        addSkills(skills, profile.getResumeSkills());

        if (!skills.isEmpty()) {

            int skillScore = Math.min(20, skills.size() * 2);

            totalScore += skillScore;
            assessedComponents++;

            components.add(new EmployabilityScoreDTO.ComponentScore(
                    "Technical Skills",
                    skillScore,
                    20,
                    "ASSESSED",
                    "Based on " + skills.size()
                            + " distinct skills recorded in your profile "
                            + "and extracted resume skills. "
                            + "These skills are not independently verified."
            ));

            if (skillScore < 12) {
                recommendations.add(
                        "Develop more role-relevant technical skills "
                                + "and update your profile."
                );
            }

        } else {

            components.add(new EmployabilityScoreDTO.ComponentScore(
                    "Technical Skills",
                    0,
                    20,
                    "NOT_ASSESSED",
                    "No technical skills have been recorded yet."
            ));

            recommendations.add(
                    "Add your technical skills to your student profile."
            );
        }

        // =========================================
        // 3. PROJECTS
        // =========================================

        String projects = profile.getProjects();

        if (projects != null && !projects.isBlank()) {

            int projectCount = countProjects(projects);

            int projectScore = Math.min(20, projectCount * 5);

            totalScore += projectScore;
            assessedComponents++;

            components.add(new EmployabilityScoreDTO.ComponentScore(
                    "Projects",
                    projectScore,
                    20,
                    "ASSESSED",
                    "Based on " + projectCount
                            + " project entries recorded in your profile. "
                            + "Project quality has not yet been independently evaluated."
            ));

            if (projectScore < 12) {
                recommendations.add(
                        "Add more meaningful projects and describe "
                                + "your contribution and technologies used."
                );
            }

        } else {

            components.add(new EmployabilityScoreDTO.ComponentScore(
                    "Projects",
                    0,
                    20,
                    "NOT_ASSESSED",
                    "No projects have been added to your profile."
            ));

            recommendations.add(
                    "Add practical projects that demonstrate your technical skills."
            );
        }

        // =========================================
        // 4. CODING PROFICIENCY - GITHUB ANALYSIS
        // =========================================

        try {

            GitHubAnalysisDTO github =
                    gitHubAnalysisService.analyzeStudent(studentId);

            int repositoryCount = github.repositories() == null
                    ? 0 : github.repositories().size();

            int activeRepositoryCount =
                    github.activeRepositories();

            int languageCount = github.languages() == null
                    ? 0 : github.languages().size();

            int documentedRepositories = 0;

            if (github.repositories() != null) {

                documentedRepositories = (int) github.repositories()
                        .stream()
                        .filter(repo -> repo.description() != null
                                && !repo.description().isBlank())
                        .count();
            }

            // Repository portfolio: maximum 6 points
            int repositoryScore =
                    Math.min(6, repositoryCount * 2);

            // Recent activity: maximum 6 points
            int activityScore =
                    Math.min(6, activeRepositoryCount * 2);

            // Language diversity: maximum 4 points
            int languageScore =
                    Math.min(4, languageCount * 2);

            // Repository descriptions: maximum 4 points
            int documentationScore =
                    Math.min(4, documentedRepositories * 2);

            int codingScore =
                    repositoryScore
                    + activityScore
                    + languageScore
                    + documentationScore;

            totalScore += codingScore;
            assessedComponents++;

            components.add(new EmployabilityScoreDTO.ComponentScore(
                    "Coding Proficiency",
                    codingScore,
                    20,
                    "ASSESSED",
                    "Based on public GitHub evidence: "
                            + repositoryCount + " analyzed repositories, "
                            + activeRepositoryCount + " recently active repositories, "
                            + languageCount + " programming languages and "
                            + documentedRepositories
                            + " repositories with descriptions. "
                            + "This is an activity-based proxy, not a direct "
                            + "measurement of coding ability."
            ));

            if (codingScore < 12) {
                recommendations.add(
                        "Improve your GitHub portfolio by building practical "
                                + "projects, maintaining consistent activity "
                                + "and documenting your repositories."
                );
            }

        } catch (RuntimeException e) {

            // Log the actual error for debugging
            logger.error(
                    "GitHub analysis failed for student ID: {}",
                    studentId,
                    e
            );

            components.add(new EmployabilityScoreDTO.ComponentScore(
                    "Coding Proficiency",
                    0,
                    20,
                    "NOT_ASSESSED",
                    "GitHub data could not be retrieved. "
                            + "Check your GitHub username and API availability."
            ));

            recommendations.add(
                    "Connect a valid public GitHub profile to assess your coding activity."
            );
        }

        // =========================================
        // 5. INTERVIEW READINESS
        // =========================================

        List<MockInterviewAttempt> attempts =
                mockInterviewAttemptRepository
                        .findByStudent_IdOrderByCreatedAtDesc(studentId);

        if (attempts != null && !attempts.isEmpty()) {

            double averageScore = attempts.stream()
                    .filter(attempt ->
                            attempt.getScore() >= 0
                                    && attempt.getScore() <= 100)
                    .mapToInt(MockInterviewAttempt::getScore)
                    .average()
                    .orElse(-1);

            if (averageScore >= 0) {

                double interviewScore = Math.round(
                        averageScore / 5.0 * 100.0
                ) / 100.0;

                totalScore += (int) Math.round(interviewScore);
                assessedComponents++;

                components.add(new EmployabilityScoreDTO.ComponentScore(
                        "Interview Readiness",
                        interviewScore,
                        20,
                        "ASSESSED",
                        "Based on " + attempts.size()
                                + " saved mock interview attempts. "
                                + "Average interview performance: "
                                + Math.round(averageScore * 100.0) / 100.0
                                + "/100."
                ));

                if (interviewScore < 12) {
                    recommendations.add(
                            "Practice more technical mock interviews "
                                    + "and work on the weak areas in your evaluations."
                    );
                }

            } else {

                components.add(new EmployabilityScoreDTO.ComponentScore(
                        "Interview Readiness",
                        0,
                        20,
                        "NOT_ASSESSED",
                        "No valid interview scores are available."
                ));
            }

        } else {

            components.add(new EmployabilityScoreDTO.ComponentScore(
                    "Interview Readiness",
                    0,
                    20,
                    "NOT_ASSESSED",
                    "Complete a mock interview to assess interview readiness."
            ));

            recommendations.add(
                    "Complete a mock interview to measure your interview performance."
            );
        }

        // =========================================
        // 6. CALCULATE OVERALL SCORE
        // =========================================

        double overallScore = assessedComponents == 0
                ? 0
                : Math.round(
                        (totalScore * 100.0)
                                / (assessedComponents * 20)
                                * 100.0
                ) / 100.0;

        int assessmentCoverage = assessedComponents * 20;

        String readinessLevel =
                getReadinessLevel(overallScore, assessedComponents);

        if (assessedComponents == 0) {
            recommendations.add(
                    "Complete your profile and assessments to generate "
                            + "your employability score."
            );
        }

        return new EmployabilityScoreDTO(
                studentId,
                overallScore,
                readinessLevel,
                assessmentCoverage,
                assessedComponents,
                5,
                components,
                recommendations
        );
    }

    // =========================================
    // RESUME SCORE CALCULATION
    // =========================================

    private Integer calculateResumeScore(StudentProfile profile) {

        String resumeText = profile.getResumeText();

        if (resumeText == null || resumeText.isBlank()) {
            return null;
        }

        int score = 5;

        if (resumeText.length() >= 500) {
            score += 5;
        }

        Set<String> resumeSkills = new HashSet<>();
        addSkills(resumeSkills, profile.getResumeSkills());

        if (resumeSkills.size() >= 3) {
            score += 5;
        }

        String text = resumeText.toLowerCase();

        String[] sectionKeywords = {
                "education",
                "skills",
                "projects",
                "experience",
                "certifications"
        };

        int sectionCount = 0;

        for (String section : sectionKeywords) {

            if (Pattern.compile(
                    "(?m)^\\s*" + Pattern.quote(section) + "\\s*:?\\s*$"
            ).matcher(text).find()) {

                sectionCount++;
            }
        }

        if (sectionCount >= 3) {
            score += 5;
        }

        return Math.min(score, 20);
    }

    // =========================================
    // EXTRACT DISTINCT SKILLS
    // =========================================

    private void addSkills(Set<String> skills, String rawSkills) {

        if (rawSkills == null || rawSkills.isBlank()) {
            return;
        }

        String[] values = rawSkills.split("[,;\\n]");

        for (String value : values) {

            String skill = value.trim().toLowerCase();

            if (!skill.isEmpty()) {
                skills.add(skill);
            }
        }
    }

    // =========================================
    // COUNT PROJECT ENTRIES
    // =========================================

    private int countProjects(String projects) {

        if (projects == null || projects.isBlank()) {
            return 0;
        }

        String[] entries = projects.split("[;\\r\\n]+");

        int count = 0;

        for (String entry : entries) {

            if (!entry.trim().isEmpty()) {
                count++;
            }
        }

        return count;
    }

    // =========================================
    // DETERMINE READINESS LEVEL
    // =========================================

    private String getReadinessLevel(
            double score,
            int assessedComponents) {

        if (assessedComponents == 0) {
            return "Not Assessed";
        }

        if (score < 40) {
            return "Early Stage";
        }

        if (score < 60) {
            return "Developing";
        }

        if (score < 80) {
            return "Competitive";
        }

        return "Strong Foundation";
    }
}


