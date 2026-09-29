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

                Use this structure:

                CAREER INSIGHTS:
                Explain the student's current career position and
                strongest career direction.

                WHY THIS ROLE:
                Explain why the top recommended role fits the student's
                existing skills.

                CURRENT STRENGTHS:
                Identify the student's strongest existing technical skills.

                SKILLS TO IMPROVE:
                Identify the most important skills the student should
                improve based on the identified gaps.

                LEARNING ROADMAP:
                Create a practical 4-step learning roadmap.
                Start with the highest-priority skill gap.
                Order the steps logically from beginner to practical
                application.

                PROJECT RECOMMENDATION:
                Suggest ONE practical project that combines the student's
                existing skills with the most important missing skill.

                INTERVIEW PREPARATION:
                List the most important technical areas the student
                should prepare for the recommended career direction.

                NEXT ACTION:
                Give ONE clear action the student should take next.

                IMPORTANT RULES:
                - Do not invent skills that the student does not have.
                - Do not invent work experience.
                - Do not invent certifications.
                - Do not change the calculated career match.
                - Do not create unsupported career roles.
                - Use the identified skill gaps as the basis for the roadmap.
                - Keep the response practical for a college student.
                - Prefer specific technical advice over generic motivation.
                - Keep the response concise but useful.
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

            /*
             * Fallback:
             * If Ollama is not running, CareerConnect still returns
             * the deterministic Java-based career analysis.
             */
            return baseAnalysis;
        }
    }

    private record OllamaResponse(
            String response
    ) {
    }
}
