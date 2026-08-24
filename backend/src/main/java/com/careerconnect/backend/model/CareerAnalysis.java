package com.careerconnect.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "career_analysis")
public class CareerAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne
    @JoinColumn(name = "student_id", nullable = false, unique = true)
    private Student student;

    private String recommendedRoles;

    private String recommendedSkills;

    private String skillGaps;

    public CareerAnalysis() {

    }

    public CareerAnalysis(
            Student student,
            String recommendedRoles,
            String recommendedSkills,
            String skillGaps) {

        this.student = student;
        this.recommendedRoles = recommendedRoles;
        this.recommendedSkills = recommendedSkills;
        this.skillGaps = skillGaps;
    }

    public int getId() {
        return id;
    }

    public Student getStudent() {
        return student;
    }

    public String getRecommendedRoles() {
        return recommendedRoles;
    }

    public String getRecommendedSkills() {
        return recommendedSkills;
    }

    public String getSkillGaps() {
        return skillGaps;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public void setRecommendedRoles(String recommendedRoles) {
        this.recommendedRoles = recommendedRoles;
    }

    public void setRecommendedSkills(String recommendedSkills) {
        this.recommendedSkills = recommendedSkills;
    }

    public void setSkillGaps(String skillGaps) {
        this.skillGaps = skillGaps;
    }
}