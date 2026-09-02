import { useEffect, useState } from "react";
import {
    getApplicationsByStudent,
    updateApplicationStatus
} from "../../services/applicationService";
import "./Applications.css";

function Applications() {

    const [applications, setApplications] = useState([]);
    const [loading, setLoading] = useState(true);
    const [message, setMessage] = useState("");

    const studentId = 1;

    useEffect(() => {
        loadApplications();
    }, []);

    const loadApplications = async () => {
        try {
            const data = await getApplicationsByStudent(studentId);
            setApplications(data);
        } catch (error) {
            console.error(error);
            setMessage("Failed to load applications");
        } finally {
            setLoading(false);
        }
    };

    const handleStatusUpdate = async (applicationId, status) => {

        try {
            await updateApplicationStatus(applicationId, status);

            setMessage(`Application ${status.toLowerCase()} successfully`);

            await loadApplications();

        } catch (error) {
            console.error(error);

            setMessage(
                error.response?.data || "Failed to update application"
            );
        }
    };

    if (loading) {
        return (
            <div className="applications-page">
                <h1>Applications</h1>
                <p>Loading applications...</p>
            </div>
        );
    }

    return (
        <div className="applications-page">

            <h1>Application Management</h1>

            <p className="applications-subtitle">
                Manage student job applications.
            </p>

            {message && (
                <p className="application-message">
                    {message}
                </p>
            )}

            {applications.length === 0 ? (

                <p>No applications found.</p>

            ) : (

                applications.map((application) => (

                    <div
                        className="application-card"
                        key={application.id}
                    >

                        <div>
                            <h2>{application.job.title}</h2>

                            <p>{application.job.company}</p>

                            <p>
                                📍 {application.job.location}
                            </p>

                            <p>
                                Applied on: {application.appliedDate}
                            </p>
                        </div>

                        <div className="application-status">

                            <span>
                                {application.status}
                            </span>

                            {application.status === "APPLIED" && (
                                <div className="application-actions">

                                    <button
                                        className="accept-btn"
                                        onClick={() =>
                                            handleStatusUpdate(
                                                application.id,
                                                "ACCEPTED"
                                            )
                                        }
                                    >
                                        Accept
                                    </button>

                                    <button
                                        className="reject-btn"
                                        onClick={() =>
                                            handleStatusUpdate(
                                                application.id,
                                                "REJECTED"
                                            )
                                        }
                                    >
                                        Reject
                                    </button>

                                </div>
                            )}

                        </div>

                    </div>

                ))
            )}

        </div>
    );
}

export default Applications;