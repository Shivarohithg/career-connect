import { useEffect, useState } from "react";
import { getApplicationsByStudent } from "../../services/applicationService";
import "./MyApplications.css";

function MyApplications() {

    const [applications, setApplications] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {

        const loadApplications = async () => {

            try {

                // Temporary student ID for testing
                const studentId = 1;

                const data =
                    await getApplicationsByStudent(studentId);

                setApplications(data);

            } catch (error) {

                console.error(
                    "Error loading applications:",
                    error
                );

                setError("Unable to load applications.");

            } finally {

                setLoading(false);

            }
        };

        loadApplications();

    }, []);

    if (loading) {
        return <h2>Loading applications...</h2>;
    }

    if (error) {
        return <h2>{error}</h2>;
    }

    return (
        <div className="applications-page">

            <div className="applications-header">

                <h1>My Applications</h1>

                <p>
                    Track the jobs you have applied for.
                </p>

            </div>

            {applications.length === 0 ? (

                <p className="no-applications">
                    You have not applied for any jobs yet.
                </p>

            ) : (

                <div className="applications-grid">

                    {applications.map((application) => (

                        <div
                            className="application-card"
                            key={application.id}
                        >

                            <div className="application-header">

                                <div className="company-logo">
                                    {application.job.company
                                        .charAt(0)
                                        .toUpperCase()}
                                </div>

                                <div>

                                    <h2>
                                        {application.job.title}
                                    </h2>

                                    <p>
                                        {application.job.company}
                                    </p>

                                </div>

                            </div>

                            <div className="application-details">

                                <p>
                                    📍 {application.job.location}
                                </p>

                                <p>
                                    💰 ₹
                                    {application.job.salary.toLocaleString(
                                        "en-IN"
                                    )}
                                </p>

                                <p>
                                    📅 Applied on:{" "}
                                    {application.appliedDate}
                                </p>

                            </div>

                            <div className="application-status">

                                <span>
                                    {application.status}
                                </span>

                            </div>

                        </div>

                    ))}

                </div>

            )}

        </div>
    );
}

export default MyApplications;