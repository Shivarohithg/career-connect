
package com.careerconnect.backend.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.careerconnect.backend.dto.GitHubAnalysisDTO;
import com.careerconnect.backend.model.Student;
import com.careerconnect.backend.model.StudentProfile;
import com.careerconnect.backend.repository.StudentProfileRepository;
import com.careerconnect.backend.repository.StudentRepository;

import tools.jackson.databind.JsonNode;

@Service
public class GitHubAnalysisService {

    private final RestClient restClient;

    private final StudentRepository studentRepository;

    private final StudentProfileRepository studentProfileRepository;

    private final String githubToken =
        System.getenv("GITHUB_TOKEN");

    // Cache duration: 15 minutes
    private static final long CACHE_DURATION_MS =
            15 * 60 * 1000L;

    // Store GitHub analysis results temporarily
    private final ConcurrentMap<String, CachedGitHubAnalysis> cache =
            new ConcurrentHashMap<>();

    // Cached result structure
    private record CachedGitHubAnalysis(
            GitHubAnalysisDTO data,
            long cachedAt
    ) {}

    
public GitHubAnalysisService(
        StudentRepository studentRepository,
        StudentProfileRepository studentProfileRepository) {

    this.restClient = RestClient.builder()
            .baseUrl("https://api.github.com")
            .defaultHeader(
                    HttpHeaders.ACCEPT,
                    "application/vnd.github+json")
            .defaultHeader(
                    "X-GitHub-Api-Version",
                    "2022-11-28")
            .defaultHeader(
                    HttpHeaders.USER_AGENT,
                    "CareerConnect-AI")
            .defaultHeaders(headers -> {
                if (githubToken != null && !githubToken.isBlank()) {
                    headers.setBearerAuth(githubToken);
                }
            })
            .build();

    this.studentRepository = studentRepository;
    this.studentProfileRepository = studentProfileRepository;
}


    public GitHubAnalysisDTO analyzeStudent(int studentId) {

        // 1. Find student
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() ->
                        new RuntimeException("Student not found"));

        // 2. Find student profile
        StudentProfile profile =
                studentProfileRepository.findByStudent(student)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Student profile not found"));

        // 3. Get GitHub username
        String savedUsername = profile.getGithubUsername();

        if (savedUsername == null || savedUsername.isBlank()) {
            throw new RuntimeException(
                    "Please add your GitHub username to your profile");
        }

        final String username = savedUsername.trim();

        // 4. Check cache
        CachedGitHubAnalysis cached = cache.get(username);

        if (cached != null) {

            long currentTime = System.currentTimeMillis();

            if (currentTime - cached.cachedAt()
                    < CACHE_DURATION_MS) {

                return cached.data();
            }

            // Remove expired cache entry
            cache.remove(username, cached);
        }

        try {

            // 5. Fetch GitHub user information
            JsonNode userData = restClient.get()
                    .uri("/users/{username}", username)
                    .retrieve()
                    .body(JsonNode.class);

            if (userData == null) {
                throw new RuntimeException(
                        "Unable to retrieve GitHub profile");
            }

            // 6. Fetch up to 100 public repositories
            JsonNode repoData = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/users/{username}/repos")
                            .queryParam("per_page", 100)
                            .queryParam("sort", "updated")
                            .build(username))
                    .retrieve()
                    .body(JsonNode.class);

            int totalStars = 0;

            int activeRepositories = 0;

            Map<String, Integer> languages =
                    new LinkedHashMap<>();

            List<GitHubAnalysisDTO.RepositoryInfo> repositories =
                    new ArrayList<>();

            Instant activeSince =
                    Instant.now().minus(180, ChronoUnit.DAYS);

            // 7. Process repositories
            if (repoData != null && repoData.isArray()) {

                for (JsonNode repo : repoData) {

                    String repoName =
                            repo.path("name").asText("");

                    String description =
                            repo.path("description").asText("");

                    String language =
                            repo.path("language").isNull()
                                    ? "Not specified"
                                    : repo.path("language").asText();

                    int stars =
                            repo.path("stargazers_count").asInt(0);

                    String updatedAt =
                            repo.path("updated_at").asText("");

                    String pushedAt =
                            repo.path("pushed_at").asText("");

                    String repoUrl =
                            repo.path("html_url").asText("");

                    totalStars += stars;

                    if (!language.equals("Not specified")) {
                        languages.merge(
                                language, 1, Integer::sum);
                    }

                    // Check recent repository activity
                    if (!pushedAt.isBlank()) {

                        try {

                            if (Instant.parse(pushedAt)
                                    .isAfter(activeSince)) {

                                activeRepositories++;
                            }

                        } catch (Exception ignored) {
                            // Ignore invalid timestamps
                        }
                    }

                    repositories.add(
                            new GitHubAnalysisDTO.RepositoryInfo(
                                    repoName,
                                    description,
                                    language,
                                    stars,
                                    updatedAt,
                                    repoUrl
                            )
                    );
                }
            }

            // 8. Sort languages by repository count
            Map<String, Integer> sortedLanguages =
                    new LinkedHashMap<>();

            languages.entrySet()
                    .stream()
                    .sorted(
                            Map.Entry
                                    .<String, Integer>
                                    comparingByValue()
                                    .reversed()
                    )
                    .forEach(entry ->
                            sortedLanguages.put(
                                    entry.getKey(),
                                    entry.getValue()
                            )
                    );

            // 9. Create GitHub analysis DTO
            GitHubAnalysisDTO analysis =
                    new GitHubAnalysisDTO(
                            username,
                            userData.path("name").asText(""),
                            userData.path("html_url").asText(""),
                            userData.path("public_repos").asInt(0),
                            userData.path("followers").asInt(0),
                            totalStars,
                            activeRepositories,
                            sortedLanguages,
                            repositories
                    );

            // 10. Cache successful result
            cache.put(
                    username,
                    new CachedGitHubAnalysis(
                            analysis,
                            System.currentTimeMillis()
                    )
            );

            return analysis;

        } catch (RestClientResponseException e) {

            if (e.getStatusCode().value() == 404) {
                throw new RuntimeException(
                        "GitHub username not found");
            }

            if (e.getStatusCode().value() == 403
                    || e.getStatusCode().value() == 429) {

                throw new RuntimeException(
                        "GitHub API rate limit reached. "
                                + "Please try again later.");
            }

            throw new RuntimeException(
                    "GitHub API error: " + e.getStatusText());

        } catch (Exception e) {

            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }

            throw new RuntimeException(
                    "Unable to analyze GitHub profile: "
                            + e.getMessage());
        }
    }
}


