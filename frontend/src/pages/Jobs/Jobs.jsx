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

    useEffect(() => {

        const loadData = async () => {

            try {

                // =====================================
                // 1. Get logged-in student
                // =====================================

                const storedStudent =
                    localStorage.getItem("student");

                if (!storedStudent) {
                    navigate("/login");
                    return;
                }

                const student =
                    JSON.parse(storedStudent);

                const studentId = student.id;

                console.log(
                    "Logged-in student ID:",
                    studentId
                );


                // =====================================
                // 2. Load all jobs
                // =====================================

                const jobsData =
                    await getAllJobs();

                setJobs(jobsData);


                // =====================================
                // 3. Load AI Job Recommendations
                // =====================================

                const recommendationResponse =
                    await axios.get(
                        `http://localhost:8080/ai/job-recommendations/${studentId}`
                    );

                const recommendations =
                    recommendationResponse.data;

                console.log(
                    "AI Job Recommendations:",
                    recommendations
                );

                setRecommendedJobs(
                    recommendations
                );


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

    const filteredJobs =
        jobs.filter(job => {

            const matchesSearch =
                job.title
                    .toLowerCase()
                    .includes(
                        searchTerm.toLowerCase()
                    ) ||

                job.company
                    .toLowerCase()
                    .includes(
                        searchTerm.toLowerCase()
                    );


            const matchesLocation =
                job.location
                    .toLowerCase()
                    .includes(
                        location.toLowerCase()
                    );


            return (
                matchesSearch &&
                matchesLocation
            );

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

                <h2>
                    {error}
                </h2>

                <button
                    className="view-button"
                    onClick={() =>
                        window.location.reload()
                    }
                >
                    Try Again
                </button>

            </div>
        );

    }


    // =====================================
    // Page
    // =====================================

    return (

        <div className="jobs-page">


            {/* ============================= */}
            {/* Hero Section */}
            {/* ============================= */}

            <section className="jobs-hero">

                <h1>
                    Find Your Next Opportunity
                </h1>

                <p>
                    Discover jobs that match your
                    skills and career goals.
                </p>

            </section>


            {/* ============================= */}
            {/* Search Section */}
            {/* ============================= */}

            <div className="search-container">

                <input
                    type="text"
                    placeholder="Search by job title or company"
                    className="search-box"
                    value={searchTerm}
                    onChange={(e) =>
                        setSearchTerm(e.target.value)
                    }
                />


                <input
                    type="text"
                    placeholder="Location"
                    className="search-box"
                    value={location}
                    onChange={(e) =>
                        setLocation(e.target.value)
                    }
                />


                <button className="search-button">
                    Search
                </button>

            </div>


            {/* ============================= */}
            {/* AI Recommended Jobs */}
            {/* ============================= */}

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

                                const job =
                                    recommendation.job;

                                return (

                                    <div
                                        className="job-card recommended-card"
                                        key={
                                            `ai-${job.id}`
                                        }
                                    >

                                        {/* AI Match */}

                                        <div className="ai-match-badge">

                                            ⭐{" "}
                                            {
                                                recommendation.matchPercentage
                                            }%
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
                                                {job.salary
                                                    ?.toLocaleString(
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
                                                    {
                                                        recommendation.matchedRole
                                                    }
                                                </p>

                                            </div>

                                        )}


                                        {/* Matched Skills */}

                                        {recommendation
                                            .matchedSkills
                                            ?.length > 0 && (

                                            <div className="ai-skills">

                                                <strong>
                                                    ✓ Matched Skills
                                                </strong>

                                                <div className="ai-skill-list">

                                                    {recommendation
                                                        .matchedSkills
                                                        .map(
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

                                        {recommendation
                                            .missingSkills
                                            ?.length > 0 && (

                                            <div className="ai-skills">

                                                <strong>
                                                    ⚠ Skills to Improve
                                                </strong>

                                                <div className="ai-skill-list">

                                                    {recommendation
                                                        .missingSkills
                                                        .map(
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
                                                    {
                                                        recommendation.explanation
                                                    }
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
                                                navigate(
                                                    `/jobs/${job.id}`
                                                )
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


            {/* ============================= */}
            {/* Available Jobs */}
            {/* ============================= */}

            <h2 className="jobs-section-title">
                Available Jobs
            </h2>


            {filteredJobs.length === 0 ? (

                <p className="no-jobs">
                    No jobs found.
                </p>

            ) : (

                <div className="jobs-grid">

                    {filteredJobs.map(job => (

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
                                    {job.salary
                                        ?.toLocaleString(
                                            "en-IN"
                                        )}
                                </p>

                            </div>


                            {/* Tags */}

                            <div className="job-tags">

                                <span className="job-tag">
                                    Full Time
                                </span>

                                <span className="job-tag">
                                    {job.location}
                                </span>

                            </div>


                            {/* View Details */}

                            <button
                                className="view-button"
                                onClick={() =>
                                    navigate(
                                        `/jobs/${job.id}`
                                    )
                                }
                            >
                                View Details
                            </button>

                        </div>

                    ))}

                </div>

            )}

        </div>

    );

}

export default Jobs;