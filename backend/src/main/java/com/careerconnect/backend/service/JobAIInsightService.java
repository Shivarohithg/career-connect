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


        /*
         * Ollama is used only as a natural-language explanation layer.
         * CareerConnect remains responsible for the actual facts.
         */

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

                WHY THIS JOB:
                SKILLS YOU ALREADY HAVE:
                SKILLS TO IMPROVE:
                APPLICATION ADVICE:
                INTERVIEW FOCUS:

                Rules:

                Only mention skills from MATCHED SKILLS or
                MISSING SKILLS.

                Do not mention any other technology.

                Do not invent experience.

                Do not invent projects.

                Do not invent certifications.

                Do not invent job requirements.

                Do not add new career roles.

                Do not change the match percentage.

                Do not add any other headings.

                Keep every section very short.

                If MATCHED SKILLS contains Java and Spring Boot,
                you may discuss Java and Spring Boot.

                If MISSING SKILLS contains SQL,
                you may discuss SQL.

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


        /*
         * Validate the response before showing it to the user.
         *
         * Because qwen2.5:0.5b is a very small model, it can sometimes
         * ignore instructions and invent additional information.
         *
         * If that happens, CareerConnect safely uses the deterministic
         * fallback instead.
         */

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


        /*
         * Required sections.
         */

        String[] requiredSections = {

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


        /*
         * Reject model-generated extra sections.
         */

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


        /*
         * Reject unsupported technologies/topics.
         *
         * They are allowed only when they actually appear in the
         * deterministic matched/missing skill lists.
         */

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


        /*
         * Reject exaggerated claims.
         */

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
