package com.careerconnect.backend.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CareerRecommendationService {

    @Autowired
    private OllamaService ollamaService;

    public String generateCareerAdvice(
            String studentSkills,
            String recommendedRoles,
            String skillGaps) {

        List<String> skills =
                parseList(studentSkills);

        List<String> gaps =
                parseList(skillGaps);

        String bestRole =
                getBestRole(recommendedRoles);

        int matchPercentage =
                getBestRolePercentage(
                        recommendedRoles
                );

        int readinessScore =
                calculateReadinessScore(
                        skills.size(),
                        gaps.size()
                );

        StringBuilder advice =
                new StringBuilder();

        advice.append(
                "AI CAREER INSIGHTS\n\n"
        );

        advice.append(
                "Career Readiness Score: "
        )
        .append(readinessScore)
        .append("%\n\n");


        /*
         * BEST CAREER DIRECTION
         */

        if (!bestRole.isEmpty()) {

            advice.append(
                    "Best Career Direction: "
            )
            .append(bestRole)
            .append("\n");

            advice.append(
                    "Career Match: "
            )
            .append(matchPercentage)
            .append("%\n\n");
        }


        /*
         * WHY THIS ROLE
         */

        if (!bestRole.isEmpty()) {

            advice.append(
                    "Why this role?\n"
            );

            if (matchPercentage >= 80) {

                advice.append(
                        "Your current skill profile has "
                        + "strong alignment with the "
                        + bestRole
                        + " role. You already have most "
                        + "of the important skills needed "
                        + "for this career direction."
                );

            } else if (matchPercentage >= 60) {

                advice.append(
                        "Your profile shows strong potential "
                        + "for the "
                        + bestRole
                        + " role. Several of your existing "
                        + "skills already match the role "
                        + "requirements."
                );

            } else if (matchPercentage >= 40) {

                advice.append(
                        "Your current skills provide a "
                        + "foundation for the "
                        + bestRole
                        + " career direction. Developing "
                        + "the missing skills can improve "
                        + "your alignment."
                );

            } else {

                advice.append(
                        "Your current profile has some "
                        + "connection with the "
                        + bestRole
                        + " career direction, but additional "
                        + "skills are needed to improve "
                        + "your readiness."
                );
            }

            advice.append("\n\n");
        }


        /*
         * CURRENT STRENGTHS
         */

        advice.append(
                "Current Strengths:\n"
        );

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


        /*
         * PRIORITY SKILL GAPS
         */

        if (!gaps.isEmpty()) {

            advice.append(
                    "\nPriority Skill Gaps:\n"
            );

            int limit =
                    Math.min(
                            gaps.size(),
                            5
                    );

            for (int i = 0;
                 i < limit;
                 i++) {

                String priority;

                if (i == 0) {
                    priority = "HIGH";
                } else if (i == 1) {
                    priority = "MEDIUM";
                } else {
                    priority = "LOW";
                }

                advice.append(
                        i + 1
                )
                .append(". ")
                .append(gaps.get(i))
                .append(" [")
                .append(priority)
                .append("]\n");
            }
        }


        /*
         * LEARNING ROADMAP
         */

        advice.append(
                "\nRecommended Learning Roadmap:\n"
        );

        if (!gaps.isEmpty()) {

            int roadmapLimit =
                    Math.min(
                            gaps.size(),
                            4
                    );

            for (int i = 0;
                 i < roadmapLimit;
                 i++) {

                advice.append(
                        "Step "
                )
                .append(i + 1)
                .append(": Learn ")
                .append(gaps.get(i))
                .append("\n");
            }

        } else {

            advice.append(
                    "Continue improving your existing "
                    + "skills through projects, coding "
                    + "practice, and technical interviews.\n"
            );
        }


        /*
         * PERSONALIZED AI RECOMMENDATION
         */

        advice.append(
                "\nAI Recommendation:\n"
        );

        if (!bestRole.isEmpty() &&
                !gaps.isEmpty()) {

            String primaryGap =
                    gaps.get(0);

            advice.append(
                    "Your current profile is most aligned "
                    + "with "
                    + bestRole
                    + ". Your next priority should be "
                    + primaryGap
                    + ". Strengthening this skill can "
                    + "improve your readiness for "
                    + bestRole
                    + " opportunities."
            );

        } else if (!bestRole.isEmpty()) {

            advice.append(
                    "Your current skill profile is well "
                    + "aligned with "
                    + bestRole
                    + ". Focus on practical projects, "
                    + "problem solving, and technical "
                    + "interview preparation."
            );

        } else {

            advice.append(
                    "Add more technical skills to your "
                    + "profile so CareerConnect AI can "
                    + "generate a more personalized "
                    + "career recommendation."
            );
        }


        /*
         * NEXT ACTION
         */

        advice.append(
                "\n\nSuggested Action:\n"
        );

        if (!gaps.isEmpty() &&
                !bestRole.isEmpty()) {

            advice.append(
                    "Build a practical project that "
                    + "combines your existing skills "
                    + "with "
                    + gaps.get(0)
                    + ". Then add the project to your "
                    + "profile to demonstrate practical "
                    + "experience."
            );

        } else if (!bestRole.isEmpty()) {

            advice.append(
                    "Build another project related to "
                    + bestRole
                    + " and practice role-specific "
                    + "technical interview questions."
            );

        } else {

            advice.append(
                    "Add your technical skills, projects, "
                    + "and coding-platform experience "
                    + "to your profile."
            );
        }


        /*
         * INTERVIEW PREPARATION
         */

        advice.append(
                "\n\nInterview Preparation:\n"
        );

        if (!bestRole.isEmpty()) {

            advice.append(
                    "For "
                    + bestRole
                    + " roles, practice data structures, "
                    + "problem solving, core programming "
                    + "concepts, and questions related "
                    + "to your primary technical skills."
            );

        } else {

            advice.append(
                    "Practice data structures, problem "
                    + "solving, programming fundamentals, "
                    + "and technical interview questions."
            );
        }


        /*
         * =========================================================
         * REAL AI INTEGRATION
         * =========================================================
         *
         * Everything above creates a deterministic analysis.
         * Ollama receives that analysis together with the
         * student's actual skills, roles and skill gaps.
         *
         * Qwen then generates the personalized explanation,
         * roadmap, project recommendation and interview advice.
         */

        String baseAnalysis =
                advice.toString();

        return ollamaService.generateCareerAdvice(
                studentSkills,
                recommendedRoles,
                skillGaps,
                baseAnalysis
        );
    }


    /*
     * READINESS SCORE
     */

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

        return Math.min(
                score,
                100
        );
    }


    /*
     * PARSE SKILLS
     */

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


    /*
     * GET BEST ROLE
     */

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


    /*
     * GET BEST ROLE PERCENTAGE
     */

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
                    firstRole.indexOf(
                            "%",
                            start
                    );

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