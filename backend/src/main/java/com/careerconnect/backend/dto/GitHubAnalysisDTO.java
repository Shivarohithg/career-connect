
package com.careerconnect.backend.dto;

import java.util.List;
import java.util.Map;

public record GitHubAnalysisDTO(
        String username,
        String name,
        String profileUrl,
        int publicRepositories,
        int followers,
        int totalStars,
        int activeRepositories,
        Map<String, Integer> languages,
        List<RepositoryInfo> repositories
) {
    public record RepositoryInfo(
            String name,
            String description,
            String language,
            int stars,
            String updatedAt,
            String url
    ) {}
}

