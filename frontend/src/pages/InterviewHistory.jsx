import { useEffect, useState } from "react";
import {
    getInterviewHistory,
    getInterviewHistoryDetails
} from "../services/mockInterviewService";

import "./InterviewHistory.css";

function InterviewHistory() {

    const storedStudent = localStorage.getItem("student");
    const student = storedStudent ? JSON.parse(storedStudent) : null;

    const studentId = student?.id;

    const [history, setHistory] = useState([]);
    const [selectedInterview, setSelectedInterview] = useState(null);

    const [loading, setLoading] = useState(true);
    const [detailsLoading, setDetailsLoading] = useState(false);
    const [error, setError] = useState("");
    

    // Fetch interview history
    useEffect(() => {

        const fetchHistory = async () => {

            if (!studentId) {
                setError("Student information not found. Please log in again.");
                setLoading(false);
                return;
            }

            try {
                const data = await getInterviewHistory(studentId);
                setHistory(data);
            } catch (err) {
                console.error("Error fetching interview history:", err);
                setError("Unable to load interview history.");
            } finally {
                setLoading(false);
            }
        };

        fetchHistory();

    }, [studentId]);

    // Fetch complete interview details
    const handleViewDetails = async (attemptId) => {

        setDetailsLoading(true);
        setSelectedInterview(null);
        setError("");

        try {
            const data = await getInterviewHistoryDetails(
                studentId,
                attemptId
            );

            setSelectedInterview(data);

        } catch (err) {
            console.error("Error fetching interview details:", err);
            setError("Unable to load interview details.");
        } finally {
            setDetailsLoading(false);
        }
    };

    const formatDate = (date) => {
        return new Date(date).toLocaleString("en-IN", {
            dateStyle: "medium",
            timeStyle: "short"
        });

    };

    const totalInterviews = history.length;

const averageScore = totalInterviews > 0
    ? Math.round(
        history.reduce((sum, item) => sum + item.score, 0) /
        totalInterviews
    )
    : 0;

const highestScore = totalInterviews > 0
    ? Math.max(...history.map(item => item.score))
    : 0;


    if (loading) {
        return <div className="history-container">Loading interview history...</div>;
    }

    return (

        <div className="history-container">

            <div className="history-header">
                <h1>My Interview History</h1>
                <p>Track your previous mock interviews and review your performance.</p>
            </div>

            <div className="history-stats">

    <div className="stat-card">
        <h3>Total Interviews</h3>
        <p>{totalInterviews}</p>
    </div>

    <div className="stat-card">
        <h3>Average Score</h3>
        <p>{averageScore}/100</p>
    </div>

    <div className="stat-card">
        <h3>Highest Score</h3>
        <p>{highestScore}/100</p>
    </div>

</div>

            {error && (
                <div className="history-error">
                    {error}
                </div>
            )}

            {!error && history.length === 0 && (
                <div className="empty-history">
                    <h2>No Interviews Yet</h2>
                    <p>Complete your first mock interview to see your history here.</p>
                </div>
            )}

            <div className="score-trend-card">
    <h3>Interview Performance Trend</h3>
    <p className="trend-subtitle">
        Track your scores across previous interviews.
    </p>

    {history.length === 0 ? (
        <p>No interview data available yet.</p>
    ) : (
        <div className="trend-chart">
            {history
                .slice()
                .reverse()
                .map((item, index) => (
                    <div className="trend-item" key={item.id}>
                        <div className="trend-bar-container">
                            <div
                                className="trend-bar"
                                style={{
                                    height: `${Math.max(5, item.score)}%`
                                }}
                            >
                                <span>{item.score}</span>
                            </div>
                        </div>

                        <span className="trend-label">
                            #{index + 1}
                        </span>
                    </div>
                ))}
        </div>
    )}
</div>

            <div className="history-layout">

                {/* Interview list */}
                <div className="history-list">

                    {history.map((item) => (

                        <div className="history-card" key={item.id}>

                            <div className="history-card-top">
                                <h3>{item.role}</h3>

                                <span className="history-score">
                                    {item.score}/100
                                </span>
                            </div>

                            <p className="history-date">
                                {formatDate(item.createdAt)}
                            </p>

                            <button
                                className="history-button"
                                onClick={() => handleViewDetails(item.id)}
                            >
                                View Details
                            </button>

                        </div>

                    ))}

                </div>

                {/* Selected interview details */}
                <div className="history-details">

                    {detailsLoading && (
                        <p>Loading interview details...</p>
                    )}

                    {!detailsLoading && !selectedInterview && history.length > 0 && (
                        <div className="details-placeholder">
                            <h3>Interview Details</h3>
                            <p>Select an interview and click View Details to see its questions, answers and AI evaluation.</p>
                        </div>
                    )}

                    {selectedInterview && (

                        <>

                            <div className="details-heading">
                                <h2>{selectedInterview.role}</h2>
                                <span>
                                    Score: {selectedInterview.score}/100
                                </span>
                            </div>

                            <p className="history-date">
                                {formatDate(selectedInterview.createdAt)}
                            </p>

                            <h3>Questions and Your Answers</h3>

                            <div className="answers-content">
                                {selectedInterview.answersText
                                    .split(/\n(?=Q\d+:)/)
                                    .map((block, index) => (

                                        <div className="answer-block" key={index}>
                                            {block.trim()}
                                        </div>

                                    ))}
                            </div>

                            <h3>AI Evaluation</h3>

                            <pre className="evaluation-content">
                                {selectedInterview.evaluation}
                            </pre>

                        </>

                    )}

                </div>

            </div>

        </div>
    );
}

export default InterviewHistory;