
package com.careerconnect.backend.dto;

import java.util.List;

public record LearningRoadmapDTO(

        int studentId,

        int jobId,

        String jobTitle,

        double currentSkillMatch,

        List<String> matchedSkills,

        List<String> missingSkills,

        int estimatedDurationWeeks,

        List<SkillPlan> learningPlan

) {

    public record SkillPlan(

            String skill,

            String priority,

            int estimatedWeeks,

            List<String> learningSteps

    ) {}

}

