package com.careerconnect.backend.service;

import java.util.List;
import java.util.Locale;

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

        String matchedSkillsText =
                matchedSkills == null || matchedSkills.isEmpty()
                        ? "None"
                        : String.join(", ", matchedSkills);

        String missingSkillsText =
                missingSkills == null || missingSkills.isEmpty()
                        ? "None"
                        : String.join(", ", missingSkills);

        String prompt = """
                You are an explanation assistant for CareerConnect.

                Use ONLY these facts:

                CAREER ROLE:
                %s

                MATCH PERCENTAGE:
                %d%%

                MATCHED SKILLS:
                %s

                MISSING SKILLS:
                %s

                JOB TITLE:
                %s

                COMPANY:
                %s

                LOCATION:
                %s

                Your response must contain exactly these headings:

                AI APPLICATION DECISION:
                WHY THIS JOB:
                SKILLS YOU ALREADY HAVE:
                SKILLS TO IMPROVE:
                APPLICATION ADVICE:
                INTERVIEW FOCUS:

                For AI APPLICATION DECISION:
                - Start with either "RECOMMEND APPLYING" or
                  "APPLY AFTER IMPROVING SKILLS".
                - Explain the decision using only the match percentage,
                  matched skills and missing skills.
                - Do not invent job requirements.

                Rules:

                Only mention skills from MATCHED SKILLS or MISSING SKILLS.

                Do not mention any other technology.

                Do not invent experience.

                Do not invent projects.

                Do not invent certifications.

                Do not invent job requirements.

                Do not add new career roles.

                Do not change the match percentage.

                Do not add any other headings.

                Keep every section very short.

                Do not introduce React, Python, JavaScript, Docker,
                AWS, Azure, Git, Maven, JPA, Spring Data, DSA,
                communication skills, problem-solving skills, or any
                other technology unless it appears in the supplied
                matched or missing skill lists.
                """.formatted(
                matchedRole,
                matchPercentage,
                matchedSkillsText,
                missingSkillsText,
                job.getTitle(),
                job.getCompany(),
                job.getLocation()
        );

        String fallback = createDeterministicFallback(
                matchedRole,
                matchPercentage,
                matchedSkills,
                missingSkills
        );

        String aiResponse =
                ollamaService.generateJobInsight(
                        prompt,
                        fallback
                );

        if (!isValidAIResponse(
                aiResponse,
                matchedSkills,
                missingSkills
        )) {

            System.out.println(
                    "Ollama response failed validation."
            );

            System.out.println(
                    "Using deterministic CareerConnect fallback."
            );

            return fallback;
        }

        return aiResponse;
    }

    private String createDeterministicFallback(
            String matchedRole,
            int matchPercentage,
            List<String> matchedSkills,
            List<String> missingSkills) {

        String matchedText =
                matchedSkills == null || matchedSkills.isEmpty()
                        ? "- None"
                        : createBulletList(matchedSkills);

        String missingText =
                missingSkills == null || missingSkills.isEmpty()
                        ? "- No major skill gaps identified."
                        : createBulletList(missingSkills);

        String applicationDecision;

        if (matchPercentage >= 70 && 
                (missingSkills == null || missingSkills.isEmpty())) {

            applicationDecision =
                    "RECOMMEND APPLYING - Your current skills cover "
                    + "the identified requirements for this role.";

        } else if (matchPercentage >= 60) {

            applicationDecision =
                    "RECOMMEND APPLYING - Your current match is "
                    + matchPercentage
                    + "%. You have relevant matched skills, while "
                    + "the remaining gaps can be improved.";

        } else {

            applicationDecision =
                    "APPLY AFTER IMPROVING SKILLS - Your current match "
                    + "is "
                    + matchPercentage
                    + "%. Focus on the identified missing skills before "
                    + "prioritizing this role.";
        }

        String applicationAdvice;

        if (missingSkills == null ||
                missingSkills.isEmpty()) {

            applicationAdvice =
                    "Your current matched skills cover the identified "
                    + "skill requirements. Focus on revising these skills "
                    + "and preparing for the application.";

        } else {

            applicationAdvice =
                    "Focus on improving the identified missing skills "
                    + "while continuing to strengthen your matched skills.";
        }

        String interviewFocus;

        if (matchedSkills == null ||
                matchedSkills.isEmpty()) {

            interviewFocus =
                    "Prepare the identified skill gaps for this role.";

        } else if (missingSkills == null ||
                missingSkills.isEmpty()) {

            interviewFocus =
                    "Revise your matched skills thoroughly for the interview.";

        } else {

            interviewFocus =
                    "Revise the matched skills and prepare the identified "
                    + "skill gaps for the interview.";
        }

        return """
                AI APPLICATION DECISION:
                %s

                WHY THIS JOB:
                The %s role matches your calculated career direction.
                Your current calculated match is %d%% based on the identified skills.

                SKILLS YOU ALREADY HAVE:
                %s

                SKILLS TO IMPROVE:
                %s

                APPLICATION ADVICE:
                %s

                INTERVIEW FOCUS:
                %s
                """.formatted(
                applicationDecision,
                matchedRole,
                matchPercentage,
                matchedText,
                missingText,
                applicationAdvice,
                interviewFocus
        );
    }

    private String createBulletList(
            List<String> skills) {

        StringBuilder result =
                new StringBuilder();

        for (String skill : skills) {

            if (skill != null &&
                    !skill.trim().isEmpty()) {

                result.append("- ")
                        .append(skill.trim())
                        .append("\n");
            }
        }

        return result.toString().trim();
    }

    private boolean isValidAIResponse(
            String response,
            List<String> matchedSkills,
            List<String> missingSkills) {

        if (response == null ||
                response.trim().isEmpty()) {

            return false;
        }

        String lowerResponse =
                response.toLowerCase(Locale.ROOT);

        String[] requiredSections = {
                "ai application decision:",
                "why this job:",
                "skills you already have:",
                "skills to improve:",
                "application advice:",
                "interview focus:"
        };

        for (String section : requiredSections) {

            if (!lowerResponse.contains(section)) {

                return false;
            }
        }

        String[] forbiddenHeadings = {
                "important rules:",
                "final rules:",
                "explanation of the calculated career match:",
                "slot 1:",
                "slot 2:",
                "slot 3:",
                "slot 4:",
                "slot 5:"
        };

        for (String heading : forbiddenHeadings) {

            if (lowerResponse.contains(heading)) {

                return false;
            }
        }

        StringBuilder allowedSkills =
                new StringBuilder();

        if (matchedSkills != null) {

            for (String skill : matchedSkills) {

                if (skill != null) {

                    allowedSkills
                            .append(" ")
                            .append(
                                    skill.toLowerCase(
                                            Locale.ROOT
                                    )
                            )
                            .append(" ");
                }
            }
        }

        if (missingSkills != null) {

            for (String skill : missingSkills) {

                if (skill != null) {

                    allowedSkills
                            .append(" ")
                            .append(
                                    skill.toLowerCase(
                                            Locale.ROOT
                                    )
                            )
                            .append(" ");
                }
            }
        }

        String[] restrictedTerms = {
                "react",
                "react native",
                "javascript",
                "typescript",
                "python",
                "node.js",
                "nodejs",
                "mongodb",
                "docker",
                "aws",
                "azure",
                "google cloud",
                "gcp",
                "kubernetes",
                "spring cloud",
                "spring security",
                "microservices",
                "html",
                "css",
                "git",
                "github",
                "jenkins",
                "kafka",
                "redis",
                "angular",
                "vue",
                "flutter",
                "android",
                "machine learning",
                "artificial intelligence",
                "data science",
                "data analytics",
                "tensorflow",
                "pytorch",
                "maven",
                "jpa",
                "spring data",
                "communication skills",
                "problem-solving skills",
                "problem solving skills",
                "professional experience",
                "work experience",
                "certification",
                "certifications"
        };

        for (String term : restrictedTerms) {

            if (lowerResponse.contains(term)) {

                boolean allowed =
                        allowedSkills
                                .toString()
                                .contains(term);

                if (!allowed) {

                    return false;
                }
            }
        }

        String[] exaggeratedPhrases = {
                "perfect fit",
                "perfectly suited",
                "perfect match",
                "expert in",
                "highly skilled",
                "strong experience",
                "extensive experience",
                "excellent candidate",
                "proficient in all"
        };

        for (String phrase : exaggeratedPhrases) {

            if (lowerResponse.contains(phrase)) {

                return false;
            }
        }

        return true;
    }
}
