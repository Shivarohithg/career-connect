package com.careerconnect.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.careerconnect.backend.model.CareerAnalysis;
import com.careerconnect.backend.model.Student;
import com.careerconnect.backend.model.StudentProfile;
import com.careerconnect.backend.repository.CareerAnalysisRepository;
import com.careerconnect.backend.repository.StudentProfileRepository;
import com.careerconnect.backend.repository.StudentRepository;
import com.careerconnect.backend.service.CareerRecommendationService;

@RestController
public class CareerRecommendationController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private CareerAnalysisRepository careerAnalysisRepository;

    @Autowired
    private CareerRecommendationService recommendationService;

    @GetMapping("/career-recommendation/{studentId}")
    public String getCareerRecommendation(
            @PathVariable int studentId) {

        Student student =
                studentRepository.findById(studentId)
                        .orElseThrow(() ->
                                new RuntimeException("Student not found"));

        StudentProfile profile =
                studentProfileRepository.findByStudent(student)
                        .orElseThrow(() ->
                                new RuntimeException("Profile not found"));

        CareerAnalysis analysis =
                careerAnalysisRepository.findByStudent(student)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Career analysis not found"));

        return recommendationService.generateCareerAdvice(
                profile.getSkills(),
                analysis.getRecommendedRoles(),
                analysis.getSkillGaps()
        );
    }
}
