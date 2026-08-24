package com.careerconnect.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.careerconnect.backend.model.CareerAnalysis;
import com.careerconnect.backend.model.Student;
import com.careerconnect.backend.model.StudentProfile;
import com.careerconnect.backend.repository.CareerAnalysisRepository;
import com.careerconnect.backend.repository.StudentProfileRepository;
import com.careerconnect.backend.repository.StudentRepository;

@Service
public class CareerAnalysisService {

    @Autowired
    private CareerAnalysisRepository careerAnalysisRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    public CareerAnalysis getAnalysis(int studentId) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() ->
                        new RuntimeException("Student not found"));

        return careerAnalysisRepository.findByStudent(student)
                .orElseThrow(() ->
                        new RuntimeException("Career analysis not found"));
    }

    public CareerAnalysis createAnalysis(int studentId) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() ->
                        new RuntimeException("Student not found"));

        StudentProfile profile =
                studentProfileRepository.findByStudent(student)
                        .orElseThrow(() ->
                                new RuntimeException("Profile not found"));

        if (careerAnalysisRepository.findByStudent(student).isPresent()) {
            throw new RuntimeException(
                    "Career analysis already exists for this student");
        }

        /*
         * Temporary analysis.
         *
         * Later, this section will be replaced by
         * our actual AI analysis.
         */

        String recommendedRoles =
                "Java Developer, Backend Developer, Full Stack Developer";

        String recommendedSkills =
                "Java, Spring Boot, REST APIs, SQL, React";

        String skillGaps =
                "Docker, Cloud Deployment, Microservices";

        CareerAnalysis analysis = new CareerAnalysis();

        analysis.setStudent(student);
        analysis.setRecommendedRoles(recommendedRoles);
        analysis.setRecommendedSkills(recommendedSkills);
        analysis.setSkillGaps(skillGaps);

        return careerAnalysisRepository.save(analysis);
    }
}
