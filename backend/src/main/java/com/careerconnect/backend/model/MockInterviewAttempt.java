package com.careerconnect.backend.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "mock_interview_attempts")
public class MockInterviewAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private int score;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String answersText;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String evaluation;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public MockInterviewAttempt() {
    }

    public MockInterviewAttempt(
            Student student,
            String role,
            int score,
            String answersText,
            String evaluation) {

        this.student = student;
        this.role = role;
        this.score = score;
        this.answersText = answersText;
        this.evaluation = evaluation;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Student getStudent() {
        return student;
    }

    public String getRole() {
        return role;
    }

    public int getScore() {
        return score;
    }

    public String getAnswersText() {
        return answersText;
    }

    public String getEvaluation() {
        return evaluation;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
