
package com.careerconnect.backend.dto;

import java.util.List;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

public class MockInterviewDTO {

    // Request to generate interview
    public record GenerateRequest(
            String role
    ) {}

    // Generated question
    public record Question(
            int questionId,
            String question,
            String topic
    ) {}

    // Response after generating interview
    public record InterviewResponse(
            String role,
            List<Question> questions
    ) {}

    // Student answer
    public record Answer(
            int questionId,
            String question,
            String answer
    ) {}

    // Request to evaluate interview
    public record SubmitRequest(
            String role,
            List<Answer> answers
    ) {}

    // Evaluation response
    public record EvaluationResponse(
            String role,
            String evaluation
    ) {}

        // Interview history summary
    public record HistoryItem(
            Long id,
            String role,
            int score,
            java.time.LocalDateTime createdAt
    ) {}

    // Complete previous interview details
    public record HistoryDetail(
            Long id,
            String role,
            int score,
            String answersText,
            String evaluation,
            java.time.LocalDateTime createdAt
    ) {}
}

