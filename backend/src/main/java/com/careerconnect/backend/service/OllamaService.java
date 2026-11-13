
package com.careerconnect.backend.service;

import java.time.Duration;
import java.util.Map;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class OllamaService {

    private final RestClient restClient;

    public OllamaService() {

        SimpleClientHttpRequestFactory factory =
                new SimpleClientHttpRequestFactory();

        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(120));

        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:11434")
                .requestFactory(factory)
                .build();
    }

    /*
     * CAREER ANALYSIS AI
     */

    public String generateCareerAdvice(
            String studentSkills,
            String recommendedRoles,
            String skillGaps,
            String baseAnalysis) {

        String prompt = """
                You are CareerConnect AI, an intelligent career guidance
                assistant for college students.

                STUDENT SKILLS:
                %s

                RECOMMENDED CAREER ROLES:
                %s

                IDENTIFIED SKILL GAPS:
                %s

                SYSTEM ANALYSIS:
                %s

                YOUR TASK:

                Generate personalized career guidance using exactly
                these sections:

                CAREER INSIGHTS:
                Explain the student's current career position.

                WHY THIS ROLE:
                Explain why the recommended role fits the student's skills.

                CURRENT STRENGTHS:
                List existing technical skills.

                SKILLS TO IMPROVE:
                List important missing skills.

                LEARNING ROADMAP:
                Create a practical 4-step learning roadmap.
                Each step must include the topic, reason, concepts
                and one practical exercise.

                PROJECT RECOMMENDATION:
                Suggest one practical project with features,
                technologies and learning outcomes.

                INTERVIEW PREPARATION:
                List important technical areas to prepare.

                NEXT ACTION:
                Give one specific action.

                RULES:
                - Use only the supplied information.
                - Do not invent skills or experience.
                - Do not invent completed projects or certifications.
                - Do not change calculated career matches.
                - Keep recommendations realistic for a college student.
                - Avoid unrelated technologies.
                - Keep responses concise and actionable.
                - Do not use markdown tables.
                """.formatted(
                studentSkills,
                recommendedRoles,
                skillGaps,
                baseAnalysis
        );

        return generate(
                prompt,
                baseAnalysis,
                700
        );
    }

    /*
     * JOB-SPECIFIC AI INSIGHT
     */



    public String generateJobInsight(
            String prompt,
            String fallback) {

        return generate(
                prompt,
                fallback,
                350
        );
    }

        /*
 * CAREER ROADMAP GENERATION
 */

public String generateCareerRoadmap(
        String prompt,
        String fallback) {

    return generate(
            prompt,
            fallback,
            700
    );
}


/*
 * MOCK INTERVIEW EVALUATION
 */

public String generateMockInterviewEvaluation(
        String prompt,
        String fallback) {

    return generate(
            prompt,
            fallback,
            1200
    );
}


    /*
     * COMMON OLLAMA GENERATION METHOD
     */

    private String generate(
            String prompt,
            String fallback,
            int maxTokens) {

        try {

            Map<String, Object> options = Map.of(
                    "temperature", 0.2,
                    "num_predict", maxTokens,
                    "num_ctx", 8192
            );

            Map<String, Object> requestBody = Map.of(
                    "model", "qwen2.5:3b",
                    "prompt", prompt,
                    "stream", false,
                    "keep_alive", "5m",
                    "options", options
            );

            OllamaResponse response =
                    restClient.post()
                            .uri("/api/generate")
                            .body(requestBody)
                            .retrieve()
                            .body(OllamaResponse.class);

            if (response == null ||
                    response.response() == null ||
                    response.response().trim().isEmpty()) {

                throw new RuntimeException(
                        "Ollama returned an empty response."
                );
            }

            return response.response().trim();

        } catch (Exception e) {

            System.err.println(
                    "Ollama AI unavailable: " + e.getMessage()
            );

            return fallback;
        }
    }

    /*
     * OLLAMA RESPONSE
     */

    private record OllamaResponse(
            String response
    ) {
    }
}

