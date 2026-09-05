import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import "./Dashboard.css";

function Dashboard() {

    const navigate = useNavigate();

    const [profile, setProfile] = useState(null);
    const [analysis, setAnalysis] = useState(null);
    const [applications, setApplications] = useState([]);

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    // Get logged-in student from localStorage
    const student = JSON.parse(localStorage.getItem("student"));
    const studentId = student?.id;

    useEffect(() => {

        // Check if user is logged in
        if (!studentId) {
            navigate("/login");
            return;
        }

        const loadDashboard = async () => {

            try {

                const [
                    profileResponse,
                    analysisResponse,
                    applicationsResponse
                ] = await Promise.all([

                    // Load student profile
                    axios.get(
                        `http://localhost:8080/student-profiles/${studentId}`
                    ),

                    // Load career analysis
                    // If analysis does not exist, create it
                    axios.get(
                        `http://localhost:8080/career-analysis/${studentId}`
                    ).catch(async (error) => {

                        if (error.response?.status === 404) {

                            const createResponse = await axios.post(
                                `http://localhost:8080/career-analysis/${studentId}`
                            );

                            return createResponse;
                        }

                        throw error;
                    }),

                    // Load applications
                    axios.get(
                        `http://localhost:8080/applications/student/${studentId}`
                    )

                ]);

                setProfile(profileResponse.data);
                setAnalysis(analysisResponse.data);
                setApplications(applicationsResponse.data);

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


    return (

        <div className="dashboard-page">

            {/* ============================= */}
            {/* Header */}
            {/* ============================= */}

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

                </div>

                <button
                    className="dashboard-profile-button"
                    onClick={() => navigate("/profile")}
                >
                    View Profile
                </button>

            </section>


            {/* ============================= */}
            {/* Statistics */}
            {/* ============================= */}

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


            {/* ============================= */}
            {/* Main Dashboard */}
            {/* ============================= */}

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