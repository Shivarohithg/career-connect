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

        if (studentSkills == null || studentSkills.trim().isEmpty()) {
            return "Add your skills to your profile to receive personalized career recommendations.";
        }

        List<String> skills = parseList(studentSkills);
        List<String> gaps = parseList(skillGaps);

        String bestRole = getBestRole(recommendedRoles);

        StringBuilder advice = new StringBuilder();

        advice.append("Based on your current profile, ");

        if (!bestRole.isEmpty()) {
            advice.append("your strongest career direction is ")
                  .append(bestRole)
                  .append(". ");
        } else {
            advice.append("you have a good foundation for several technology careers. ");
        }

        advice.append("\n\nYour current strengths include ");

        if (!skills.isEmpty()) {
            advice.append(String.join(", ", skills));
        } else {
            advice.append("the skills listed in your profile");
        }

        advice.append(". ");

        if (!gaps.isEmpty()) {

            advice.append("\n\nTo improve your job readiness, focus on ");

            int limit = Math.min(gaps.size(), 5);

            for (int i = 0; i < limit; i++) {

                if (i > 0) {
                    advice.append(", ");
                }

                advice.append(gaps.get(i));
            }

            advice.append(".");

        } else {

            advice.append(
                    "\n\nYour current skill profile matches the available career roles well."
            );
        }

        advice.append(
                "\n\nRecommended approach: strengthen your existing skills, "
                + "build practical projects, practice DSA and gradually learn "
                + "the missing technologies required for your target role."
        );

        return advice.toString();
    }

    private List<String> parseList(String value) {

        List<String> result = new ArrayList<>();

        if (value == null || value.trim().isEmpty()) {
            return result;
        }

        String[] items = value.split(",");

        for (String item : items) {

            String cleaned = item.trim();

            if (!cleaned.isEmpty()) {
                result.add(cleaned);
            }
        }

        return result;
    }

    private String getBestRole(String recommendedRoles) {

        if (recommendedRoles == null ||
                recommendedRoles.trim().isEmpty()) {

            return "";
        }

        String firstRole =
                recommendedRoles.split(",")[0].trim();

        return firstRole
                .replaceAll(
                        "\\s*\\(\\d+% Match\\)",
                        ""
                )
                .trim();
    }
} 
