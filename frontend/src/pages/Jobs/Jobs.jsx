
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import { getAllJobs } from "../../services/jobService";
import "./Jobs.css";

function Jobs() {
    const navigate = useNavigate();

    const [jobs, setJobs] = useState([]);
    const [recommendedJobs, setRecommendedJobs] = useState([]);

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const [searchTerm, setSearchTerm] = useState("");
    const [location, setLocation] = useState("");

    // Day 70: Resume matching states
    const [resumeMatches, setResumeMatches] = useState({});
    const [resumeMatchLoading, setResumeMatchLoading] = useState(false);
    const [resumeMatchError, setResumeMatchError] = useState("");
    const [sortBy, setSortBy] = useState("default");

    // =====================================
    // Load Jobs, Recommendations and Resume Matches
    // =====================================

    useEffect(() => {
        const loadData = async () => {
            try {
                // 1. Get logged-in student
                const storedStudent =
                    localStorage.getItem("student");

                if (!storedStudent) {
                    navigate("/login");
                    return;
                }

                const student = JSON.parse(storedStudent);
                const studentId = student.id;

                // 2. Load all jobs
                const jobsData = await getAllJobs();
                setJobs(jobsData);

                // 3. Load existing profile-based AI recommendations
                try {
                    const recommendationResponse = await axios.get(
                        `http://localhost:8080/ai/job-recommendations/${studentId}`
                    );

                    setRecommendedJobs(
                        recommendationResponse.data
                    );
                } catch (recommendationError) {
                    console.error(
                        "Error loading AI recommendations:",
                        recommendationError
                    );
                }

                // 4. Day 70: Load resume-based matches for all jobs
                setResumeMatchLoading(true);

                try {
                    const resumeResponse = await axios.get(
                        `http://localhost:8080/ai/resume-job-match/all/${studentId}`
                    );

                    const matchData = resumeResponse.data;

                    // Convert response array into a jobId-based lookup object.
                    const matchMap = {};

                    matchData.forEach(match => {
                        matchMap[match.jobId] = match;
                    });

                    setResumeMatches(matchMap);
                    setResumeMatchError("");

                } catch (resumeError) {
                    console.error(
                        "Error loading resume job matches:",
                        resumeError
                    );

                    setResumeMatches({});

                    setResumeMatchError(
                        resumeError.response?.data?.error ||
                        "Unable to calculate resume matches."
                    );

                } finally {
                    setResumeMatchLoading(false);
                }

            } catch (error) {
                console.error(
                    "Error loading jobs:",
                    error
                );

                setError(
                    error.response?.data ||
                    "Unable to load jobs."
                );

            } finally {
                setLoading(false);
            }
        };

        loadData();

    }, [navigate]);

    // =====================================
    // Search + Location Filtering
    // =====================================

    const filteredJobs = jobs.filter(job => {
        const matchesSearch =
            job.title
                ?.toLowerCase()
                .includes(searchTerm.toLowerCase()) ||

            job.company
                ?.toLowerCase()
                .includes(searchTerm.toLowerCase());

        const matchesLocation =
            job.location
                ?.toLowerCase()
                .includes(location.toLowerCase());

        return matchesSearch && matchesLocation;
    });

    // =====================================
    // Day 70: Sort Jobs by Resume Match
    // =====================================

    const sortedJobs = [...filteredJobs].sort((a, b) => {
        if (sortBy === "highest") {
            const matchA = resumeMatches[a.id];
            const matchB = resumeMatches[b.id];

            const scoreA =
                matchA?.matchAvailable
                    ? matchA.matchPercentage
                    : -1;

            const scoreB =
                matchB?.matchAvailable
                    ? matchB.matchPercentage
                    : -1;

            return scoreB - scoreA;
        }

        if (sortBy === "lowest") {
            const matchA = resumeMatches[a.id];
            const matchB = resumeMatches[b.id];

            const scoreA =
                matchA?.matchAvailable
                    ? matchA.matchPercentage
                    : -1;

            const scoreB =
                matchB?.matchAvailable
                    ? matchB.matchPercentage
                    : -1;

            // Jobs without a match remain at the bottom.
            if (scoreA === -1) return 1;
            if (scoreB === -1) return -1;

            return scoreA - scoreB;
        }

        return 0;
    });

    // =====================================
    // Loading
    // =====================================

    if (loading) {
        return (
            <div className="jobs-page">

                <div className="jobs-hero">
                    <h1>
                        Find Your Next Opportunity
                    </h1>

                    <p>
                        Analyzing jobs based on your
                        skills and career direction...
                    </p>
                </div>

                <h2 className="jobs-section-title">
                    🤖 AI is analyzing available jobs...
                </h2>

            </div>
        );
    }

    // =====================================
    // Error
    // =====================================

    if (error) {
        return (
            <div className="jobs-page">

                <h2>{error}</h2>

                <button
                    className="view-button"
                    onClick={() => window.location.reload()}
                >
                    Try Again
                </button>

            </div>
        );
    }

    // =====================================
    // Reusable Resume Match Card Section
    // =====================================

    const renderResumeMatch = job => {
        const match = resumeMatches[job.id];

        if (resumeMatchLoading) {
            return (
                <div className="resume-match-section">
                    <p>🔄 Calculating resume match...</p>
                </div>
            );
        }

        if (resumeMatchError) {
            return null;
        }

        if (!match) {
            return null;
        }

        if (!match.matchAvailable) {
            return (
                <div className="resume-match-section">
                    <strong>📄 Resume Match</strong>

                    <p>
                        Match unavailable:{" "}
                        {match.message ||
                            "Job requirements are not configured."}
                    </p>
                </div>
            );
        }

        const percentage = match.matchPercentage;

        return (
            <div
                className="resume-match-section"
                style={{
                    marginTop: "15px",
                    padding: "14px",
                    borderRadius: "10px",
                    background: "#f4f7ff",
                    border: "1px solid #dce5ff"
                }}
            >

                {/* Match Percentage */}

                <div
                    style={{
                        display: "flex",
                        justifyContent: "space-between",
                        alignItems: "center",
                        marginBottom: "10px"
                    }}
                >
                    <strong>
                        📄 Resume Match
                    </strong>

                    <strong
                        style={{
                            color:
                                percentage >= 70
                                    ? "#16803c"
                                    : percentage >= 40
                                        ? "#c47b00"
                                        : "#d14343",
                            fontSize: "18px"
                        }}
                    >
                        {percentage}%
                    </strong>
                </div>

                {/* Progress Bar */}

                <div
                    style={{
                        width: "100%",
                        height: "9px",
                        background: "#dce3ed",
                        borderRadius: "10px",
                        overflow: "hidden",
                        marginBottom: "14px"
                    }}
                >
                    <div
                        style={{
                            width: `${percentage}%`,
                            height: "100%",
                            background:
                                percentage >= 70
                                    ? "#22a06b"
                                    : percentage >= 40
                                        ? "#e5a000"
                                        : "#dc5555",
                            borderRadius: "10px",
                            transition: "width 0.4s ease"
                        }}
                    />
                </div>

                {/* Matched Skills */}

                {match.matchedSkills?.length > 0 && (
                    <div style={{ marginBottom: "12px" }}>

                        <strong>
                            ✓ Matched Skills
                        </strong>

                        <div
                            style={{
                                display: "flex",
                                flexWrap: "wrap",
                                gap: "6px",
                                marginTop: "8px"
                            }}
                        >
                            {match.matchedSkills.map(skill => (
                                <span
                                    key={skill}
                                    style={{
                                        padding: "5px 9px",
                                        borderRadius: "15px",
                                        background: "#d9f5e5",
                                        color: "#176b3a",
                                        fontSize: "12px"
                                    }}
                                >
                                    ✓ {skill}
                                </span>
                            ))}
                        </div>

                    </div>
                )}

                {/* Missing Skills */}

                {match.missingSkills?.length > 0 && (
                    <div style={{ marginBottom: "12px" }}>

                        <strong>
                            ⚠ Missing Skills
                        </strong>

                        <div
                            style={{
                                display: "flex",
                                flexWrap: "wrap",
                                gap: "6px",
                                marginTop: "8px"
                            }}
                        >
                            {match.missingSkills.map(skill => (
                                <span
                                    key={skill}
                                    style={{
                                        padding: "5px 9px",
                                        borderRadius: "15px",
                                        background: "#ffe3e3",
                                        color: "#a83232",
                                        fontSize: "12px"
                                    }}
                                >
                                    {skill}
                                </span>
                            ))}
                        </div>

                    </div>
                )}

                <p
                    style={{
                        fontSize: "11px",
                        color: "#666",
                        marginTop: "10px"
                    }}
                >
                    Based only on skills detected in your uploaded
                    resume. This is a CareerConnect estimate,
                    not an official ATS score.
                </p>

            </div>
        );
    };

    // =====================================
    // Page
    // =====================================

    return (
        <div className="jobs-page">

            {/* Hero Section */}

            <section className="jobs-hero">

                <h1>
                    Find Your Next Opportunity
                </h1>

                <p>
                    Discover jobs that match your
                    skills and career goals.
                </p>

            </section>

            {/* Search Section */}

            <div className="search-container">

                <input
                    type="text"
                    placeholder="Search by job title or company"
                    className="search-box"
                    value={searchTerm}
                    onChange={e => setSearchTerm(e.target.value)}
                />

                <input
                    type="text"
                    placeholder="Location"
                    className="search-box"
                    value={location}
                    onChange={e => setLocation(e.target.value)}
                />

                <button className="search-button">
                    Search
                </button>

            </div>

            {/* =====================================
                AI Recommended Jobs
            ===================================== */}

            {recommendedJobs.length > 0 && (
                <>
                    <div className="ai-jobs-heading">

                        <h2 className="jobs-section-title">
                            🤖 AI Recommended Jobs
                        </h2>

                        <p>
                            Personalized recommendations
                            based on your profile, skills,
                            and career direction.
                        </p>

                    </div>

                    <div className="jobs-grid">

                        {recommendedJobs
                            .slice(0, 5)
                            .map(recommendation => {

                                const job = recommendation.job;

                                if (!job) return null;

                                return (
                                    <div
                                        className="job-card recommended-card"
                                        key={`ai-${job.id}`}
                                    >

                                        {/* AI Match */}

                                        <div className="ai-match-badge">
                                            ⭐{" "}
                                            {recommendation.matchPercentage}%
                                            AI Match
                                        </div>

                                        {/* Company + Job Title */}

                                        <div className="job-card-header">

                                            <div className="company-logo">
                                                {job.company
                                                    ?.charAt(0)
                                                    .toUpperCase()}
                                            </div>

                                            <div>
                                                <h3 className="job-title">
                                                    {job.title}
                                                </h3>

                                                <p className="job-company">
                                                    {job.company}
                                                </p>
                                            </div>

                                        </div>

                                        {/* Job Details */}

                                        <div className="job-details">

                                            <p className="job-info">
                                                📍 {job.location}
                                            </p>

                                            <p className="job-salary">
                                                ₹
                                                {job.salary?.toLocaleString(
                                                    "en-IN"
                                                )}
                                            </p>

                                        </div>

                                        {/* AI Role */}

                                        {recommendation.matchedRole && (
                                            <div className="ai-role">

                                                <strong>
                                                    🎯 Career Direction
                                                </strong>

                                                <p>
                                                    {recommendation.matchedRole}
                                                </p>

                                            </div>
                                        )}

                                        {/* Matched Skills */}

                                        {recommendation.matchedSkills?.length > 0 && (
                                            <div className="ai-skills">

                                                <strong>
                                                    ✓ Matched Skills
                                                </strong>

                                                <div className="ai-skill-list">

                                                    {recommendation.matchedSkills.map(
                                                        skill => (
                                                            <span
                                                                className="ai-skill matched"
                                                                key={skill}
                                                            >
                                                                ✓ {skill}
                                                            </span>
                                                        )
                                                    )}

                                                </div>

                                            </div>
                                        )}

                                        {/* Missing Skills */}

                                        {recommendation.missingSkills?.length > 0 && (
                                            <div className="ai-skills">

                                                <strong>
                                                    ⚠ Skills to Improve
                                                </strong>

                                                <div className="ai-skill-list">

                                                    {recommendation.missingSkills.map(
                                                        skill => (
                                                            <span
                                                                className="ai-skill missing"
                                                                key={skill}
                                                            >
                                                                {skill}
                                                            </span>
                                                        )
                                                    )}

                                                </div>

                                            </div>
                                        )}

                                        {/* AI Explanation */}

                                        {recommendation.explanation && (
                                            <div className="ai-explanation">

                                                <strong>
                                                    💡 Why recommended?
                                                </strong>

                                                <p>
                                                    {recommendation.explanation}
                                                </p>

                                            </div>
                                        )}

                                        {/* Tags */}

                                        <div className="job-tags">

                                            <span className="job-tag">
                                                AI Recommended
                                            </span>

                                            <span className="job-tag">
                                                Full Time
                                            </span>

                                        </div>

                                        {/* View Details */}

                                        <button
                                            className="view-button"
                                            onClick={() =>
                                                navigate(`/jobs/${job.id}`)
                                            }
                                        >
                                            View Details
                                        </button>

                                    </div>
                                );
                            })}

                    </div>
                </>
            )}

            {/* =====================================
                Available Jobs
            ===================================== */}

            <h2 className="jobs-section-title">
                Available Jobs
            </h2>

            {/* Resume Match Information */}

            <div
                style={{
                    marginBottom: "18px",
                    padding: "14px",
                    borderRadius: "10px",
                    background: "#f4f7ff",
                    border: "1px solid #dce5ff"
                }}
            >

                <strong>
                    📄 Resume-Based Job Matching
                </strong>

                <p style={{ marginTop: "6px", fontSize: "14px" }}>
                    Compare your uploaded resume against job
                    requirements and find opportunities that
                    match your skills.
                </p>

                {resumeMatchLoading && (
                    <p style={{ marginTop: "8px" }}>
                        🔄 Calculating resume matches...
                    </p>
                )}

                {!resumeMatchLoading && resumeMatchError && (
                    <p
                        style={{
                            marginTop: "8px",
                            color: "#b45309"
                        }}
                    >
                        {resumeMatchError}
                    </p>
                )}

            </div>

            {/* Sorting */}

            <div
                style={{
                    display: "flex",
                    justifyContent: "flex-end",
                    alignItems: "center",
                    gap: "10px",
                    marginBottom: "20px",
                    flexWrap: "wrap"
                }}
            >

                <label htmlFor="resume-sort">
                    Sort jobs:
                </label>

                <select
                    id="resume-sort"
                    value={sortBy}
                    onChange={e => setSortBy(e.target.value)}
                    style={{
                        padding: "9px 12px",
                        borderRadius: "7px",
                        border: "1px solid #ccc",
                        cursor: "pointer"
                    }}
                >
                    <option value="default">
                        Default order
                    </option>

                    <option value="highest">
                        Highest resume match
                    </option>

                    <option value="lowest">
                        Lowest resume match
                    </option>

                </select>

            </div>

            {/* Available Jobs Cards */}

            {sortedJobs.length === 0 ? (

                <p className="no-jobs">
                    No jobs found.
                </p>

            ) : (

                <div className="jobs-grid">

                    {sortedJobs.map(job => {

                        const match = resumeMatches[job.id];

                        return (
                            <div
                                className="job-card"
                                key={job.id}
                            >

                                {/* Company + Job Title */}

                                <div className="job-card-header">

                                    <div className="company-logo">
                                        {job.company
                                            ?.charAt(0)
                                            .toUpperCase()}
                                    </div>

                                    <div>
                                        <h3 className="job-title">
                                            {job.title}
                                        </h3>

                                        <p className="job-company">
                                            {job.company}
                                        </p>
                                    </div>

                                </div>

                                {/* Job Details */}

                                <div className="job-details">

                                    <p className="job-info">
                                        📍 {job.location}
                                    </p>

                                    <p className="job-salary">
                                        ₹
                                        {job.salary?.toLocaleString(
                                            "en-IN"
                                        )}
                                    </p>

                                </div>

                                {/* Day 70: Resume Match Details */}

                                {renderResumeMatch(job)}

                                {/* Tags */}

                                <div className="job-tags">

                                    <span className="job-tag">
                                        Full Time
                                    </span>

                                    <span className="job-tag">
                                        {job.location}
                                    </span>

                                    {match?.matchAvailable && (
                                        <span className="job-tag">
                                            Resume Match: {match.matchPercentage}%
                                        </span>
                                    )}

                                </div>

                                {/* View Details */}

                                <button
                                    className="view-button"
                                    onClick={() =>
                                        navigate(`/jobs/${job.id}`)
                                    }
                                >
                                    View Details
                                </button>

                            </div>
                        );
                    })}

                </div>
            )}

        </div>
    );
}

export default Jobs;
