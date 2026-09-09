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

            const studentId = student.id;

            console.log("Logged-in student ID:", studentId);

            const data = await getApplicationsByStudent(studentId);

            console.log("Applications received:", data);

            if (!Array.isArray(data)) {
                throw new Error("Invalid applications data received from server.");
            }

            setApplications(data);

            setError("");

        } catch (error) {

            console.error("FULL ERROR:", error);
            console.error("ERROR MESSAGE:", error.message);
            console.error("ERROR RESPONSE:", error.response);
            console.error("ERROR DATA:", error.response?.data);
            console.error("ERROR STATUS:", error.response?.status);

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
                    error.message ||
                    "Unable to load applications."
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

                <div className="applications-grid">

                    {applications.map((application) => {

                        const job = application.job;

                        return (

                            <div
                                className="application-card"
                                key={application.id}
                            >

                                <div className="application-header">

                                    <div className="company-logo">

                                        {job?.company
                                            ? job.company.charAt(0).toUpperCase()
                                            : "C"}

                                    </div>

                                    <div>

                                        <h2>
                                            {job?.title || "Job"}
                                        </h2>

                                        <p>
                                            {job?.company || "Company"}
                                        </p>

                                    </div>

                                </div>


                                <div className="application-details">

                                    <p>
                                        📍{" "}
                                        {job?.location || "Location not available"}
                                    </p>

                                    <p>
                                        💰 ₹
                                        {typeof job?.salary === "number"
                                            ? job.salary.toLocaleString("en-IN")
                                            : "Not available"}
                                    </p>

                                    <p>
                                        📅 Applied on:{" "}
                                        {application.appliedDate || "N/A"}
                                    </p>

                                </div>


                                <div className="application-status">

                                    <span
                                        className={`status-${application.status
                                            ?.toLowerCase()
                                            .replace(/\s+/g, "-")}`}
                                    >
                                        {application.status || "APPLIED"}
                                    </span>

                                </div>

                            </div>

                        );

                    })}

                </div>

            )}

        </div>

    );
}

export default MyApplications;