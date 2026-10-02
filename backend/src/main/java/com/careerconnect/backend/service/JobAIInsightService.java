package com.careerconnect.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.careerconnect.backend.model.Job;
import com.careerconnect.backend.model.Student;
import com.careerconnect.backend.model.StudentProfile;
import com.careerconnect.backend.repository.JobRepository;
import com.careerconnect.backend.repository.StudentProfileRepository;
import com.careerconnect.backend.repository.StudentRepository;

@Service
public class JobAIInsightService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private OllamaService ollamaService;

    public String generateJobInsight(
            int studentId,
            int jobId,
            String matchedRole,
            int matchPercentage,
            List<String> matchedSkills,
            List<String> missingSkills) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() ->
                        new RuntimeException("Student not found"));

        StudentProfile profile =
                studentProfileRepository.findByStudent(student)
                        .orElseThrow(() ->
                                new RuntimeException("Profile not found"));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() ->
                        new RuntimeException("Job not found"));

        String prompt = """
                You are CareerConnect AI.

                Analyze whether this job is suitable for the student.

                ==============================
                STUDENT
                ==============================
                Name: %s
                Branch: %s
                CGPA: %s

                ==============================
                STUDENT SKILLS
                ==============================
                %s

                ==============================
                JOB
                ==============================
                Job Title: %s
                Company: %s
                Location: %s

                ==============================
                MATCH ANALYSIS
                ==============================
                Matched Career Role: %s
                Match Percentage: %d%%
                Matched Skills: %s
                Missing Skills: %s

                ==============================
                YOUR TASK
                ==============================

                Generate a concise, personalized explanation of this job.

                Use exactly this structure:

                WHY THIS JOB:
                Explain why this job matches the student's current
                skills and career direction.

                SKILLS YOU ALREADY HAVE:
                Explain the strongest matching skills.

                SKILLS TO IMPROVE:
                Explain the most important missing skills.

                APPLICATION ADVICE:
                Give practical advice about what the student should
                strengthen before or while applying.

                INTERVIEW FOCUS:
                List the technical areas the student should prepare
                for if applying for this job.

                IMPORTANT RULES:
                - Use ONLY the information provided.
                - Do not invent student skills.
                - Do not invent experience.
                - Do not invent certifications.
                - Do not change the calculated match percentage.
                - Do not invent job requirements.
                - Keep the advice realistic for a college student.
                - Be specific and practical.
                - Keep the response concise.
                """.formatted(
                student.getName(),
                student.getBranch(),
                student.getCgpa(),
                profile.getSkills(),
                job.getTitle(),
                job.getCompany(),
                job.getLocation(),
                matchedRole,
                matchPercentage,
                String.join(", ", matchedSkills),
                String.join(", ", missingSkills)
        );

        String fallback = """
                WHY THIS JOB:
                This job matches your current career direction based
                on the calculated role and skill match.

                SKILLS YOU ALREADY HAVE:
                %s

                SKILLS TO IMPROVE:
                %s

                APPLICATION ADVICE:
                Strengthen the missing skills while continuing to build
                practical projects related to this career direction.

                INTERVIEW FOCUS:
                Prepare the matched skills and the identified skill gaps.
                """.formatted(
                String.join(", ", matchedSkills),
                String.join(", ", missingSkills)
        );

        return ollamaService.generateJobInsight(prompt, fallback);
    }
}
