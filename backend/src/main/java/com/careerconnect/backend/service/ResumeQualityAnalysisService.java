
package com.careerconnect.backend.service;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.careerconnect.backend.model.Student;
import com.careerconnect.backend.model.StudentProfile;
import com.careerconnect.backend.repository.StudentProfileRepository;
import com.careerconnect.backend.repository.StudentRepository;

@Service
public class ResumeQualityAnalysisService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private OllamaService ollamaService;

    public Map<String, Object> analyzeResume(int studentId) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() ->
                        new RuntimeException("Student not found"));

        StudentProfile profile =
                studentProfileRepository.findByStudent(student)
                        .orElseThrow(() ->
                                new RuntimeException("Profile not found"));

        String resumeText = profile.getResumeText();

        if (resumeText == null || resumeText.trim().isEmpty()) {
            throw new RuntimeException("No resume uploaded");
        }

        String text = resumeText.toLowerCase(Locale.ROOT);

        // Numerical scoring
        int atsScore = calculateATSScore(text);
        int skillsScore = calculateSkillsScore(text);
        int projectScore = calculateProjectScore(text);
        int completenessScore = calculateCompletenessScore(text);

        int overallScore = Math.round(
                (atsScore + skillsScore + projectScore
                        + completenessScore) / 4.0f
        );

        String extractedSkills = profile.getResumeSkills() == null
                ? "Not available"
                : profile.getResumeSkills();

        // AI prompt
        String prompt = """
                You are CareerConnect's Resume Quality Assistant.

                Analyze ONLY the uploaded resume.

                RESUME:
                %s

                EXTRACTED RESUME SKILLS:
                %s

                Return your answer using EXACTLY these four headings:

                STRENGTHS:
                Give three specific strengths supported by the resume.

                AREAS TO IMPROVE:
                Give three specific improvements based on the resume.

                MISSING KEYWORDS:
                Suggest up to five relevant technical keywords
                that are genuinely absent from the resume.

                ACTIONABLE SUGGESTIONS:
                Give three practical actions to improve the resume.

                STRICT RULES:
                - Analyze only the supplied resume.
                - Do not invent skills, projects or qualifications.
                - Never list an existing resume skill as missing.
                - Do not repeat keywords.
                - Do not generate a new resume.
                - Do not generate a resume summary.
                - Do not generate a technical skills list.
                - Do not give generic or repetitive advice.
                - Do not calculate numerical scores.
                - Keep every section concise.
                """.formatted(
                resumeText.length() > 7000
                        ? resumeText.substring(0, 7000)
                        : resumeText,
                extractedSkills
        );

        String fallback = buildFallbackSuggestions();

        // Generate and validate AI suggestions
        String suggestions = ollamaService.generateJobInsight(
                prompt,
                fallback
        );

        suggestions = validateSuggestions(suggestions, text);

        // Final API response
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("overallScore", overallScore);
        result.put("atsCompatibility", atsScore);
        result.put("technicalSkillsScore", skillsScore);
        result.put("projectQualityScore", projectScore);
        result.put("resumeCompletenessScore", completenessScore);
        result.put("aiSuggestions", suggestions);

        return result;
    }

    // ATS compatibility score
    private int calculateATSScore(String text) {

        int score = 0;

        if (Pattern.compile(
                "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
        ).matcher(text).find()) {
            score += 20;
        }

        if (Pattern.compile("\\+?[0-9][0-9\\s-]{8,}")
                .matcher(text).find()) {
            score += 20;
        }

        if (containsAny(text, "education", "academic")) {
            score += 15;
        }

        if (containsAny(text, "skills", "technical skills")) {
            score += 15;
        }

        if (containsAny(text, "projects", "project")) {
            score += 15;
        }

        if (containsAny(text, "experience", "internship")) {
            score += 15;
        }

        return score;
    }

    // Technical skills score
    private int calculateSkillsScore(String text) {

        String[] skills = {
                "java", "python", "c++", "javascript",
                "sql", "react", "spring boot", "node.js",
                "machine learning", "deep learning",
                "tensorflow", "pytorch", "pandas",
                "numpy", "scikit-learn", "docker",
                "aws", "azure", "git", "mongodb",
                "mysql", "nlp", "langchain"
        };

        int count = 0;

        for (String skill : skills) {
            if (text.contains(skill)) {
                count++;
            }
        }

        return Math.min(count * 10, 100);
    }

    // Project quality score
    private int calculateProjectScore(String text) {

        int score = 0;

        if (containsAny(text, "projects", "project")) {
            score += 40;
        }

        if (containsAny(text,
                "developed", "built", "implemented",
                "designed", "created", "engineered")) {
            score += 30;
        }

        if (Pattern.compile(
                "\\b\\d+(?:\\.\\d+)?\\s*(%|ms|seconds|users|records)\\b"
        ).matcher(text).find()) {
            score += 30;
        }

        return score;
    }

    // Resume completeness score
    private int calculateCompletenessScore(String text) {

        int score = 0;

        if (containsAny(text, "summary", "objective", "profile")) {
            score += 20;
        }

        if (containsAny(text, "education", "university", "college")) {
            score += 20;
        }

        if (containsAny(text, "skills", "technical skills")) {
            score += 20;
        }

        if (containsAny(text, "projects", "project")) {
            score += 20;
        }

        if (containsAny(text, "experience", "certifications",
                "achievements", "extracurricular")) {
            score += 20;
        }

        return score;
    }

    // Check whether any keyword exists
    private boolean containsAny(String text, String... keywords) {

        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }

        return false;
    }

    // Validate the AI response
    private String validateSuggestions(
            String suggestions,
            String resumeText) {

        if (suggestions == null || suggestions.trim().isEmpty()) {
            return buildFallbackSuggestions();
        }

        String output = suggestions.toUpperCase(Locale.ROOT);

        String[] headings = {
                "STRENGTHS:",
                "AREAS TO IMPROVE:",
                "MISSING KEYWORDS:",
                "ACTIONABLE SUGGESTIONS:"
        };

        for (String heading : headings) {
            if (!output.contains(heading)) {
                return buildFallbackSuggestions();
            }
        }

        StringBuilder cleaned = new StringBuilder();

        String[] lines = suggestions.split("\\R");

        boolean missingKeywordSection = false;

        for (String line : lines) {

            String trimmed = line.trim();

            if (trimmed.equalsIgnoreCase("MISSING KEYWORDS:")) {
                missingKeywordSection = true;
                cleaned.append(line).append("\n");
                continue;
            }

            if (trimmed.equalsIgnoreCase("ACTIONABLE SUGGESTIONS:")) {
                missingKeywordSection = false;
                cleaned.append(line).append("\n");
                continue;
            }

            if (missingKeywordSection && trimmed.startsWith("-")) {

                String keyword = trimmed.substring(1)
                        .trim()
                        .replaceAll("[*`]", "")
                        .replaceAll("^[0-9]+[.)]\\s*", "");

                if (!keyword.isEmpty()
                        && resumeText.contains(
                                keyword.toLowerCase(Locale.ROOT))) {
                    continue;
                }
            }

            cleaned.append(line).append("\n");
        }

        return cleaned.toString().trim();
    }

    // Fallback if Ollama produces an invalid response
    private String buildFallbackSuggestions() {

        return """
                STRENGTHS:
                - The resume contains technical skills and project information.
                - The resume provides information about the candidate's education.
                - Technical knowledge can be demonstrated through project descriptions.

                AREAS TO IMPROVE:
                - Add measurable outcomes to project descriptions.
                - Improve the clarity and organization of resume sections.
                - Highlight individual contributions in each project.

                MISSING KEYWORDS:
                - Compare the resume with a specific target job description
                  to identify relevant missing keywords.

                ACTIONABLE SUGGESTIONS:
                - Add measurable results wherever evidence is available.
                - Use clear action verbs to describe project contributions.
                - Tailor the resume to the requirements of target roles.
                """;
    }
}



