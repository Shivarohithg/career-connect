
package com.careerconnect.backend.dto;

import java.util.List;

public record EmployabilityScoreDTO(

        int studentId,

        double overallScore,

        String readinessLevel,

        int assessmentCoverage,

        int assessedComponents,

        int totalComponents,

        List<ComponentScore> components,

        List<String> recommendations

) {

    public record ComponentScore(

            String component,

            double score,

            double maximumScore,

            String status,

            String explanation

    ) {
    }
}

