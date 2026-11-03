
package com.careerconnect.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.careerconnect.backend.model.CareerAnalysis;

import java.util.ArrayList;
import java.util.List;

@Service
public class CareerRoadmapService {

    @Autowired
    private CareerAnalysisService careerAnalysisService;

    public String generateRoadmap(int studentId) {

        /*
         * GET EXISTING CAREER ANALYSIS
         */

        CareerAnalysis analysis =
                careerAnalysisService.getAnalysis(studentId);

        String recommendedRoles =
                analysis.getRecommendedRoles() == null
                        ? ""
                        : analysis.getRecommendedRoles();

        String recommendedSkills =
                analysis.getRecommendedSkills() == null
                        ? ""
                        : analysis.getRecommendedSkills();

        String skillGaps =
                analysis.getSkillGaps() == null
                        ? ""
                        : analysis.getSkillGaps();

        /*
         * CHECK CAREER ROLE
         */

        if (recommendedRoles.trim().isEmpty()) {

            return """
                    CAREER ROADMAP

                    No recommended career role is available.

                    Please complete your career analysis
                    and add technical skills to your profile.
                    """;
        }

        /*
         * GET BEST ROLE
         */

        String bestRole =
                recommendedRoles.split(",")[0].trim();

        /*
         * FILTER RELEVANT SKILLS
         */

        List<String> relevantSkills =
                filterRelevantSkills(bestRole, recommendedSkills);

        List<String> relevantGaps =
                filterRelevantSkills(bestRole, skillGaps);

        /*
         * GENERATE ROADMAP
         */

        StringBuilder roadmap = new StringBuilder();

        roadmap.append("CAREER GOAL:\n");
        roadmap.append(bestRole);
        roadmap.append("\n\n");

        /*
         * PHASE 1
         */

        roadmap.append("""
                PHASE 1 - FUNDAMENTALS:

                1. Java Syntax:
                   Variables, data types, operators,
                   conditional statements and loops.

                2. Object-Oriented Programming:
                   Classes, objects, inheritance,
                   polymorphism, abstraction and encapsulation.

                3. Java Collections:
                   ArrayList, HashMap, HashSet
                   and basic collection operations.

                Practical Exercise:
                Write a Java program to store student
                details using a class and display them.
                """);

        roadmap.append("\n\n");

        /*
         * PHASE 2
         */

        roadmap.append("PHASE 2 - CORE SKILLS:\n\n");

        if (!relevantSkills.isEmpty()) {

            roadmap.append("Relevant Skills:\n");

            for (String skill : relevantSkills) {

                roadmap.append("- ");
                roadmap.append(skill);
                roadmap.append("\n");
            }

            roadmap.append("\n");
        }

        if (!relevantGaps.isEmpty()) {

            roadmap.append("Priority Skill Gaps:\n");

            for (String gap : relevantGaps) {

                roadmap.append("- ");
                roadmap.append(gap);
                roadmap.append("\n");
            }

            roadmap.append("\n");
        }

        roadmap.append("""
                Core Learning:
                Understand Spring Boot application structure,
                REST controllers, service layers,
                database integration and API testing.

                Practical Exercise:
                Create a Spring Boot GET endpoint
                that returns a list of students.
                """);

        roadmap.append("\n\n");

        /*
         * PHASE 3
         */

        roadmap.append("""
                PHASE 3 - PRACTICAL DEVELOPMENT:

                Project Name:
                User Management REST API

                Project Features:
                1. Create a user.
                2. Retrieve user details.
                3. Update user information.
                4. Delete a user.

                Technologies:
                Java, Spring Boot, REST APIs and SQL.

                Learning Outcome:
                Understand backend application structure,
                CRUD operations and database integration.
                """);

        roadmap.append("\n\n");

        /*
         * PHASE 4
         */

        roadmap.append("""
                PHASE 4 - INTERVIEW PREPARATION:

                Interview Topics:
                1. Java OOP and Collections.
                2. Spring Boot architecture.
                3. REST API design and HTTP methods.

                Interview Practice:
                Explain the difference between
                GET, POST, PUT and DELETE
                with suitable examples.
                """);

        roadmap.append("\n\n");

        /*
         * FINAL ACTION
         */

        roadmap.append("""
                FINAL ACTION:

                Task:
                Write a Java method that checks
                whether a number is even or odd.

                Objective:
                Practice Java methods, conditional
                statements and return values.
                """);

        return roadmap.toString();
    }

    /*
     * FILTER SKILLS BY CAREER ROLE
     */

    private List<String> filterRelevantSkills(
            String role,
            String skills) {

        List<String> filtered = new ArrayList<>();

        if (skills == null || skills.trim().isEmpty()) {
            return filtered;
        }

        String normalizedRole = role.toLowerCase();

        List<String> allowedSkills;

        if (normalizedRole.contains("backend")) {

            allowedSkills = List.of(
                    "java",
                    "oop",
                    "data structures",
                    "algorithms",
                    "spring boot",
                    "spring",
                    "rest apis",
                    "rest api",
                    "sql",
                    "mysql",
                    "jpa",
                    "hibernate",
                    "git",
                    "maven",
                    "gradle"
            );

        } else if (normalizedRole.contains("frontend")) {

            allowedSkills = List.of(
                    "html",
                    "css",
                    "javascript",
                    "react",
                    "git",
                    "rest apis",
                    "rest api"
            );

        } else if (normalizedRole.contains("python")) {

            allowedSkills = List.of(
                    "python",
                    "data structures",
                    "algorithms",
                    "sql",
                    "git",
                    "rest apis",
                    "rest api"
            );

        } else {

            allowedSkills = List.of(
                    "java",
                    "python",
                    "c++",
                    "c",
                    "javascript",
                    "sql",
                    "git",
                    "data structures",
                    "algorithms",
                    "rest apis",
                    "rest api",
                    "spring boot"
            );
        }

        String[] skillArray = skills.split(",");

        for (String skill : skillArray) {

            String trimmedSkill = skill.trim();

            String normalizedSkill =
                    trimmedSkill.toLowerCase();

            for (String allowed : allowedSkills) {

                if (normalizedSkill.equals(allowed)) {

                    if (!filtered.contains(trimmedSkill)) {
                        filtered.add(trimmedSkill);
                    }

                    break;
                }
            }
        }

        return filtered;
    }
}

