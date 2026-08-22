package com.careerconnect.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.careerconnect.backend.model.Student;
import com.careerconnect.backend.model.StudentProfile;
import com.careerconnect.backend.repository.StudentProfileRepository;
import com.careerconnect.backend.repository.StudentRepository;

@Service
public class StudentProfileService {

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private StudentRepository studentRepository;

    public StudentProfile getProfile(int studentId) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() ->
                        new RuntimeException("Student not found"));

        return studentProfileRepository.findByStudent(student)
                .orElseThrow(() ->
                        new RuntimeException("Profile not found"));
    }

    public StudentProfile createProfile(
            int studentId,
            String skills,
            String projects,
            String githubUsername,
            String leetcodeUsername,
            String hackerrankUsername,
            String resumePath) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() ->
                        new RuntimeException("Student not found"));

        if (studentProfileRepository.findByStudent(student).isPresent()) {
            throw new RuntimeException(
                    "Profile already exists for this student");
        }

        StudentProfile profile = new StudentProfile();

        profile.setStudent(student);
        profile.setSkills(skills);
        profile.setProjects(projects);
        profile.setGithubUsername(githubUsername);
        profile.setLeetcodeUsername(leetcodeUsername);
        profile.setHackerrankUsername(hackerrankUsername);
        profile.setResumePath(resumePath);

        return studentProfileRepository.save(profile);
    }
}