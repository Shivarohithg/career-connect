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

        List<String> skills = parseList(studentSkills);
        List<String> gaps = parseList(skillGaps);

        String bestRole = getBestRole(recommendedRoles);
        int matchPercentage = getBestRolePercentage(recommendedRoles);

        int readinessScore =
                calculateReadinessScore(
                        skills.size(),
                        gaps.size()
                );

        StringBuilder advice = new StringBuilder();

        advice.append("AI CAREER INSIGHTS\n\n");


        // Career readiness
        advice.append("Career Readiness Score: ")
              .append(readinessScore)
              .append("%\n\n");


        // Best career direction
        if (!bestRole.isEmpty()) {

            advice.append("Best Career Direction: ")
                  .append(bestRole)
                  .append("\n");

            advice.append("Career Match: ")
                  .append(matchPercentage)
                  .append("%\n\n");
        }


        // Why this role
        if (!bestRole.isEmpty()) {

            advice.append("Why this role?\n");

            if (skills.size() >= 3) {

                advice.append(
                        "Your profile contains multiple technical "
                        + "skills that align with the requirements "
                        + "of the "
                        + bestRole
                        + " role."
                );

            } else {

                advice.append(
                        "Your current skills provide a foundation "
                        + "for the "
                        + bestRole
                        + " career direction."
                );
            }

            advice.append("\n\n");
        }


        // Current strengths
        advice.append("Current Strengths:\n");

        if (skills.isEmpty()) {

            advice.append(
                    "No technical skills added yet.\n"
            );

        } else {

            for (String skill : skills) {

                advice.append("✓ ")
                      .append(skill)
                      .append("\n");
            }
        }


        // Skill gaps
        if (!gaps.isEmpty()) {

            advice.append("\nPriority Skill Gaps:\n");

            int limit =
                    Math.min(gaps.size(), 5);

            for (int i = 0; i < limit; i++) {

                String priority;

                if (i == 0) {
                    priority = "HIGH";
                } else if (i == 1) {
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


        // Personalized roadmap
        advice.append(
                "\nRecommended Learning Roadmap:\n"
        );

        if (!gaps.isEmpty()) {

            int roadmapLimit =
                    Math.min(gaps.size(), 4);

            for (int i = 0;
                 i < roadmapLimit;
                 i++) {

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


        // Personalized advice
        advice.append(
                "\nAI Recommendation:\n"
        );

        if (!bestRole.isEmpty() &&
                !gaps.isEmpty()) {

            advice.append(
                    "Your current profile is most aligned with "
                    + bestRole
                    + ". Focus first on "
                    + gaps.get(0)
                    + " to improve your readiness for this role."
            );

        } else if (!bestRole.isEmpty()) {

            advice.append(
                    "Your current skill profile is well aligned "
                    + "with "
                    + bestRole
                    + ". Continue building practical projects "
                    + "and preparing for technical interviews."
            );

        } else {

            advice.append(
                    "Add more technical skills to your profile "
                    + "so CareerConnect AI can generate a more "
                    + "personalized career recommendation."
            );
        }


        // Project recommendation
        if (!gaps.isEmpty()) {

            advice.append(
                    "\n\nSuggested Action:\n"
                    + "Build a practical project that uses your "
                    + "existing skills together with "
                    + gaps.get(0)
                    + ". This will help convert the skill gap "
                    + "into practical experience."
            );
        }


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


    private List<String> parseList(
            String value) {

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
                        "\\s*\\(\\d+%\\s*Match\\)",
                        ""
                )
                .trim();
    }


    private int getBestRolePercentage(
            String recommendedRoles) {

        if (recommendedRoles == null ||
                recommendedRoles.trim().isEmpty()) {

            return 0;
        }

        String firstRole =
                recommendedRoles
                        .split(",")[0]
                        .trim();

        try {

            int start =
                    firstRole.lastIndexOf("(") + 1;

            int end =
                    firstRole.indexOf("%", start);

            return Integer.parseInt(
                    firstRole.substring(
                            start,
                            end
                    )
            );

        } catch (Exception e) {

            return 0;
        }
    }
}
