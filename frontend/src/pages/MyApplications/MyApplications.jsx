import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getApplicationsByStudent } from "../../services/applicationService";
import "./MyApplications.css";

function MyApplications() {

    const navigate = useNavigate();

    const [applications, setApplications] = useState([]);
    const [loading, setLoading] = useState(true);
    const [refreshing, setRefreshing] = useState(false);
    const [error, setError] = useState("");

    const loadApplications = useCallback(async (isRefresh = false) => {

        try {

            if (isRefresh) {
                setRefreshing(true);
                setError("");
            }

            const storedStudent = localStorage.getItem("student");

            if (!storedStudent) {
                setLoading(false);
                navigate("/login");
                return;
            }

            const student = JSON.parse(storedStudent);

            if (!student.id) {
                setLoading(false);
                setError("Invalid student information. Please login again.");
                return;
            }

            const data = await getApplicationsByStudent(student.id);

            if (!Array.isArray(data)) {
                throw new Error(
                    "Invalid applications data received from server."
                );
            }

            setApplications(data);
            setError("");

        } catch (error) {

            console.error("Error loading applications:", error);

            if (error.response) {

                setError(
                    typeof error.response.data === "string"
                        ? error.response.data
                        : "Server error while loading applications."
                );

            } else if (error.request) {

                setError(
                    "Cannot connect to the Spring Boot server. Make sure the backend is running on port 8080."
                );

            } else {

                setError(
                    error.message || "Unable to load applications."
                );
            }

        } finally {

            setLoading(false);
            setRefreshing(false);

        }

    }, [navigate]);


    useEffect(() => {

        loadApplications();

    }, [loadApplications]);


    const totalApplications = applications.length;

    const appliedCount = applications.filter(
        (application) =>
            application.status?.toUpperCase() === "APPLIED"
    ).length;

    const interviewCount = applications.filter(
        (application) =>
            application.status?.toUpperCase() === "INTERVIEW"
    ).length;

    const rejectedCount = applications.filter(
        (application) =>
            application.status?.toUpperCase() === "REJECTED"
    ).length;


    if (loading) {

        return (
            <div className="applications-page">

                <div className="applications-header">

                    <h1>My Applications</h1>

                    <p>
                        Loading your applications...
                    </p>

                </div>

            </div>
        );
    }


    return (

        <div className="applications-page">

            <div className="applications-header">

                <div>
                    <h1>My Applications</h1>

                    <p>
                        Track the jobs you have applied for.
                    </p>
                </div>

                <button
                    className="refresh-button"
                    onClick={() => loadApplications(true)}
                    disabled={refreshing}
                >
                    {refreshing
                        ? "Refreshing..."
                        : "↻ Refresh"}
                </button>

            </div>


            {!error && (

                <div className="application-summary">

                    <div className="summary-card">

                        <span className="summary-number">
                            {totalApplications}
                        </span>

                        <span className="summary-label">
                            Total
                        </span>

                    </div>


                    <div className="summary-card">

                        <span className="summary-number">
                            {appliedCount}
                        </span>

                        <span className="summary-label">
                            Applied
                        </span>

                    </div>


                    <div className="summary-card">

                        <span className="summary-number">
                            {interviewCount}
                        </span>

                        <span className="summary-label">
                            Interview
                        </span>

                    </div>


                    <div className="summary-card">

                        <span className="summary-number">
                            {rejectedCount}
                        </span>

                        <span className="summary-label">
                            Rejected
                        </span>

                    </div>

                </div>

            )}


            {error && (

                <div className="applications-error">

                    <p>{error}</p>

                    <button
                        onClick={() => loadApplications(true)}
                    >
                        Try Again
                    </button>

                </div>

            )}


            {!error && applications.length === 0 && (

                <div className="no-applications">

                    <div className="empty-icon">
                        📄
                    </div>

                    <h2>
                        No Applications Yet
                    </h2>

                    <p>
                        You have not applied for any jobs yet.
                    </p>

                    <button
                        onClick={() => navigate("/jobs")}
                    >
                        Browse Jobs
                    </button>

                </div>

            )}


            {!error && applications.length > 0 && (

                <div className="applications-section">

                    <h2 className="applications-section-title">
                        Your Applications
                    </h2>


                    <div className="applications-grid">

                        {applications.map((application) => {

                            const job = application.job;

                            const status =
                                application.status || "APPLIED";

                            return (

                                <div
                                    className="application-card"
                                    key={application.id}
                                >

                                    <div className="application-header">

                                        <div className="company-logo">

                                            {job?.company
                                                ? job.company
                                                    .charAt(0)
                                                    .toUpperCase()
                                                : "C"}

                                        </div>


                                        <div className="application-job-info">

                                            <h2>
                                                {job?.title || "Job"}
                                            </h2>

                                            <p>
                                                {job?.company || "Company"}
                                            </p>

                                        </div>

                                    </div>


                                    <div className="application-details">

                                        <div className="application-detail">

                                            <span>📍</span>

                                            <div>
                                                <strong>Location</strong>

                                                <p>
                                                    {job?.location ||
                                                        "Not available"}
                                                </p>
                                            </div>

                                        </div>


                                        <div className="application-detail">

                                            <span>💰</span>

                                            <div>
                                                <strong>Salary</strong>

                                                <p>
                                                    {typeof job?.salary ===
                                                    "number"
                                                        ? `₹${job.salary.toLocaleString(
                                                            "en-IN"
                                                        )}`
                                                        : "Not available"}
                                                </p>
                                            </div>

                                        </div>


                                        <div className="application-detail">

                                            <span>📅</span>

                                            <div>
                                                <strong>Applied On</strong>

                                                <p>
                                                    {application.appliedDate ||
                                                        "N/A"}
                                                </p>
                                            </div>

                                        </div>

                                    </div>


                                    <div className="application-footer">

                                        <span
                                            className={`application-status status-${status
                                                .toLowerCase()
                                                .replace(/\s+/g, "-")}`}
                                        >
                                            {status}
                                        </span>


                                        {job?.id && (

                                            <button
                                                className="view-job-button"
                                                onClick={() =>
                                                    navigate(
                                                        `/jobs/${job.id}`
                                                    )
                                                }
                                            >
                                                View Job
                                            </button>

                                        )}

                                    </div>

                                </div>

                            );

                        })}

                    </div>

                </div>

            )}

        </div>

    );
}

export default MyApplications;