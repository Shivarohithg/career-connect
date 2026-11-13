
import { useState } from "react";
import {
    generateInterview,
    evaluateInterview
} from "../services/mockInterviewService";
import "./MockInterview.css";

function MockInterview() {

    const student = JSON.parse(
        localStorage.getItem("student") || "{}"
    );

    const studentId = student.id;

    const [role, setRole] = useState("Java Developer");
    const [questions, setQuestions] = useState([]);
    const [answers, setAnswers] = useState({});
    const [current, setCurrent] = useState(0);

    const [loading, setLoading] = useState(false);
    const [submitting, setSubmitting] = useState(false);

    const [result, setResult] = useState(null);
    const [error, setError] = useState("");

    const startInterview = async () => {

        if (!studentId) {
            setError("Please log in to start your interview.");
            return;
        }

        try {
            setLoading(true);
            setError("");
            setResult(null);
            setQuestions([]);
            setAnswers({});
            setCurrent(0);

            const data = await generateInterview(
                studentId,
                role
            );

            setQuestions(data.questions || []);

            if (!data.questions?.length) {
                setError("No questions were generated.");
            }

        } catch (err) {
            setError(
                err.response?.data?.message ||
                "Unable to generate interview questions."
            );
        } finally {
            setLoading(false);
        }
    };

    const updateAnswer = (value) => {

        const question = questions[current];

        setAnswers(prev => ({
            ...prev,
            [question.questionId]: value
        }));
    };

    const submitInterview = async () => {

        try {
            setSubmitting(true);
            setError("");

            const submittedAnswers = questions.map(q => ({
                questionId: q.questionId,
                question: q.question,
                answer: answers[q.questionId] || ""
            }));

            const data = await evaluateInterview(
                studentId,
                {
                    role,
                    answers: submittedAnswers
                }
            );

            setResult(data);

        } catch (err) {
            setError(
                err.response?.data?.message ||
                "Unable to evaluate your answers."
            );
        } finally {
            setSubmitting(false);
        }
    };

    const progress = questions.length
        ? ((current + 1) / questions.length) * 100
        : 0;

    return (
        <div className="mock-page">

            <div className="mock-container">

                <div className="mock-header">
                    <div>
                        <span className="mock-tag">
                            CAREERCONNECT AI
                        </span>

                        <h1>Mock Interview</h1>

                        <p>
                            Practice technical interviews
                            and improve your job readiness.
                        </p>
                    </div>
                </div>

                {!questions.length && !result && (

                    <div className="mock-card">

                        <h2>Start Your Interview</h2>

                        <label>Choose your target role</label>

                        <input
                            value={role}
                            onChange={e => setRole(e.target.value)}
                            placeholder="Enter your target role"
                        />

                        <button
                            className="mock-primary"
                            onClick={startInterview}
                            disabled={loading || !role.trim()}
                        >
                            {loading
                                ? "Generating Questions..."
                                : "Generate Interview"}
                        </button>

                    </div>
                )}

                {questions.length > 0 && !result && (

                    <div className="mock-card">

                        <div className="mock-question-top">
                            <span>{role}</span>

                            <span>
                                Question {current + 1} of {questions.length}
                            </span>
                        </div>

                        <div className="mock-progress">
                            <div
                                style={{ width: `${progress}%` }}
                            />
                        </div>

                        <span className="mock-topic">
                            {questions[current].topic}
                        </span>

                        <h2>
                            {questions[current].question}
                        </h2>

                        <textarea
                            rows="7"
                            placeholder="Write your answer here..."
                            value={
                                answers[
                                    questions[current].questionId
                                ] || ""
                            }
                            onChange={e =>
                                updateAnswer(e.target.value)
                            }
                        />

                        <div className="mock-navigation">

                            <button
                                className="mock-secondary"
                                disabled={current === 0}
                                onClick={() =>
                                    setCurrent(current - 1)
                                }
                            >
                                Previous
                            </button>

                            {current < questions.length - 1 ? (

                                <button
                                    className="mock-primary"
                                    onClick={() =>
                                        setCurrent(current + 1)
                                    }
                                >
                                    Next Question
                                </button>

                            ) : (

                                <button
                                    className="mock-primary"
                                    onClick={submitInterview}
                                    disabled={submitting}
                                >
                                    {submitting
                                        ? "Evaluating..."
                                        : "Submit Interview"}
                                </button>

                            )}

                        </div>

                    </div>
                )}

                {result && (

                    <div className="mock-card mock-result">

                        <span className="mock-tag">
                            INTERVIEW COMPLETED
                        </span>

                        <h2>Your AI Evaluation</h2>

                        <div className="mock-evaluation">
                            {result.evaluation}
                        </div>

                        <button
                            className="mock-primary"
                            onClick={() => {
                                setQuestions([]);
                                setAnswers({});
                                setResult(null);
                                setCurrent(0);
                            }}
                        >
                            Start Another Interview
                        </button>

                    </div>
                )}

                {error && (
                    <div className="mock-error">
                        {error}
                    </div>
                )}

            </div>

        </div>
    );
}

export default MockInterview;
