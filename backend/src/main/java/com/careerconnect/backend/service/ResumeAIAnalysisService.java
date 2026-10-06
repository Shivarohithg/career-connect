package com.careerconnect.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.careerconnect.backend.model.Student;
import com.careerconnect.backend.model.StudentProfile;
import com.careerconnect.backend.repository.StudentProfileRepository;
import com.careerconnect.backend.repository.StudentRepository;

@Service
public class ResumeAIAnalysisService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private OllamaService ollamaService;

    public String analyzeResume(int studentId) {

        Student student =
                studentRepository.findById(studentId)
                        .orElseThrow(() ->
                                new RuntimeException("Student not found"));

        StudentProfile profile =
                studentProfileRepository.findByStudent(student)
                        .orElseThrow(() ->
                                new RuntimeException("Profile not found"));

        String resumeText = profile.getResumeText();

        if (resumeText == null ||
                resumeText.trim().isEmpty()) {

            throw new RuntimeException(
                    "No resume uploaded for this student");
        }

        String resumeSkills =
                profile.getResumeSkills() == null
                        ? "None detected"
                        : profile.getResumeSkills();

        if (resumeText.length() > 12000) {
            resumeText =
                    resumeText.substring(0, 12000);
        }

        String prompt = """
                You are the Resume Intelligence AI for CareerConnect.

                Analyze ONLY the uploaded resume provided below.

                IMPORTANT:
                - The uploaded resume belongs to the person being analyzed.
                - Do NOT use the student's CareerConnect account profile.
                - Do NOT use the student's name, branch, CGPA, profile skills,
                  profile projects, or any other account information.
                - Do NOT assume information from outside the resume.
                - Use only information explicitly present in the resume.
                - Do not invent projects, skills, experience, certifications,
                  education, achievements, or career goals.
                - If information is not present, do not claim it exists.

                DETECTED RESUME SKILLS:
                %s

                UPLOADED RESUME:
                %s

                Provide the analysis using exactly these headings:

                RESUME SUMMARY:

                TECHNICAL STRENGTHS:

                CAREER DIRECTION:

                AREAS TO IMPROVE:

                RECOMMENDED NEXT STEPS:

                Keep the analysis concise and resume-specific.
                """.formatted(
                        resumeSkills,
                        resumeText
                );

        String fallback = """
                RESUME SUMMARY:

                The uploaded resume was successfully processed.

                TECHNICAL STRENGTHS:

                %s

                CAREER DIRECTION:

                Based on the technologies and projects found in the
                uploaded resume, explore roles related to those skills.

                AREAS TO IMPROVE:

                Continue strengthening the technologies and concepts
                mentioned in the resume.

                RECOMMENDED NEXT STEPS:

                Build more projects and deepen the skills identified
                in the uploaded resume.
                """.formatted(resumeSkills);

        return ollamaService.generateJobInsight(
                prompt,
                fallback
        );
    }
}