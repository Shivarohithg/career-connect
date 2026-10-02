package com.careerconnect.backend.service;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class OllamaService {

    private final RestClient restClient;

    public OllamaService() {

        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:11434")
                .build();
    }

    /*
     * =========================================================
     * CAREER ANALYSIS AI
     * =========================================================
     */

    public String generateCareerAdvice(
            String studentSkills,
            String recommendedRoles,
            String skillGaps,
            String baseAnalysis) {

        String prompt = """
                You are CareerConnect AI, an intelligent career guidance
                assistant for college students.

                Analyze the student's career information and provide
                personalized, practical guidance.

                ==============================
                STUDENT SKILLS
                ==============================
                %s

                ==============================
                RECOMMENDED CAREER ROLES
                ==============================
                %s

                ==============================
                IDENTIFIED SKILL GAPS
                ==============================
                %s

                ==============================
                SYSTEM ANALYSIS
                ==============================
                %s

                ==============================
                YOUR TASK
                ==============================

                Based ONLY on the information provided above, generate
                personalized career guidance.

                Use exactly this structure:

                CAREER INSIGHTS:
                Explain the student's current career position and strongest
                career direction in 2-3 sentences.

                WHY THIS ROLE:
                Explain why the top recommended role fits the student's
                existing skills.

                CURRENT STRENGTHS:
                List the student's strongest existing technical skills.
                Use one skill per line.

                SKILLS TO IMPROVE:
                List the most important skills the student should improve
                based on the identified skill gaps.
                Use one skill per line.

                LEARNING ROADMAP:
                Create a personalized 4-step learning roadmap.

                For EACH step provide:
                Step number and skill/topic
                Why it should be learned
                What specific concepts to learn
                One practical exercise or mini-task

                Follow this progression:
                Step 1 = highest-priority missing foundation
                Step 2 = next important technical skill
                Step 3 = practical application
                Step 4 = project/interview readiness

                Do NOT simply repeat "Learn <skill>".
                Make every step actionable and specific.

                PROJECT RECOMMENDATION:
                Suggest ONE practical project that combines the student's
                existing skills with the most important missing skill.

                Include:
                Project idea
                Main features
                Technologies to use
                What the project demonstrates

                INTERVIEW PREPARATION:
                List the most important technical areas the student should
                prepare for the recommended career direction.

                NEXT ACTION:
                Give ONE specific action the student should take next.

                IMPORTANT RULES:
                - Use ONLY the information provided.
                - Do not invent skills the student does not have.
                - Do not invent work experience.
                - Do not invent certifications.
                - Do not invent projects that the student already completed.
                - Do not change the calculated career match.
                - Do not create unsupported career roles.
                - Use the identified skill gaps as the basis for the roadmap.
                - Do not add unrelated technologies.
                - Keep the roadmap realistic for a college student.
                - Prefer specific technical advice over generic motivation.
                - Make the roadmap progressively harder.
                - Keep each section concise but useful.
                - Do not include markdown tables.
                """.formatted(
                studentSkills,
                recommendedRoles,
                skillGaps,
                baseAnalysis
        );

        try {

            Map<String, Object> requestBody = Map.of(
                    "model", "qwen2.5:0.5b",
                    "prompt", prompt,
                    "stream", false
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
                    "Ollama AI unavailable: "
                            + e.getMessage()
            );

            return baseAnalysis;
        }
    }


    /*
     * =========================================================
     * JOB-SPECIFIC AI INSIGHT
     * =========================================================
     */

    public String generateJobInsight(
            String prompt,
            String fallback) {

        try {

            Map<String, Object> requestBody = Map.of(
                    "model", "qwen2.5:0.5b",
                    "prompt", prompt,
                    "stream", false
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
                    "Ollama Job AI unavailable: "
                            + e.getMessage()
            );

            /*
             * If Ollama is unavailable, return the deterministic
             * job explanation instead of breaking the application.
             */
            return fallback;
        }
    }


    /*
     * =========================================================
     * OLLAMA RESPONSE
     * =========================================================
     */

    private record OllamaResponse(
            String response
    ) {
    }
}
