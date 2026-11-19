
package com.careerconnect.backend.dto;

public record LeetCodeAnalysisDTO(
        String username,
        int totalSolved,
        int easySolved,
        int mediumSolved,
        int hardSolved,
        Integer globalRanking,
        int totalActiveDays,
        int currentStreak,
        Integer contestsAttended,
        Double contestRating,
        Integer contestGlobalRanking
) {}

