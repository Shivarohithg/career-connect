import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import "./Dashboard.css";
function Dashboard() {
    const navigate = useNavigate();
    const [profile, setProfile] = useState(null);
    const [analysis, setAnalysis] = useState(null);
    const [applications, setApplications] = useState([]);
    const [employability, setEmployability] = useState(null);
    const [leetcode, setLeetcode] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const student = JSON.parse(localStorage.getItem("student"));
    const studentId = student?.id;
    useEffect(() => {
        if (!studentId) {
            navigate("/login");
            return;
        }
        const loadDashboard = async () => {
            try {
                const [
                    profileResponse,
                    analysisResponse,
                    applicationsResponse,
                    employabilityResponse,
                    leetcodeResponse
                ] = await Promise.all([
                    axios.get(
                        `http://localhost:8080/student-profiles/${studentId}`
                    ),
                    axios.get(
                        `http://localhost:8080/career-analysis/${studentId}`
                    ).catch(async (error) => {
                        if (error.response?.status === 404) {
                            return await axios.post(
                                `http://localhost:8080/career-analysis/${studentId}`
                            );
                        }
                        throw error;
                    }),
                    axios.get(
                        `http://localhost:8080/applications/student/${studentId}`
                    ),
                    // Employability score is independent
                    axios.get(
                        `http://localhost:8080/employability-score/${studentId}`
                    ).catch((error) => {
                        console.error(
                            "Employability score loading error:",
                            error
                        );
                        return null;
                    }),
                    // LeetCode intelligence is independent of the other dashboard data
                    axios.get(
                        `http://localhost:8080/leetcode-analysis/${studentId}`
                    ).catch((error) => {
                        console.error(
                            "LeetCode analysis loading error:",
                            error
                        );
                        return null;
                    })
                ]);
                setProfile(profileResponse.data);
                setAnalysis(analysisResponse.data);
                setApplications(applicationsResponse.data);
                setEmployability(
                    employabilityResponse?.data || null
                );
                setLeetcode(leetcodeResponse?.data || null);
                console.log(
    "Employability API Response:",
    employabilityResponse?.data
);
            } catch (error) {
                console.error("Dashboard loading error:", error);
                setError("Unable to load dashboard.");
            } finally {
                setLoading(false);
            }
        };
        loadDashboard();
    }, [studentId, navigate]);
    if (loading) {
        return (
            <h2 className="dashboard-loading">
                Loading dashboard...
            </h2>
        );
    }
    if (error) {
        return (
            <h2 className="dashboard-error">
                {error}
            </h2>
        );
    }
    const acceptedCount = applications.filter(
        application => application.status === "ACCEPTED"
    ).length;
    const rejectedCount = applications.filter(
        application => application.status === "REJECTED"
    ).length;
    const appliedCount = applications.filter(
        application => application.status === "APPLIED"
    ).length;
    const skills = profile?.skills
        ? profile.skills
            .split(",")
            .map(skill => skill.trim())
            .filter(skill => skill.length > 0)
        : [];
    const recommendedRoles = analysis?.recommendedRoles
        ? analysis.recommendedRoles
            .split(",")
            .map(role => role.trim())
            .slice(0, 3)
        : [];
    // Calculate profile completion
    const profileFields = [
        profile?.student?.name,
        profile?.student?.branch,
        profile?.student?.cgpa,
        profile?.skills,
        profile?.projects,
        profile?.githubUsername,
        profile?.leetcodeUsername,
        profile?.hackerrankUsername,
        profile?.resumePath
    ];
    const completedFields = profileFields.filter(
        field =>
            field !== null &&
            field !== undefined &&
            String(field).trim() !== ""
    ).length;
    const profileCompletion = Math.round(
        (completedFields / profileFields.length) * 100
    );
    // Employability score display helpers
    const scoreColor = (score) => {
        if (score >= 80) return "#16a34a";
        if (score >= 60) return "#2563eb";
        if (score >= 40) return "#d97706";
        return "#dc2626";
    };
    // LeetCode performance insights are derived from the available solved counts.
    const leetcodeTotal = Number(leetcode?.totalSolved || 0);
    const leetcodeEasy = Number(leetcode?.easySolved || 0);
    const leetcodeMedium = Number(leetcode?.mediumSolved || 0);
    const leetcodeHard = Number(leetcode?.hardSolved || 0);
    const leetcodeDistribution = [
        { label: "Easy", solved: leetcodeEasy, className: "easy" },
        { label: "Medium", solved: leetcodeMedium, className: "medium" },
        { label: "Hard", solved: leetcodeHard, className: "hard" }
    ].map(item => ({
        ...item,
        percentage: leetcodeTotal > 0
            ? Math.round((item.solved / leetcodeTotal) * 100)
            : 0
    }));
    let leetcodeFocus = "Start with Easy problems to build core problem-solving patterns.";
    let leetcodeSummary = "No solved-problem data is available yet. Start solving problems to build your coding profile.";
    if (leetcodeTotal > 0) {
        if (leetcodeEasy === 0) {
            leetcodeFocus = "Build your fundamentals with Easy problems before increasing difficulty.";
        } else if (leetcodeMedium === 0) {
            leetcodeFocus = "You have started with Easy problems. Begin practicing Medium problems to strengthen pattern application.";
        } else if (leetcodeHard === 0) {
            leetcodeFocus = "Continue practicing Easy and Medium problems. Attempt Hard problems after you feel confident with core patterns.";
        } else {
            leetcodeFocus = "You have solved problems across all three difficulty levels. Keep practicing mixed sets to maintain breadth.";
        }
        if (leetcodeTotal < 10) {
            leetcodeSummary = `You have solved ${leetcodeTotal} problems so far. This is an early sample, so focus on building a consistent foundation before drawing conclusions about your strengths.`;
        } else if (leetcodeEasy > leetcodeMedium + leetcodeHard) {
            leetcodeSummary = "Most of your solved problems are Easy. Gradually increase Medium practice to develop stronger problem-solving patterns.";
        } else if (leetcodeMedium > leetcodeEasy + leetcodeHard) {
            leetcodeSummary = "Medium problems make up the largest part of your solved set. Keep revisiting fundamentals and attempt a few Hard problems when ready.";
        } else if (leetcodeHard > leetcodeEasy + leetcodeMedium) {
            leetcodeSummary = "Hard problems make up the largest part of your solved set. Continue balancing advanced practice with fundamentals.";
        } else {
            leetcodeSummary = "Your solved problems are spread across difficulty levels. Continue practicing mixed problems to maintain a balanced profile.";
        }
    }
    const leetcodeRecommendations = [
        leetcodeFocus,
        leetcodeTotal < 10
            ? "Aim for a small, regular practice routine and review the approach behind every accepted solution."
            : "Re-solve previously completed problems after a few days to check retention and improve solution quality."
    ];
    return (
        <div className="dashboard-page">
            {/* Dashboard Header */}
            <section className="dashboard-header">
                <div>
                    <p className="dashboard-label">
                        CAREER DASHBOARD
                    </p>
                    <h1>
                        Welcome, {profile?.student?.name}
                    </h1>
                    <p>
                        Track your career progress, applications and
                        recommendations in one place.
                    </p>
                    <div className="profile-completion">
                        <div className="completion-info">
                            <span>Profile Completion</span>
                            <strong>{profileCompletion}%</strong>
                        </div>
                        <div className="completion-track">
                            <div
                                className="completion-fill"
                                style={{
                                    width: `${profileCompletion}%`
                                }}
                            ></div>
                        </div>
                    </div>
                </div>
                <button
                    className="dashboard-profile-button"
                    onClick={() => navigate("/profile")}
                >
                    View Profile
                </button>
            </section>
            {/* Employability Score */}
            <section className="employability-section">
                <div className="employability-header">
                    <div>
                        <p className="employability-label">
                            CAREER INTELLIGENCE
                        </p>
                        <h2>AI Employability Score</h2>
                        <p>
                            Understand your career readiness through
                            your resume, skills, projects and interview
                            performance.
                        </p>
                    </div>
                    <span className="employability-icon">
                        ✦
                    </span>
                </div>
                {employability ? (
                    <>
                        <div className="employability-main">
                            <div className="employability-score-circle"
                                style={{
                                    "--score-color": scoreColor(
                                        employability.overallScore
                                    ),
                                    "--score-progress":
                                        `${Math.min(
                                            100,
                                            Math.max(
                                                0,
                                                employability.overallScore
                                            )
                                        ) * 3.6}deg`
                                }}
                            >
                                <div className="score-circle-inner">
                                    <strong>
                                        {Number(
                                            employability.overallScore
                                        ).toFixed(1)}
                                    </strong>
                                    <span>/100</span>
                                </div>
                            </div>
                            <div className="employability-summary">
                                <span className="readiness-badge">
                                    {employability.readinessLevel}
                                </span>
                                <h3>
                                    Your Career Readiness
                                </h3>
                                <p>
                                    Your current score is calculated
                                    using the information and assessments
                                    available in your CareerConnect profile.
                                </p>
                                <div className="assessment-info">
                                    <span>
                                        Assessment Coverage
                                    </span>
                                    <strong>
                                        {employability.assessmentCoverage}%
                                    </strong>
                                </div>
                                <div className="assessment-track">
                                    <div
                                        className="assessment-fill"
                                        style={{
                                            width: `${employability.assessmentCoverage}%`
                                        }}
                                    ></div>
                                </div>
                                <small>
                                    {employability.assessedComponents}
                                    {" "}of{" "}
                                    {employability.totalComponents}
                                    {" "}components assessed
                                </small>
                            </div>
                        </div>
                        <div className="employability-components">
                            <h3>Score Breakdown</h3>
                            <div className="component-grid">
                                {employability.components?.map(
                                    (component, index) => (
                                        <div
                                            className="component-card"
                                            key={index}
                                        >
                                            <div className="component-top">
                                                <strong>
                                                    {component.component}
                                                </strong>
                                                <span
                                                    className={
                                                        component.status === "ASSESSED"
                                                            ? "component-status assessed"
                                                            : "component-status not-assessed"
                                                    }
                                                >
                                                    {component.status === "ASSESSED"
                                                        ? "Assessed"
                                                        : "Not assessed"}
                                                </span>
                                            </div>
                                            <div className="component-score">
                                                {component.status === "ASSESSED"
                                                    ? `${component.score}/${component.maximumScore}`
                                                    : "Pending"}
                                            </div>
                                            {component.status === "ASSESSED" && (
                                                <div className="component-progress">
                                                    <div
                                                        style={{
                                                            width: `${Math.min(
                                                                100,
                                                                (component.score /
                                                                    component.maximumScore) * 100
                                                            )}%`
                                                        }}
                                                    ></div>
                                                </div>
                                            )}
                                            <p>
                                                {component.explanation}
                                            </p>
                                        </div>
                                    )
                                )}
                            </div>
                        </div>
                        {employability.recommendations?.length > 0 && (
                            <div className="employability-recommendations">
                                <h3>
                                    <span>✦</span> Recommended Improvements
                                </h3>
                                <ul>
                                    {employability.recommendations
                                        .slice(0, 3)
                                        .map((recommendation, index) => (
                                            <li key={index}>
                                                {recommendation}
                                            </li>
                                        ))}
                                </ul>
                            </div>
                        )}
                    </>
                ) : (
                    <div className="employability-unavailable">
                        <h3>
                            Your employability assessment is unavailable
                        </h3>
                        <p>
                            We couldn't retrieve your score right now.
                            Your other dashboard information is still available.
                        </p>
                        <button
                            onClick={() => window.location.reload()}
                        >
                            Try Again
                        </button>
                    </div>
                )}
            </section>
            {/* LeetCode Intelligence */}
            <section className="leetcode-section">
                <div className="leetcode-header">
                    <div>
                        <p className="leetcode-label">CODING PROFILE</p>
                        <h2>LeetCode Intelligence</h2>
                        <p>
                            Track your problem-solving progress, difficulty
                            distribution and coding consistency.
                        </p>
                    </div>
                    <span className="leetcode-icon">⌘</span>
                </div>
                {leetcode ? (
                    <>
                        <div className="leetcode-overview">
                            <div className="leetcode-total-card">
                                <span className="leetcode-stat-caption">
                                    Problems Solved
                                </span>
                                <strong>{leetcode.totalSolved}</strong>
                                <span className="leetcode-stat-foot">
                                    Across all difficulty levels
                                </span>
                            </div>
                            <div className="leetcode-rank-card">
                                <span className="leetcode-stat-caption">
                                    Global Ranking
                                </span>
                                <strong>
                                    {leetcode.globalRanking != null
                                        ? Number(leetcode.globalRanking).toLocaleString()
                                        : "Unranked"}
                                </strong>
                                <span className="leetcode-stat-foot">
                                    {leetcode.username}
                                </span>
                            </div>
                        </div>
                        <div className="leetcode-details">
                            <div className="leetcode-difficulty-card">
                                <h3>Difficulty Breakdown</h3>
                                {[
                                    {
                                        label: "Easy",
                                        solved: leetcode.easySolved,
                                        className: "easy"
                                    },
                                    {
                                        label: "Medium",
                                        solved: leetcode.mediumSolved,
                                        className: "medium"
                                    },
                                    {
                                        label: "Hard",
                                        solved: leetcode.hardSolved,
                                        className: "hard"
                                    }
                                ].map((item) => (
                                    <div className="leetcode-difficulty-row" key={item.label}>
                                        <div className="leetcode-difficulty-info">
                                            <span className={item.className}>
                                                {item.label}
                                            </span>
                                            <strong>{item.solved}</strong>
                                        </div>
                                        <div className="leetcode-progress-track">
                                            <div
                                                className={`leetcode-progress-fill ${item.className}`}
                                                style={{
                                                    width: `${leetcode.totalSolved > 0
                                                        ? (item.solved / leetcode.totalSolved) * 100
                                                        : 0}%`
                                                }}
                                            ></div>
                                        </div>
                                    </div>
                                ))}
                            </div>
                            <div className="leetcode-activity-card">
                                <h3>Coding Activity</h3>
                                <div className="leetcode-activity-grid">
                                    <div className="leetcode-activity-item">
                                        <span>Active Days</span>
                                        <strong>{leetcode.totalActiveDays}</strong>
                                    </div>
                                    <div className="leetcode-activity-item">
                                        <span>Current Streak</span>
                                        <strong>{leetcode.currentStreak} days</strong>
                                    </div>
                                    <div className="leetcode-activity-item">
                                        <span>Contests Attended</span>
                                        <strong>
                                            {leetcode.contestsAttended != null
                                                ? leetcode.contestsAttended
                                                : "Not participated"}
                                        </strong>
                                    </div>
                                    <div className="leetcode-activity-item">
                                        <span>Contest Rating</span>
                                        <strong>
                                            {leetcode.contestRating != null
                                                ? Math.round(leetcode.contestRating)
                                                : "N/A"}
                                        </strong>
                                    </div>
                                </div>
                            </div>
                        </div>
                        {/* LeetCode Performance Insights */}
                        <div className="leetcode-insights">
                            <div className="leetcode-insights-heading">
                                <div>
                                    <p className="leetcode-label">PERFORMANCE REVIEW</p>
                                    <h3>Your DSA Progress Insights</h3>
                                </div>
                                <span className="leetcode-insights-tag">Based on solved problems</span>
                            </div>
                            <p className="leetcode-insights-summary">
                                {leetcodeSummary}
                            </p>
                            <div className="leetcode-distribution">
                                {leetcodeDistribution.map(item => (
                                    <div className="leetcode-distribution-item" key={item.label}>
                                        <div className="leetcode-distribution-top">
                                            <span className={item.className}>{item.label}</span>
                                            <strong>{item.percentage}%</strong>
                                        </div>
                                        <div className="leetcode-progress-track">
                                            <div
                                                className={`leetcode-progress-fill ${item.className}`}
                                                style={{ width: `${item.percentage}%` }}
                                            ></div>
                                        </div>
                                    </div>
                                ))}
                            </div>
                            <div className="leetcode-practice-focus">
                                <h4>Recommended Next Steps</h4>
                                <ul>
                                    {leetcodeRecommendations.map((recommendation, index) => (
                                        <li key={index}>{recommendation}</li>
                                    ))}
                                </ul>
                            </div>
                        </div>
                        <div className="leetcode-footer">
                            <span>
                                LeetCode profile: <strong>{leetcode.username}</strong>
                            </span>
                            <a
                                href={`https://leetcode.com/u/${encodeURIComponent(leetcode.username)}/`}
                                target="_blank"
                                rel="noreferrer"
                            >
                                View LeetCode Profile ↗
                            </a>
                        </div>
                        <p className="leetcode-note">
                            Activity and ranking are shown as returned by
                            LeetCode. Contest details remain unavailable until
                            you participate in a contest.
                        </p>
                    </>
                ) : (
                    <div className="leetcode-unavailable">
                        <h3>LeetCode data is not available</h3>
                        <p>
                            Add your LeetCode username in your profile and make
                            sure it is saved correctly. Your other dashboard
                            information is still available.
                        </p>
                        <button onClick={() => navigate("/profile")}>
                            Update Profile
                        </button>
                    </div>
                )}
            </section>
            {/* Statistics */}
            <section className="dashboard-stats">
                <div className="stat-card">
                    <h3>Applications</h3>
                    <p>{applications.length}</p>
                    <span>Total applications</span>
                </div>
                <div className="stat-card">
                    <h3>Applied</h3>
                    <p>{appliedCount}</p>
                    <span>Pending applications</span>
                </div>
                <div className="stat-card">
                    <h3>Accepted</h3>
                    <p>{acceptedCount}</p>
                    <span>Successful applications</span>
                </div>
                <div className="stat-card">
                    <h3>Rejected</h3>
                    <p>{rejectedCount}</p>
                    <span>Rejected applications</span>
                </div>
            </section>
            {/* Main Dashboard */}
            <section className="dashboard-grid">
                {/* Profile Summary */}
                <div className="dashboard-card">
                    <div className="card-header">
                        <h2>👤 Profile Summary</h2>
                        <button
                            onClick={() => navigate("/profile")}
                        >
                            Edit
                        </button>
                    </div>
                    <p>
                        <strong>Branch:</strong>{" "}
                        {profile?.student?.branch}
                    </p>
                    <p>
                        <strong>CGPA:</strong>{" "}
                        {profile?.student?.cgpa}
                    </p>
                    <div className="skills-container">
                        <strong>Skills:</strong>
                        <div className="skill-tags">
                            {skills.length > 0 ? (
                                skills.map((skill, index) => (
                                    <span
                                        className="skill-tag"
                                        key={index}
                                    >
                                        {skill}
                                    </span>
                                ))
                            ) : (
                                <span>No skills added</span>
                            )}
                        </div>
                    </div>
                </div>
                {/* Career Analysis */}
                <div className="dashboard-card">
                    <div className="card-header">
                        <h2>📊 Career Analysis</h2>
                        <button
                            onClick={() =>
                                navigate("/career-analysis")
                            }
                        >
                            View
                        </button>
                    </div>
                    <p className="analysis-description">
                        Recommended career roles based on your
                        skills and profile.
                    </p>
                    <div className="role-list">
                        {recommendedRoles.length > 0 ? (
                            recommendedRoles.map((role, index) => (
                                <div
                                    className="role-item"
                                    key={index}
                                >
                                    {role}
                                </div>
                            ))
                        ) : (
                            <p>
                                No career analysis available.
                            </p>
                        )}
                    </div>
                </div>
                {/* Job Recommendations */}
                <div className="dashboard-card">
                    <div className="card-header">
                        <h2>⭐ Job Recommendations</h2>
                        <button
                            onClick={() => navigate("/jobs")}
                        >
                            View Jobs
                        </button>
                    </div>
                    <p>
                        Find opportunities based on your
                        profile and career analysis.
                    </p>
                    <button
                        className="primary-dashboard-button"
                        onClick={() => navigate("/jobs")}
                    >
                        Explore Recommended Jobs
                    </button>
                </div>
                {/* Applications */}
                <div className="dashboard-card">
                    <div className="card-header">
                        <h2>📝 Applications</h2>
                        <button
                            onClick={() =>
                                navigate("/my-applications")
                            }
                        >
                            View All
                        </button>
                    </div>
                    {applications.length === 0 ? (
                        <p>
                            You haven't applied for any jobs yet.
                        </p>
                    ) : (
                        <div className="recent-applications">
                            {applications
                                .slice(-3)
                                .reverse()
                                .map(application => (
                                    <div
                                        className="recent-application"
                                        key={application.id}
                                    >
                                        <div>
                                            <strong>
                                                {application.job.title}
                                            </strong>
                                            <p>
                                                {application.job.company}
                                            </p>
                                        </div>
                                        <span
                                            className={`dashboard-status status-${application.status.toLowerCase()}`}
                                        >
                                            {application.status}
                                        </span>
                                    </div>
                                ))}
                        </div>
                    )}
                </div>
            </section>
        </div>
    );
}
export default Dashboard;
