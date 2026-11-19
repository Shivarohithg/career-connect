
package com.careerconnect.backend.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.careerconnect.backend.dto.LeetCodeAnalysisDTO;
import com.careerconnect.backend.model.Student;
import com.careerconnect.backend.model.StudentProfile;
import com.careerconnect.backend.repository.StudentProfileRepository;
import com.careerconnect.backend.repository.StudentRepository;

import tools.jackson.databind.JsonNode;

@Service
public class LeetCodeAnalysisService {

    private final RestClient restClient;
    private final StudentRepository studentRepository;
    private final StudentProfileRepository studentProfileRepository;

    private static final long CACHE_DURATION_MS = 30 * 60 * 1000L;

    private final ConcurrentMap<String, CachedLeetCodeAnalysis> cache =
            new ConcurrentHashMap<>();

    private record CachedLeetCodeAnalysis(
            LeetCodeAnalysisDTO data,
            long cachedAt
    ) {}

    private static final String PROFILE_QUERY = """
    query userProfile($username: String!) {
        matchedUser(username: $username) {
            username
            profile {
                ranking
            }
            submitStats: submitStatsGlobal {
                acSubmissionNum {
                    difficulty
                    count
                }
            }
            userCalendar {
                activeYears
                streak
                totalActiveDays
                submissionCalendar
            }
        }
        userContestRanking(username: $username) {
            attendedContestsCount
            rating
            globalRanking
        }
    }
    """;

    public LeetCodeAnalysisService(
            StudentRepository studentRepository,
            StudentProfileRepository studentProfileRepository) {

        this.restClient = RestClient.builder()
                .baseUrl("https://leetcode.com")
                .defaultHeader(
                        HttpHeaders.ACCEPT,
                        "application/json")
                .defaultHeader(
                        HttpHeaders.USER_AGENT,
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/130.0.0.0 Safari/537.36")
                .defaultHeader(
                        HttpHeaders.REFERER,
                        "https://leetcode.com/")
                .build();

        this.studentRepository = studentRepository;
        this.studentProfileRepository = studentProfileRepository;
    }

    public LeetCodeAnalysisDTO analyzeStudent(int studentId) {

        // 1. Find student
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() ->
                        new RuntimeException("Student not found"));

        // 2. Find student profile
        StudentProfile profile = studentProfileRepository
                .findByStudent(student)
                .orElseThrow(() ->
                        new RuntimeException("Student profile not found"));

        // 3. Get saved LeetCode username
        String savedUsername = profile.getLeetcodeUsername();

        if (savedUsername == null || savedUsername.isBlank()) {
            throw new RuntimeException(
                    "Please add your LeetCode username to your profile");
        }

        String username = savedUsername.trim();

        // 4. Check cache
        CachedLeetCodeAnalysis cached = cache.get(username);

        if (cached != null) {
            if (System.currentTimeMillis() - cached.cachedAt()
                    < CACHE_DURATION_MS) {
                return cached.data();
            }

            cache.remove(username, cached);
        }

        try {

            // 5. Prepare GraphQL request
            Map<String, Object> requestBody = Map.of(
                    "query", PROFILE_QUERY,
                    "variables", Map.of("username", username)
            );

            // 6. Call LeetCode GraphQL API
            JsonNode response = restClient.post()
                    .uri("/graphql")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null) {
                throw new RuntimeException(
                        "Empty response received from LeetCode");
            }

            // TEMPORARY DEBUG LOG
            System.out.println("LEETCODE RAW RESPONSE:");
            System.out.println(response);

            // 7. Check GraphQL errors
            JsonNode errors = response.path("errors");

            if (errors.isArray() && !errors.isEmpty()) {
                String message = errors.get(0)
                        .path("message")
                        .asText("LeetCode GraphQL error");

                throw new RuntimeException(
                        "LeetCode GraphQL error: " + message);
            }

            // 8. Check whether user exists
            JsonNode userData = response
                    .path("data")
                    .path("matchedUser");

            if (userData.isMissingNode() || userData.isNull()) {
                throw new RuntimeException(
                        "LeetCode username not found: " + username);
            }

            // 9. Read solved statistics
            JsonNode solvedStats = userData
                    .path("submitStats")
                    .path("acSubmissionNum");

            if (!solvedStats.isArray() || solvedStats.isEmpty()) {
                throw new RuntimeException(
                        "LeetCode did not return valid problem-solving statistics.");
            }

            int easySolved = 0;
            int mediumSolved = 0;
            int hardSolved = 0;
            int totalSolved = 0;

            boolean easyFound = false;
            boolean mediumFound = false;
            boolean hardFound = false;

            for (JsonNode item : solvedStats) {

                String difficulty =
                        item.path("difficulty").asText("");

                int count = item.path("count").asInt(0);

                switch (difficulty.toUpperCase()) {

                    case "ALL" -> totalSolved = count;

                    case "EASY" -> {
                        easySolved = count;
                        easyFound = true;
                    }

                    case "MEDIUM" -> {
                        mediumSolved = count;
                        mediumFound = true;
                    }

                    case "HARD" -> {
                        hardSolved = count;
                        hardFound = true;
                    }

                    default -> {
                        // Ignore unknown categories
                    }
                }
            }

            if (!easyFound || !mediumFound || !hardFound) {
                throw new RuntimeException(
                        "LeetCode returned incomplete difficulty statistics.");
            }

            // Use the sum if the API does not provide All
            if (totalSolved == 0) {
                totalSolved =
                        easySolved + mediumSolved + hardSolved;
            }

            // 10. Read global ranking
            int rawRanking = userData
        .path("profile")
        .path("ranking")
        .asInt(0);

Integer globalRanking =
        (rawRanking > 0 && rawRanking < 5000001)
                ? rawRanking
                : null;

            // 11. Read activity information
            // 11. Read activity information
JsonNode calendar = userData.path("userCalendar");

int totalActiveDays =
        calendar.path("totalActiveDays").asInt(0);

int currentStreak =
        calendar.path("streak").asInt(0);

// TEMPORARY DEBUG OUTPUT
System.out.println("LEETCODE CALENDAR DATA:");
System.out.println(calendar);

System.out.println("LEETCODE PROFILE RANKING:");
System.out.println(
        userData.path("profile").path("ranking")
);

            // 12. Read contest information
            JsonNode contest = response
                    .path("data")
                    .path("userContestRanking");

            Integer contestsAttended = null;
            Double contestRating = null;
            Integer contestGlobalRanking = null;

            if (!contest.isMissingNode() && !contest.isNull()) {

                contestsAttended = contest
                        .path("attendedContestsCount")
                        .asInt(0);

                contestRating = contest
                        .path("rating")
                        .asDouble(0.0);

                contestGlobalRanking = contest
                        .path("globalRanking")
                        .asInt(0);
            }

            // 13. Create DTO
            LeetCodeAnalysisDTO analysis =
                    new LeetCodeAnalysisDTO(
                            userData.path("username").asText(username),
                            totalSolved,
                            easySolved,
                            mediumSolved,
                            hardSolved,
                            globalRanking,
                            totalActiveDays,
                            currentStreak,
                            contestsAttended,
                            contestRating,
                            contestGlobalRanking
                    );

            // 14. Cache successful result
            cache.put(
                    username,
                    new CachedLeetCodeAnalysis(
                            analysis,
                            System.currentTimeMillis())
            );

            return analysis;

        } catch (RestClientResponseException e) {

            throw new RuntimeException(
                    "LeetCode API returned HTTP "
                            + e.getStatusCode().value()
                            + ". Please try again later.");

        } catch (RuntimeException e) {

            throw e;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Unable to analyze LeetCode profile: "
                            + e.getMessage());
        }
    }
}

