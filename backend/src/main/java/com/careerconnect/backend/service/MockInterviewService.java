package com.careerconnect.backend.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.careerconnect.backend.dto.MockInterviewDTO;
import com.careerconnect.backend.model.Student;
import com.careerconnect.backend.model.StudentProfile;
import com.careerconnect.backend.repository.StudentProfileRepository;
import com.careerconnect.backend.repository.StudentRepository;
import com.careerconnect.backend.model.MockInterviewAttempt;
import com.careerconnect.backend.repository.MockInterviewAttemptRepository;

@Service
public class MockInterviewService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private OllamaService ollamaService;

    @Autowired
private MockInterviewAttemptRepository mockInterviewAttemptRepository;

    /*
     * GENERATE MOCK INTERVIEW
     */

    public MockInterviewDTO.InterviewResponse generateInterview(
            int studentId,
            String role) {

        if (role == null || role.isBlank()) {
            throw new RuntimeException("Please select a career role.");
        }

Student student = studentRepository.findById(studentId)
        .orElseThrow(() ->
                new RuntimeException("Student not found"));

        StudentProfile profile =
                studentProfileRepository.findByStudent(student)
                        .orElseThrow(() ->
                                new RuntimeException("Profile not found"));

        String resumeText = profile.getResumeText();

        String resumeSkills =
                profile.getResumeSkills() == null
                        ? ""
                        : profile.getResumeSkills();

        String profileSkills =
                profile.getSkills() == null
                        ? ""
                        : profile.getSkills();

        String projects =
                profile.getProjects() == null
                        ? ""
                        : profile.getProjects();

        if (resumeText == null || resumeText.isBlank()) {
            resumeText = "Resume not available";
        }

        if (resumeText.length() > 6000) {
            resumeText = resumeText.substring(0, 6000);
        }

        String roleGuidance = getRoleGuidance(role);

        String prompt = """
                You are an expert technical interviewer at a software
                engineering company.

                Your task is to create a REAL technical interview.

                SELECTED JOB ROLE:
                %s

                ROLE-SPECIFIC TECHNICAL EXPECTATIONS:
                %s

                CANDIDATE RESUME:
                %s

                RESUME SKILLS:
                %s

                PROFILE SKILLS:
                %s

                PROJECTS:
                %s

                IMPORTANT INSTRUCTIONS:

                1. Generate exactly FIVE technical interview questions.

                2. Every question MUST be directly relevant to the
                   selected job role.

                3. Do not generate generic questions such as:
                   - Explain fundamental concepts.
                   - What are your strengths?
                   - Why should we hire you?
                   - Explain your experience.

                4. Do not ask Java questions for a Python Developer.
                   Do not ask Python questions for a Java Developer.

                5. Do not introduce unrelated technologies.

                6. Prioritize technologies explicitly present in the
                   resume and profile, provided they are relevant
                   to the selected role.

                7. If a technology is not mentioned in the resume,
                   you may ask a basic role-relevant knowledge question,
                   but do not claim the candidate has experience with it.

                8. Create a balanced interview:
                   - Questions 1 and 2: Technical fundamentals.
                   - Questions 3 and 4: Practical technical problems.
                   - Question 5: Project-based or real-world scenario.

                9. Questions must test actual technical understanding.

                10. Questions must be clear, specific and answerable.

                RETURN EXACTLY THIS FORMAT:

                Q1: [Specific technical question]
                TOPIC: [Technology or concept]

                Q2: [Specific technical question]
                TOPIC: [Technology or concept]

                Q3: [Specific technical question]
                TOPIC: [Technology or concept]

                Q4: [Specific technical question]
                TOPIC: [Technology or concept]

                Q5: [Specific technical question]
                TOPIC: [Technology or concept]

                Return only the five questions.
                Do not include answers or explanations.
                """.formatted(
                role,
                roleGuidance,
                resumeText,
                resumeSkills,
                profileSkills,
                projects
        );

        String fallback = getFallbackQuestions(role);

        String aiResponse =
                ollamaService.generateCareerRoadmap(
                        prompt,
                        fallback
                );

        List<MockInterviewDTO.Question> questions =
                parseQuestions(aiResponse);

        if (questions.size() != 5) {
            questions = parseQuestions(fallback);
        }

        return new MockInterviewDTO.InterviewResponse(
                role,
                questions
        );
    }

    /*
     * ROLE-SPECIFIC GUIDANCE
     */

    private String getRoleGuidance(String role) {

        String r = role.toLowerCase();

        if (r.contains("java")) {
            return """
                    Java programming, OOP, inheritance, polymorphism,
                    abstraction, interfaces, exception handling,
                    collections, ArrayList, HashMap, multithreading,
                    Java 8 features, streams, Spring Boot, REST APIs,
                    dependency injection, Spring Data JPA and MySQL.
                    Focus on the technologies relevant to the candidate.
                    """;
        }

        if (r.contains("python")) {
            return """
                    Python syntax, data types, lists, tuples, dictionaries,
                    sets, functions, OOP, exception handling, file handling,
                    decorators, generators, list comprehensions,
                    modules, packages, virtual environments,
                    Python libraries, APIs and database connectivity.
                    Focus on the technologies relevant to the candidate.
                    """;
        }

        if (r.contains("frontend") ||
                r.contains("front-end") ||
                r.contains("web developer")) {

            return """
                    HTML5, CSS3, JavaScript, DOM manipulation,
                    events, responsive design, ES6, promises,
                    async/await, React components, props, state,
                    hooks, React Router, Axios and API integration.
                    Focus on the technologies relevant to the candidate.
                    """;
        }

        if (r.contains("full stack") ||
                r.contains("full-stack")) {

            return """
                    HTML, CSS, JavaScript, React, components,
                    hooks, Java or Node.js backend development,
                    REST APIs, HTTP methods, authentication,
                    database design, SQL, CRUD operations,
                    API integration and frontend-backend communication.
                    Focus on the technologies relevant to the candidate.
                    """;
        }

        if (r.contains("backend") ||
                r.contains("back-end")) {

            return """
                    Backend programming, REST APIs, HTTP methods,
                    server-side validation, authentication,
                    authorization, exception handling, database design,
                    SQL, transactions, ORM, API security and performance.
                    Focus on the technologies relevant to the candidate.
                    """;
        }

        if (r.contains("data analyst")) {
            return """
                    Python, SQL, data cleaning, data manipulation,
                    Pandas, NumPy, data visualization, statistics,
                    exploratory data analysis, Excel and interpreting
                    datasets. Focus on the technologies relevant to
                    the candidate.
                    """;
        }

        if (r.contains("software engineer") ||
                r.contains("software developer")) {

            return """
                    Programming fundamentals, OOP, data structures,
                    algorithms, time and space complexity,
                    problem solving, debugging, databases,
                    software design, REST APIs, Git and testing.
                    Focus on the technologies relevant to the candidate.
                    """;
        }

        return """
                Ask specific technical questions based on the selected
                job role, its required programming languages, frameworks,
                tools, databases and practical responsibilities.
                Do not ask generic HR questions.
                """;
    }
        /*
     * ROLE-SPECIFIC FALLBACK QUESTIONS
     */

    private String getFallbackQuestions(String role) {

        String r = role.toLowerCase();

        if (r.contains("java")) {

            return """
                    Q1: Explain the difference between an abstract class and an interface in Java.
                    TOPIC: Java OOP

                    Q2: How does HashMap store key-value pairs and handle hash collisions internally?
                    TOPIC: Java Collections

                    Q3: Write a Java program to find the first non-repeating character in a string.
                    TOPIC: Java Problem Solving

                    Q4: Explain how exception handling works using try, catch and finally in Java.
                    TOPIC: Java Exception Handling

                    Q5: How would you create a REST API in Spring Boot and connect it to a MySQL database using Spring Data JPA?
                    TOPIC: Spring Boot and Database Integration
                    """;
        }

        if (r.contains("python")) {

            return """
                    Q1: What is the difference between a list, tuple, set and dictionary in Python?
                    TOPIC: Python Data Structures

                    Q2: Explain the difference between shallow copy and deep copy in Python.
                    TOPIC: Python Concepts

                    Q3: Write a Python program to find the frequency of each character in a string.
                    TOPIC: Python Problem Solving

                    Q4: Explain decorators and generators in Python with practical examples.
                    TOPIC: Python Advanced Concepts

                    Q5: How would you build a Python REST API and connect it to a database?
                    TOPIC: Python Backend Development
                    """;
        }

        if (r.contains("frontend") ||
                r.contains("front-end") ||
                r.contains("web developer")) {

            return """
                    Q1: Explain the difference between let, const and var in JavaScript.
                    TOPIC: JavaScript

                    Q2: What is the difference between Flexbox and CSS Grid?
                    TOPIC: CSS

                    Q3: Explain how useState and useEffect work in React with examples.
                    TOPIC: React Hooks

                    Q4: How would you fetch data from a REST API using Axios in React?
                    TOPIC: API Integration

                    Q5: How would you build a responsive React page with reusable components and form validation?
                    TOPIC: Frontend Application Development
                    """;
        }

        if (r.contains("full stack") ||
                r.contains("full-stack")) {

            return """
                    Q1: Explain how a React frontend communicates with a Spring Boot or Node.js backend.
                    TOPIC: Full Stack Architecture

                    Q2: What are REST APIs and how do GET, POST, PUT and DELETE differ?
                    TOPIC: REST APIs

                    Q3: How would you implement CRUD operations using a backend API and MySQL?
                    TOPIC: Database Integration

                    Q4: How would you implement authentication and protect private routes in a web application?
                    TOPIC: Authentication

                    Q5: Describe how you would design a full-stack job application platform with React, backend APIs and MySQL.
                    TOPIC: Full Stack Project Design
                    """;
        }

        if (r.contains("backend") ||
                r.contains("back-end")) {

            return """
                    Q1: Explain how REST APIs work and describe the purpose of HTTP methods.
                    TOPIC: REST API Fundamentals

                    Q2: What is the difference between authentication and authorization?
                    TOPIC: Backend Security

                    Q3: How would you implement CRUD operations using a backend framework?
                    TOPIC: Backend Development

                    Q4: Explain database indexing and how it improves query performance.
                    TOPIC: Database Management

                    Q5: How would you design a secure backend service for a job application platform?
                    TOPIC: Backend System Design
                    """;
        }

        if (r.contains("data analyst")) {

            return """
                    Q1: What is the difference between a primary key and a foreign key in SQL?
                    TOPIC: SQL

                    Q2: Explain the difference between INNER JOIN and LEFT JOIN.
                    TOPIC: SQL Joins

                    Q3: How would you handle missing values in a Pandas DataFrame?
                    TOPIC: Python Pandas

                    Q4: Explain the difference between mean, median and mode.
                    TOPIC: Statistics

                    Q5: How would you analyze a dataset and communicate its important findings using visualizations?
                    TOPIC: Data Analysis
                    """;
        }

        if (r.contains("software engineer") ||
                r.contains("software developer")) {

            return """
                    Q1: Explain the difference between an array and a linked list.
                    TOPIC: Data Structures

                    Q2: What is the difference between a stack and a queue?
                    TOPIC: Data Structures

                    Q3: Explain how binary search works and analyze its time complexity.
                    TOPIC: Algorithms

                    Q4: What are the four main principles of object-oriented programming?
                    TOPIC: OOP

                    Q5: How would you design a software application that stores and manages user data?
                    TOPIC: Software Design
                    """;
        }

        return """
                Q1: What are the main programming concepts used in this role?
                TOPIC: Technical Fundamentals

                Q2: Explain an important technology used in this role.
                TOPIC: Technical Knowledge

                Q3: How would you solve a practical problem using the technologies required for this role?
                TOPIC: Problem Solving

                Q4: How would you debug a technical issue in an application?
                TOPIC: Debugging

                Q5: How would you design a small application related to this role?
                TOPIC: Application Design
                """;
    }
        /*
     * EVALUATE STUDENT ANSWERS
     */

    public MockInterviewDTO.EvaluationResponse evaluateInterview(
            int studentId,
            MockInterviewDTO.SubmitRequest request) {

Student student = studentRepository.findById(studentId)
        .orElseThrow(() ->
                new RuntimeException("Student not found"));

        if (request == null ||
                request.answers() == null ||
                request.answers().isEmpty()) {

            throw new RuntimeException(
                    "Please submit interview answers");
        }

        int questionCount = request.answers().size();

        StringBuilder answerContent = new StringBuilder();

        /*
         * VALIDATE AND COLLECT ANSWERS
         */

        Map<Integer, Boolean> invalidAnswers = new HashMap<>();

        for (int i = 0; i < questionCount; i++) {

            MockInterviewDTO.Answer answer =
                    request.answers().get(i);

            if (answer == null) {
                throw new RuntimeException(
                        "Invalid interview answer");
            }

            String question = answer.question() == null
                    ? "Question not provided"
                    : answer.question().trim();

            String studentAnswer = answer.answer() == null
                    ? ""
                    : answer.answer().trim();

            boolean invalid =
                    isInvalidAnswer(studentAnswer);

            invalidAnswers.put(i + 1, invalid);

            answerContent.append("\nQ")
                    .append(i + 1)
                    .append(": ")
                    .append(question)
                    .append("\nSTUDENT ANSWER: ")
                    .append(studentAnswer.isEmpty()
                            ? "[NO ANSWER]"
                            : studentAnswer)
                    .append("\n");
        }

        /*
         * AI EVALUATION PROMPT
         */

        String prompt = """
                You are a STRICT technical interview evaluator.

                TARGET ROLE:
                %s

                STUDENT ANSWERS:
                %s

                EACH QUESTION CARRIES 20 MARKS.

                SCORING RUBRIC:

                17-20: Technically correct, detailed and accurate.
                12-16: Mostly correct but missing important details.
                6-11: Partially correct with limited understanding.
                1-5: Mostly incorrect, vague or irrelevant.
                0: Empty, meaningless or completely incorrect.

                STRICT RULES:

                1. Evaluate only what the student actually wrote.
                2. Do not assume knowledge that is not demonstrated.
                3. Do not award marks for unmentioned experience.
                4. Generic answers must not receive high marks.
                5. Empty answers must receive exactly 0.
                6. Give partial marks for partially correct answers.
                7. Evaluate each question independently.
                8. Do not invent student skills or projects.
                9. Do not automatically give similar scores.
                10. Be strict but fair.
                11. Do not calculate an overall score.
                12. Return an integer score for every question.
                13. Compare every answer with its actual question.
                14. Never provide feedback unrelated to the answer.
                15. Do not assume that mentioning a technology means
                    the student understands it.

                EXACT OUTPUT FORMAT:

                Q1 SCORE: [integer]/20
                Q1 CORRECTNESS: [Correct/Partially Correct/Incorrect]
                Q1 EVIDENCE: [What was actually demonstrated]
                Q1 MISSING: [Missing concepts]
                Q1 FEEDBACK: [How to improve]

                Repeat this format for every question.

                STRENGTHS:
                Mention only demonstrated strengths.

                WEAK AREAS:
                Mention actual knowledge gaps.

                IMPROVEMENT ROADMAP:
                Give exactly three practical learning actions.

                INTERVIEW READINESS:
                Give an honest assessment based on this interview.

                Do not use markdown tables.
                """.formatted(
                request.role(),
                answerContent
        );

        String fallback = """
                EVALUATION UNAVAILABLE

                The AI could not evaluate the submitted answers.
                Please try again.
                """;

        String evaluation =
                ollamaService.generateMockInterviewEvaluation(
                        prompt,
                        fallback
                );

        System.out.println(
                "========== OLLAMA RAW EVALUATION ==========");

        System.out.println(evaluation);

        System.out.println(
                "============================================");

        if (evaluation == null ||
                evaluation.isBlank() ||
                evaluation.contains("EVALUATION UNAVAILABLE")) {

            return new MockInterviewDTO.EvaluationResponse(
                    request.role(),
                    fallback
            );
        }

        /*
         * EXTRACT INDIVIDUAL SCORES
         */

        Map<Integer, Integer> scores = new HashMap<>();

        Pattern scorePattern = Pattern.compile(
                "(?im)^\\s*Q\\s*(\\d+)\\s*SCORE\\s*:\\s*(\\d{1,2})\\s*(?:/\\s*20)?\\b.*$"
        );

        Matcher matcher = scorePattern.matcher(evaluation);

        while (matcher.find()) {

            int questionNumber =
                    Integer.parseInt(matcher.group(1));

            int score =
                    Integer.parseInt(matcher.group(2));

            if (questionNumber < 1 ||
                    questionNumber > questionCount ||
                    score < 0 ||
                    score > 20) {

                throw new RuntimeException(
                        "AI returned an invalid score.");
            }

            if (scores.containsKey(questionNumber)) {

                throw new RuntimeException(
                        "AI returned duplicate question scores.");
            }

            scores.put(questionNumber, score);
        }

        /*
         * VALIDATE ALL SCORES
         */

        for (int i = 1; i <= questionCount; i++) {

            if (!scores.containsKey(i)) {

                throw new RuntimeException(
                        "AI evaluation incomplete. Missing score for question "
                                + i + ". Please try again.");
            }

            /*
             * FORCE ZERO FOR EMPTY OR OBVIOUSLY RANDOM ANSWERS
             */

            if (invalidAnswers.get(i)) {
                scores.put(i, 0);
            }
        }

        /*
         * REMOVE AI-GENERATED OVERALL SCORE
         */

        String cleanedEvaluation = evaluation.replaceAll(
                "(?im)^\\s*OVERALL SCORE\\s*:.*(?:\\R|$)",
                ""
        ).trim();

        /*
         * OVERRIDE FEEDBACK FOR INVALID ANSWERS
         */

        for (int i = 1; i <= questionCount; i++) {

            if (invalidAnswers.get(i)) {

                String invalidFeedback =
                        "Q" + i + " SCORE: 0/20\n" +
                        "Q" + i + " CORRECTNESS: Incorrect\n" +
                        "Q" + i + " EVIDENCE: No relevant technical knowledge was demonstrated in the submitted answer.\n" +
                        "Q" + i + " MISSING: A meaningful answer explaining the technical concepts was not provided.\n" +
                        "Q" + i + " FEEDBACK: Read the question carefully and provide a relevant technical explanation.\n";

                Pattern questionPattern = Pattern.compile(
                        "(?is)^\\s*Q\\s*" + i +
                        "\\s*SCORE\\s*:.*?(?=\\n\\s*Q\\s*" +
                        (i + 1) +
                        "\\s*SCORE\\s*:|\\n\\s*STRENGTHS\\s*:|\\z)"
                );

                Matcher questionMatcher =
                        questionPattern.matcher(cleanedEvaluation);

                if (questionMatcher.find()) {

                    cleanedEvaluation =
                            questionMatcher.replaceFirst(
                                    Matcher.quoteReplacement(
                                            invalidFeedback
                                    )
                            );

                } else {

                    cleanedEvaluation +=
                            "\n" + invalidFeedback;
                }
            }
        }

        /*
         * UPDATE INDIVIDUAL SCORES
         * USING JAVA-VALIDATED VALUES
         */

        for (int i = 1; i <= questionCount; i++) {

            int score = scores.get(i);

            Pattern individualScorePattern = Pattern.compile(
                    "(?im)^\\s*Q\\s*" + i +
                    "\\s*SCORE\\s*:\\s*\\d{1,2}\\s*(?:/\\s*20)?\\b.*$"
            );

            Matcher individualMatcher =
                    individualScorePattern.matcher(cleanedEvaluation);

            if (individualMatcher.find()) {

                cleanedEvaluation =
                        individualMatcher.replaceFirst(
                                "Q" + i + " SCORE: " + score + "/20"
                        );
            }
        }

        /*
         * CALCULATE FINAL SCORE AFTER ALL OVERRIDES
         */

        int totalScore = 0;

        for (int i = 1; i <= questionCount; i++) {
            totalScore += scores.get(i);
        }

        int maximumScore = questionCount * 20;

        int finalScore = Math.round(
                (totalScore * 100.0f) / maximumScore
        );

        System.out.println("INDIVIDUAL SCORES: " + scores);
        System.out.println("TOTAL SCORE: " + totalScore);
        System.out.println("FINAL SCORE: " + finalScore);

        String finalEvaluation =
                "OVERALL SCORE: " +
                finalScore +
                "/100\n\n" +
                cleanedEvaluation;

// Prepare interview questions and submitted answers for storage.
StringBuilder savedAnswers = new StringBuilder();

for (int i = 0; i < request.answers().size(); i++) {

    MockInterviewDTO.Answer answer = request.answers().get(i);

    savedAnswers.append("Q")
            .append(i + 1)
            .append(": ")
            .append(answer.question())
            .append("\nSTUDENT ANSWER: ")
            .append(answer.answer() == null
                    ? ""
                    : answer.answer())
            .append("\n\n");
}

// Save completed interview in MySQL.
MockInterviewAttempt attempt = new MockInterviewAttempt(
        student,
        request.role(),
        finalScore,
        savedAnswers.toString(),
        finalEvaluation
);

mockInterviewAttemptRepository.save(attempt);

// Return evaluation to frontend.
return new MockInterviewDTO.EvaluationResponse(
        request.role(),
        finalEvaluation
);
    }

    /*
     * BASIC ANSWER VALIDATION
     */

    private boolean isInvalidAnswer(String answer) {

        if (answer == null || answer.isBlank()) {
            return true;
        }

        String cleaned = answer.trim();

        /*
         * Remove spaces and punctuation for basic checks.
         */

        String lettersOnly =
                cleaned.replaceAll("[^a-zA-Z]", "");

        if (lettersOnly.isEmpty()) {
            return true;
        }

        /*
         * Detect obvious random strings with no vowels.
         * Example: gvygvv, xyzxyz, asdfgh.
         */

        if (lettersOnly.length() >= 4 &&
                !lettersOnly.matches("(?i).* [aeiou] .*".trim()) &&
                !lettersOnly.matches("(?i).* [aeiou].*".trim())) {

            if (!lettersOnly.matches("(?i).*[aeiou].*")) {
                return true;
            }
        }

        /*
         * Detect repeated-character spam.
         * Example: aaaaaaaa, xxxxxxxxx.
         */

        if (lettersOnly.length() >= 5 &&
                lettersOnly.matches("(?i)(.)\\1+")) {
            return true;
        }

        return false;
    }
        /*
     * PARSE GENERATED QUESTIONS
     */

    private List<MockInterviewDTO.Question> parseQuestions(
            String response) {

        List<MockInterviewDTO.Question> questions =
                new ArrayList<>();

        if (response == null || response.isBlank()) {
            return questions;
        }

        String[] lines = response.split("\\n");

        String currentQuestion = null;
        String currentTopic = "Technical";

        for (String line : lines) {

            line = line.trim();

            if (line.matches("(?i)^Q\\s*\\d+\\s*:.*")) {

                if (currentQuestion != null) {

                    questions.add(
                            new MockInterviewDTO.Question(
                                    questions.size() + 1,
                                    currentQuestion,
                                    currentTopic
                            )
                    );
                }

                currentQuestion =
                        line.replaceFirst(
                                "(?i)^Q\\s*\\d+\\s*:\\s*",
                                ""
                        );

                currentTopic = "Technical";
            }

            else if (line.startsWith("TOPIC:")) {

                currentTopic =
                        line.substring("TOPIC:".length()).trim();
            }
        }

        if (currentQuestion != null) {

            questions.add(
                    new MockInterviewDTO.Question(
                            questions.size() + 1,
                            currentQuestion,
                            currentTopic
                    )
            );
        }

        return questions;
    }
}




