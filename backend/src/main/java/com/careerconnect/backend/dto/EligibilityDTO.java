
package com.careerconnect.backend.dto;

import java.util.List;

public record EligibilityDTO(

        int studentId,
        int jobId,

        boolean eligible,

        boolean cgpaEligible,
        boolean branchEligible,

        double studentCgpa,
        double requiredCgpa,

        String studentBranch,
        List<String> eligibleBranches,

        List<String> matchedSkills,
        List<String> missingSkills,

        double skillMatchPercentage,

        String message

) {}

