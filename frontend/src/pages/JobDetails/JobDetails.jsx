import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { getJobById, applyForJob } from "../../services/jobService";
import "./JobDetails.css";

function JobDetails() {

    const { id } = useParams();
    const navigate = useNavigate();

    const [job, setJob] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const [applying, setApplying] = useState(false);
    const [applicationMessage, setApplicationMessage] = useState("");

    useEffect(() => {

        const loadJob = async () => {

            try {

                const data = await getJobById(id);

                setJob(data);

            } catch (error) {

                console.error("Error loading job:", error);

                setError("Unable to load job details.");

            } finally {

                setLoading(false);

            }
        };

        loadJob();

    }, [id]);


    const handleApply = async () => {

        try {

            setApplying(true);
            setApplicationMessage("");

            // Temporary student ID for testing
            const studentId = 1;

            await applyForJob(studentId, id);

            setApplicationMessage(
                "Application submitted successfully!"
            );

        } catch (error) {

            console.error("Error applying for job:", error);

            if (error.response?.data) {

                setApplicationMessage(error.response.data);

            } else {

                setApplicationMessage(
                    "Unable to submit application."
                );

            }

        } finally {

            setApplying(false);

        }
    };


    if (loading) {
        return <h2>Loading job details...</h2>;
    }

    if (error) {
        return <h2>{error}</h2>;
    }

    if (!job) {
        return <h2>Job not found.</h2>;
    }


    return (
        <div className="job-details-page">

            <button
                className="back-button"
                onClick={() => navigate("/jobs")}
            >
                ← Back to Jobs
            </button>


            <div className="job-details-card">

                <div className="job-details-header">

                    <div className="details-company-logo">
                        {job.company.charAt(0).toUpperCase()}
                    </div>

                    <div>

                        <h1>{job.title}</h1>

                        <p className="details-company">
                            {job.company}
                        </p>

                    </div>

                </div>


                <div className="job-details-info">

                    <div className="details-item">

                        <span>📍</span>

                        <div>
                            <strong>Location</strong>
                            <p>{job.location}</p>
                        </div>

                    </div>


                    <div className="details-item">

                        <span>💰</span>

                        <div>
                            <strong>Salary</strong>

                            <p>
                                ₹{job.salary.toLocaleString("en-IN")}
                            </p>

                        </div>

                    </div>


                    <div className="details-item">

                        <span>💼</span>

                        <div>
                            <strong>Employment</strong>
                            <p>Full Time</p>
                        </div>

                    </div>

                </div>


                <hr />


                <section className="job-description">

                    <h2>About This Job</h2>

                    <p>
                        Join {job.company} as a {job.title}.
                        This opportunity is based in {job.location}.
                    </p>

                    <p>
                        Explore this opportunity and take the next
                        step in your career journey with CareerConnect.
                    </p>

                </section>


                <button
                    className="apply-button"
                    onClick={handleApply}
                    disabled={applying}
                >
                    {applying ? "Applying..." : "Apply Now"}
                </button>


                {applicationMessage && (
                    <p className="application-message">
                        {applicationMessage}
                    </p>
                )}

            </div>

        </div>
    );
}

export default JobDetails;