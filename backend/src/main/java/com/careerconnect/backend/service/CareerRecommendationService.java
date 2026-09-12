package com.careerconnect.backend.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class CareerRecommendationService {

    public String generateCareerAdvice(
            String studentSkills,
            String recommendedRoles,
            String skillGaps) {

        if (studentSkills == null ||
                studentSkills.trim().isEmpty()) {

            return "Add your skills to your profile to receive personalized career recommendations.";
        }

        List<String> skills = parseList(studentSkills);
        List<String> gaps = parseList(skillGaps);

        String bestRole = getBestRole(recommendedRoles);

        int readinessScore =
                calculateReadinessScore(
                        skills.size(),
                        gaps.size()
                );

        StringBuilder advice = new StringBuilder();

        advice.append("AI CAREER INSIGHTS\n\n");

        advice.append("Career Readiness Score: ")
              .append(readinessScore)
              .append("%\n\n");

        if (!bestRole.isEmpty()) {

            advice.append("Best Career Direction: ")
                  .append(bestRole)
                  .append("\n\n");
        }

        advice.append("Current Strengths:\n");

        for (String skill : skills) {

            advice.append("✓ ")
                  .append(skill)
                  .append("\n");
        }

        if (!gaps.isEmpty()) {

            advice.append("\nPriority Skill Gaps:\n");

            int limit = Math.min(gaps.size(), 5);

            for (int i = 0; i < limit; i++) {

                String priority;

                if (i < 2) {
                    priority = "HIGH";
                } else if (i < 4) {
                    priority = "MEDIUM";
                } else {
                    priority = "LOW";
                }

                advice.append(i + 1)
                      .append(". ")
                      .append(gaps.get(i))
                      .append(" [")
                      .append(priority)
                      .append("]\n");
            }
        }

        advice.append("\nRecommended Learning Roadmap:\n");

        if (!gaps.isEmpty()) {

            int roadmapLimit =
                    Math.min(gaps.size(), 4);

            for (int i = 0; i < roadmapLimit; i++) {

                advice.append("Step ")
                      .append(i + 1)
                      .append(": Learn ")
                      .append(gaps.get(i))
                      .append("\n");
            }

        } else {

            advice.append(
                    "Continue improving your existing skills "
                    + "through projects and interview practice.\n"
            );
        }

        advice.append(
                "\nCareer Advice:\n"
                + "Strengthen your existing technical skills, "
                + "work on practical projects, practice DSA, "
                + "and focus on the highest-priority skill gaps "
                + "before applying for advanced roles."
        );

        return advice.toString();
    }


    private int calculateReadinessScore(
            int skillCount,
            int gapCount) {

        if (skillCount == 0) {
            return 0;
        }

        int total =
                skillCount + gapCount;

        if (total == 0) {
            return 100;
        }

        int score =
                (skillCount * 100) / total;

        return Math.min(score, 100);
    }


    private List<String> parseList(String value) {

        List<String> result =
                new ArrayList<>();

        if (value == null ||
                value.trim().isEmpty()) {

            return result;
        }

        String[] items =
                value.split(",");

        for (String item : items) {

            String cleaned =
                    item.trim();

            if (!cleaned.isEmpty()) {
                result.add(cleaned);
            }
        }

        return result;
    }


    private String getBestRole(
            String recommendedRoles) {

        if (recommendedRoles == null ||
                recommendedRoles.trim().isEmpty()) {

            return "";
        }

        String firstRole =
                recommendedRoles
                        .split(",")[0]
                        .trim();

        return firstRole
                .replaceAll(
                        "\\s*\\(\\d+% Match\\)",
                        ""
                )
                .trim();
    }
}
