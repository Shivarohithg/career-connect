
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

import com.careerconnect.backend.dto.LearningRoadmapDTO;
import com.careerconnect.backend.dto.LearningRoadmapDTO.SkillPlan;
import com.careerconnect.backend.model.Job;
import com.careerconnect.backend.model.Student;
import com.careerconnect.backend.model.StudentProfile;
import com.careerconnect.backend.repository.JobRepository;
import com.careerconnect.backend.repository.StudentProfileRepository;
import com.careerconnect.backend.repository.StudentRepository;

@Service
public class LearningRoadmapService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    public LearningRoadmapDTO generateRoadmap(int studentId, int jobId) {

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

        // 4. Collect student skills
        Set<String> studentSkills = new LinkedHashSet<>();

        if (profile != null) {
            studentSkills.addAll(splitValues(profile.getSkills()));
            studentSkills.addAll(splitValues(profile.getResumeSkills()));
        }

        Set<String> normalizedStudentSkills = studentSkills.stream()
                .map(this::normalize)
                .collect(Collectors.toSet());

        // 5. Get job-required skills
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

        // 6. Calculate current skill match
        double currentSkillMatch;

        if (requiredSkills.isEmpty()) {
            currentSkillMatch = 100.0;
        } else {
            currentSkillMatch =
                    matchedSkills.size() * 100.0 / requiredSkills.size();

            currentSkillMatch =
                    Math.round(currentSkillMatch * 100.0) / 100.0;
        }

        // 7. Generate learning plan
        List<SkillPlan> learningPlan = new ArrayList<>();

        int totalWeeks = 0;

        for (String skill : missingSkills) {

            int weeks = estimateWeeks(skill);

            List<String> steps = getLearningSteps(skill);

            learningPlan.add(
                    new SkillPlan(
                            skill,
                            "High",
                            weeks,
                            steps
                    )
            );

            totalWeeks += weeks;
        }

        // 8. Return roadmap
        return new LearningRoadmapDTO(
                studentId,
                jobId,
                job.getTitle(),
                currentSkillMatch,
                matchedSkills,
                missingSkills,
                totalWeeks,
                learningPlan
        );
    }

    // Estimate learning duration based on skill
    private int estimateWeeks(String skill) {

        String normalizedSkill = normalize(skill);

        switch (normalizedSkill) {

            case "java":
            case "python":
            case "c++":
            case "javascript":
            case "sql":
                return 2;

            case "spring boot":
            case "react":
            case "node.js":
            case "nodejs":
            case "hibernate":
            case "mongodb":
            case "mysql":
                return 3;

            case "microservices":
            case "system design":
            case "machine learning":
            case "deep learning":
                return 4;

            default:
                return 2;
        }
    }

    // Generate learning steps for each missing skill
    private List<String> getLearningSteps(String skill) {

        String normalizedSkill = normalize(skill);

        switch (normalizedSkill) {

            case "java":
                return Arrays.asList(
                        "Learn Java syntax, variables, data types and control statements.",
                        "Practice OOP concepts, collections and exception handling.",
                        "Solve Java programming problems.",
                        "Build a small Java application."
                );

            case "spring boot":
                return Arrays.asList(
                        "Understand Spring Boot architecture and dependency injection.",
                        "Learn REST APIs, controllers and service layers.",
                        "Connect Spring Boot with MySQL using Spring Data JPA.",
                        "Build a complete CRUD REST API."
                );

            case "sql":
            case "mysql":
                return Arrays.asList(
                        "Learn SQL queries, filtering and sorting.",
                        "Practice joins, grouping and aggregate functions.",
                        "Understand subqueries and database relationships.",
                        "Create and query a sample relational database."
                );

            case "react":
                return Arrays.asList(
                        "Learn components, JSX and props.",
                        "Practice state management and React hooks.",
                        "Understand React Router and API integration.",
                        "Build a small React application."
                );

            case "javascript":
                return Arrays.asList(
                        "Learn variables, functions, arrays and objects.",
                        "Understand ES6 features and asynchronous JavaScript.",
                        "Practice DOM manipulation and event handling.",
                        "Build an interactive web application."
                );

            case "python":
                return Arrays.asList(
                        "Learn Python syntax, data types and functions.",
                        "Practice lists, dictionaries and OOP.",
                        "Solve Python programming problems.",
                        "Build a small Python project."
                );

            case "microservices":
                return Arrays.asList(
                        "Understand monolithic and microservices architecture.",
                        "Learn how independent services communicate.",
                        "Explore service discovery and API gateways.",
                        "Build a small multi-service application."
                );

            default:
                return Arrays.asList(
                        "Understand the fundamentals of " + skill + ".",
                        "Learn the important concepts and practical techniques.",
                        "Practice with exercises related to " + skill + ".",
                        "Build a small project using " + skill + "."
                );
        }
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

