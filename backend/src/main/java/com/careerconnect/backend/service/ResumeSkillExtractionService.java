package com.careerconnect.backend.service;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.careerconnect.backend.model.Student;
import com.careerconnect.backend.model.StudentProfile;
import com.careerconnect.backend.repository.StudentProfileRepository;
import com.careerconnect.backend.repository.StudentRepository;

@Service
public class ResumeSkillExtractionService {

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private StudentRepository studentRepository;

    private static final Set<String> KNOWN_SKILLS = new LinkedHashSet<>(
            Arrays.asList(
                    "Java",
                    "Python",
                    "C",
                    "C++",
                    "JavaScript",
                    "HTML",
                    "CSS",
                    "React",
                    "Node.js",
                    "Express",
                    "Spring Boot",
                    "SQL",
                    "MySQL",
                    "MongoDB",
                    "DBMS",
                    "DSA",
                    "OOP",
                    "Git",
                    "GitHub",
                    "REST APIs",
                    "AWS",
                    "Azure",
                    "Docker",
                    "Kubernetes",
                    "Machine Learning",
                    "Data Science",
                    "Pandas",
                    "NumPy",
                    "Statistics"
            )
    );

    public String extractSkills(String resumeText) {

        if (resumeText == null || resumeText.trim().isEmpty()) {
            return "";
        }

        String normalizedText = resumeText.toLowerCase();

        Set<String> detectedSkills = new LinkedHashSet<>();

        for (String skill : KNOWN_SKILLS) {

            if (normalizedText.contains(skill.toLowerCase())) {
                detectedSkills.add(skill);
            }
        }

        return detectedSkills.stream()
                .collect(Collectors.joining(", "));
    }

    public StudentProfile extractAndSaveSkills(int studentId) {

        Student student =
                studentRepository.findById(studentId)
                        .orElseThrow(() ->
                                new RuntimeException("Student not found"));

        StudentProfile profile =
                studentProfileRepository.findByStudent(student)
                        .orElseThrow(() ->
                                new RuntimeException("Profile not found"));

        String resumeText = profile.getResumeText();

        String resumeSkills = extractSkills(resumeText);

        profile.setResumeSkills(resumeSkills);

        return studentProfileRepository.save(profile);
    }
}
