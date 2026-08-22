package com.careerconnect.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "student_profiles")
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne
    @JoinColumn(name = "student_id", nullable = false, unique = true)
    private Student student;

    private String skills;

    private String projects;

    private String githubUsername;

    private String leetcodeUsername;

    private String hackerrankUsername;

    private String resumePath;

    public StudentProfile() {

    }

    public StudentProfile(
            Student student,
            String skills,
            String projects,
            String githubUsername,
            String leetcodeUsername,
            String hackerrankUsername,
            String resumePath) {

        this.student = student;
        this.skills = skills;
        this.projects = projects;
        this.githubUsername = githubUsername;
        this.leetcodeUsername = leetcodeUsername;
        this.hackerrankUsername = hackerrankUsername;
        this.resumePath = resumePath;
    }

    public int getId() {
        return id;
    }

    public Student getStudent() {
        return student;
    }

    public String getSkills() {
        return skills;
    }

    public String getProjects() {
        return projects;
    }

    public String getGithubUsername() {
        return githubUsername;
    }

    public String getLeetcodeUsername() {
        return leetcodeUsername;
    }

    public String getHackerrankUsername() {
        return hackerrankUsername;
    }

    public String getResumePath() {
        return resumePath;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public void setProjects(String projects) {
        this.projects = projects;
    }

    public void setGithubUsername(String githubUsername) {
        this.githubUsername = githubUsername;
    }

    public void setLeetcodeUsername(String leetcodeUsername) {
        this.leetcodeUsername = leetcodeUsername;
    }

    public void setHackerrankUsername(String hackerrankUsername) {
        this.hackerrankUsername = hackerrankUsername;
    }

    public void setResumePath(String resumePath) {
        this.resumePath = resumePath;
    }
}
