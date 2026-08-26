package com.careerconnect.backend.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.careerconnect.backend.model.CareerAnalysis;
import com.careerconnect.backend.model.JobRole;
import com.careerconnect.backend.model.Student;
import com.careerconnect.backend.model.StudentProfile;
import com.careerconnect.backend.repository.CareerAnalysisRepository;
import com.careerconnect.backend.repository.JobRoleRepository;
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

    @Autowired
    private JobRoleRepository jobRoleRepository;

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

        String studentSkills = profile.getSkills();

        List<JobRole> jobRoles = jobRoleRepository.findAll();

        List<String> recommendedRoles = new ArrayList<>();
        List<String> recommendedSkills = new ArrayList<>();
        List<String> skillGaps = new ArrayList<>();

        /*
         * Compare student's skills with every job role.
         */

        for (JobRole jobRole : jobRoles) {

            String[] requiredSkills =
                    jobRole.getRequiredSkills().split(",");

            int matchedSkills = 0;

            for (String requiredSkill : requiredSkills) {

                String skill = requiredSkill.trim();

                if (studentSkills != null &&
                        studentSkills.toLowerCase()
                                .contains(skill.toLowerCase())) {

                    matchedSkills++;

                } else {

                    if (!skillGaps.contains(skill)) {
                        skillGaps.add(skill);
                    }
                }
            }

            /*
             * Calculate match percentage.
             */

            int matchPercentage =
                    (matchedSkills * 100) / requiredSkills.length;

            /*
             * Recommend roles with at least one
             * matching skill.
             */

            if (matchedSkills > 0) {

                recommendedRoles.add(
                        jobRole.getRoleName()
                                + " (" + matchPercentage + "% Match)"
                );
            }

            /*
             * Store all required skills.
             */

            for (String requiredSkill : requiredSkills) {

                String skill = requiredSkill.trim();

                if (!recommendedSkills.contains(skill)) {
                    recommendedSkills.add(skill);
                }
            }
        }

        /*
         * Sort roles by highest match percentage.
         */

recommendedRoles.sort(
        Comparator.comparingInt(
                (String role) -> extractPercentage(role)
        ).reversed()
);

        String recommendedRolesString =
                String.join(", ", recommendedRoles);

        String recommendedSkillsString =
                String.join(", ", recommendedSkills);

        String skillGapsString =
                String.join(", ", skillGaps);

        CareerAnalysis analysis = new CareerAnalysis();

        analysis.setStudent(student);

        analysis.setRecommendedRoles(
                recommendedRolesString);

        analysis.setRecommendedSkills(
                recommendedSkillsString);

        analysis.setSkillGaps(
                skillGapsString);

        return careerAnalysisRepository.save(analysis);
    }

    /*
     * Extract percentage from:
     *
     * "Java Backend Developer (67% Match)"
     */

    private int extractPercentage(String role) {

        try {

            int start = role.lastIndexOf("(") + 1;

            int end = role.indexOf("%", start);

            return Integer.parseInt(
                    role.substring(start, end)
            );

        } catch (Exception e) {

            return 0;
        }
    }
}