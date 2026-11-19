
import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { getJobById } from "../../services/jobService";
import { applyForJob } from "../../services/applicationService";
import "./JobDetails.css";

function JobDetails() {
    const { id } = useParams();
    const navigate = useNavigate();

    const [job, setJob] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [aiRecommendation, setAiRecommendation] = useState(null);
    const [aiLoading, setAiLoading] = useState(true);
    const [jobAIInsight, setJobAIInsight] = useState("");
    const [jobAIInsightLoading, setJobAIInsightLoading] = useState(true);
    const [jobAIInsightError, setJobAIInsightError] = useState("");
    const [eligibility, setEligibility] = useState(null);
    const [eligibilityLoading, setEligibilityLoading] = useState(true);
    const [eligibilityError, setEligibilityError] = useState("");
    const [roadmap, setRoadmap] = useState(null);
    const [roadmapLoading, setRoadmapLoading] = useState(true);
    const [roadmapError, setRoadmapError] = useState("");
    const [applying, setApplying] = useState(false);
    const [applicationMessage, setApplicationMessage] = useState("");

    useEffect(() => {
        const loadJob = async () => {
            try {
                setLoading(true);
                setError("");
                setJob(await getJobById(id));
            } catch (error) {
                console.error("Error loading job:", error);
                setError("Unable to load job details.");
            } finally {
                setLoading(false);
            }
        };
        loadJob();
    }, [id]);

    // Load formal eligibility assessment
    useEffect(() => {
        const loadEligibility = async () => {
            try {
                setEligibilityLoading(true);
                setEligibilityError("");
                setEligibility(null);

                const storedStudent = localStorage.getItem("student");
                if (!storedStudent) {
                    setEligibilityError("Please login to check your job eligibility.");
                    return;
                }

                const student = JSON.parse(storedStudent);
                if (!student.id || !id) {
                    setEligibilityError("Student or job information is missing.");
                    return;
                }

                const response = await fetch(
                    `http://localhost:8080/eligibility/${student.id}/${id}`
                );
                if (!response.ok) {
                    throw new Error("Unable to calculate job eligibility.");
                }

                setEligibility(await response.json());
            } catch (error) {
                console.error("Error loading eligibility:", error);
                setEligibilityError(error.message || "Unable to load eligibility.");
            } finally {
                setEligibilityLoading(false);
            }
        };

        loadEligibility();
    }, [id]);

    // Load job-specific learning roadmap
    useEffect(() => {
        const loadRoadmap = async () => {
            try {
                setRoadmapLoading(true);
                setRoadmapError("");
                setRoadmap(null);

                const storedStudent = localStorage.getItem("student");
                if (!storedStudent) {
                    setRoadmapError("Please login to view your personalized learning roadmap.");
                    return;
                }

                const student = JSON.parse(storedStudent);
                if (!student.id || !id) {
                    setRoadmapError("Student or job information is missing.");
                    return;
                }

                const response = await fetch(
                    `http://localhost:8080/learning-roadmap/${student.id}/${id}`
                );
                if (!response.ok) {
                    throw new Error("Unable to generate your learning roadmap.");
                }

                setRoadmap(await response.json());
            } catch (error) {
                console.error("Error loading learning roadmap:", error);
                setRoadmapError(error.message || "Unable to load learning roadmap.");
            } finally {
                setRoadmapLoading(false);
            }
        };

        loadRoadmap();
    }, [id]);

    useEffect(() => {
        const loadAIRecommendation = async () => {
            try {
                setAiLoading(true);
                const storedStudent = localStorage.getItem("student");

                if (!storedStudent) {
                    setAiRecommendation(null);
                    return;
                }

                const student = JSON.parse(storedStudent);
                const studentId = student.id;

                if (!studentId) {
                    setAiRecommendation(null);
                    return;
                }

                const response = await fetch(
                    `http://localhost:8080/ai/job-recommendations/${studentId}`
                );

                if (!response.ok) {
                    throw new Error("Unable to load AI recommendation.");
                }

                const recommendations = await response.json();

                const currentRecommendation = recommendations.find(
                    recommendation =>
                        String(recommendation.job?.id) === String(id)
                );

                setAiRecommendation(currentRecommendation || null);
            } catch (error) {
                console.error("Error loading AI recommendation:", error);
                setAiRecommendation(null);
            } finally {
                setAiLoading(false);
            }
        };

        loadAIRecommendation();
    }, [id]);

    useEffect(() => {
        const loadJobAIInsight = async () => {
            try {
                setJobAIInsightLoading(true);
                setJobAIInsightError("");
                setJobAIInsight("");

                const storedStudent = localStorage.getItem("student");

                if (!storedStudent) {
                    setJobAIInsightError(
                        "Please login to generate the AI job insight."
                    );
                    return;
                }

                const student = JSON.parse(storedStudent);
                const studentId = student.id;

                if (!studentId || !id) {
                    setJobAIInsightError(
                        "Student or job information is missing."
                    );
                    return;
                }

                const response = await fetch(
                    `http://localhost:8080/ai/job-insight/${studentId}/${id}`
                );

                if (!response.ok) {
                    const errorText = await response.text();
                    throw new Error(
                        errorText || "Unable to load AI job insight."
                    );
                }

                const insight = await response.text();

                if (!insight || !insight.trim()) {
                    throw new Error("Ollama returned an empty response.");
                }

                setJobAIInsight(insight.trim());
            } catch (error) {
                console.error("Error loading AI job insight:", error);
                setJobAIInsightError(
                    error.message || "Unable to load AI job insight."
                );
                setJobAIInsight("");
            } finally {
                setJobAIInsightLoading(false);
            }
        };

        loadJobAIInsight();
    }, [id]);

    const handleApply = async () => {
        try {
            setApplying(true);
            setApplicationMessage("");

            if (eligibility && !eligibility.eligible) {
                setApplicationMessage(
                    "You do not meet the academic eligibility requirements for this job."
                );
                return;
            }

            if (!eligibility) {
                setApplicationMessage(
                    "Eligibility could not be verified. Please try again before applying."
                );
                return;
            }

            const storedStudent = localStorage.getItem("student");

            if (!storedStudent) {
                setApplicationMessage("Please login first to apply for a job.");
                navigate("/login");
                return;
            }

            const student = JSON.parse(storedStudent);
            const studentId = student.id;

            if (!studentId) {
                setApplicationMessage(
                    "Student information is missing. Please login again."
                );
                return;
            }

            await applyForJob(studentId, id);

            setApplicationMessage("Application submitted successfully!");
        } catch (error) {
            console.error("Error applying for job:", error);

            if (error.response?.status === 409) {
                setApplicationMessage(
                    "You have already applied for this job."
                );
            } else if (error.response?.data) {
                setApplicationMessage(error.response.data);
            } else {
                setApplicationMessage("Unable to submit application.");
            }
        } finally {
            setApplying(false);
        }
    };

    const formatAIInsight = text => {
        const sections = text
            .split(/\n(?=[A-Z ]+:)/)
            .filter(section => section.trim());

        return sections.map((section, index) => {
            const parts = section.split(":");
            const title = parts.shift();
            const content = parts.join(":").trim();

            return (
                <div className="ai-insight-box" key={index}>
                    <h3>{title}</h3>
                    <p>{content}</p>
                </div>
            );
        });
    };

    if (loading) {
        return (
            <div className="job-details-page">
                <h2>Loading job details...</h2>
            </div>
        );
    }

    if (error) {
        return (
            <div className="job-details-page">
                <h2>{error}</h2>
            </div>
        );
    }

    if (!job) {
        return (
            <div className="job-details-page">
                <h2>Job not found.</h2>
            </div>
        );
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
                        {job.company?.charAt(0).toUpperCase()}
                    </div>

                    <div>
                        <h1>{job.title}</h1>
                        <p className="details-company">{job.company}</p>
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
                                ₹
                                {job.salary?.toLocaleString("en-IN")}
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

                {/* JOB ELIGIBILITY CHECKER */}
                <section className="eligibility-section">
                    <div className="eligibility-header">
                        <div>
                            <span className="eligibility-label">🎯 CAREERCONNECT ELIGIBILITY CHECK</span>
                            <h2>Your Job Eligibility</h2>
                            <p>See how your academic profile and skills match this job.</p>
                        </div>
                        {!eligibilityLoading && eligibility && (
                            <span className={`eligibility-status ${eligibility.eligible ? "eligible" : "not-eligible"}`}>
                                {eligibility.eligible ? "✓ Eligible" : "✕ Not Eligible"}
                            </span>
                        )}
                    </div>

                    {eligibilityLoading && <div className="eligibility-loading">Analyzing your profile...</div>}
                    {!eligibilityLoading && eligibilityError && (
                        <div className="eligibility-error">{eligibilityError}</div>
                    )}
                    {!eligibilityLoading && eligibility && (
                        <>
                            <div className="eligibility-message">{eligibility.message}</div>
                            <div className="eligibility-requirements">
                                <div className="eligibility-requirement-card">
                                    <div>
                                        <span>📚 CGPA Requirement</span>
                                        <strong>{eligibility.studentCgpa} / 10</strong>
                                        <p>Minimum required: {eligibility.requiredCgpa}</p>
                                    </div>
                                    <span className={`requirement-result ${eligibility.cgpaEligible ? "passed" : "failed"}`}>
                                        {eligibility.cgpaEligible ? "Passed" : "Not Met"}
                                    </span>
                                </div>
                                <div className="eligibility-requirement-card">
                                    <div>
                                        <span>🎓 Branch Requirement</span>
                                        <strong>{eligibility.studentBranch}</strong>
                                        <p>Accepted: {eligibility.eligibleBranches?.length ? eligibility.eligibleBranches.join(", ") : "All branches"}</p>
                                    </div>
                                    <span className={`requirement-result ${eligibility.branchEligible ? "passed" : "failed"}`}>
                                        {eligibility.branchEligible ? "Passed" : "Not Met"}
                                    </span>
                                </div>
                            </div>
                            <div className="eligibility-skills">
                                <div className="eligibility-skill-heading">
                                    <h3>💡 Skill Match Analysis</h3>
                                    <strong>{eligibility.skillMatchPercentage}%</strong>
                                </div>
                                <div className="eligibility-progress">
                                    <div className="eligibility-progress-fill" style={{ width: `${Math.min(100, Math.max(0, eligibility.skillMatchPercentage || 0))}%` }} />
                                </div>
                                <div className="eligibility-skill-summary">
                                    <span>{eligibility.matchedSkills?.length || 0} Matched</span>
                                    <span>{eligibility.missingSkills?.length || 0} Missing</span>
                                </div>
                                {eligibility.matchedSkills?.length > 0 && (
                                    <div className="eligibility-skill-group">
                                        <h4>✓ Matched Skills</h4>
                                        <div className="eligibility-skill-list">
                                            {eligibility.matchedSkills.map((skill, index) => <span className="eligibility-skill matched" key={index}>✓ {skill}</span>)}
                                        </div>
                                    </div>
                                )}
                                {eligibility.missingSkills?.length > 0 && (
                                    <div className="eligibility-skill-group">
                                        <h4>⚠ Skills to Improve</h4>
                                        <div className="eligibility-skill-list">
                                            {eligibility.missingSkills.map((skill, index) => <span className="eligibility-skill missing" key={index}>+ {skill}</span>)}
                                        </div>
                                    </div>
                                )}
                            </div>
                        </>
                    )}
                </section>

                {/* PERSONALIZED LEARNING ROADMAP */}
                <section className="roadmap-section">
                    <div className="roadmap-header">
                        <div>
                            <span className="roadmap-label">🧭 PERSONALIZED CAREER DEVELOPMENT</span>
                            <h2>Your Learning Roadmap</h2>
                            <p>Practical steps to build the skills this job requires.</p>
                        </div>
                        {!roadmapLoading && roadmap && (
                            <span className="roadmap-duration">
                                {roadmap.estimatedDurationWeeks} {roadmap.estimatedDurationWeeks === 1 ? "week" : "weeks"}
                            </span>
                        )}
                    </div>

                    {roadmapLoading && <div className="roadmap-empty">Preparing your personalized learning roadmap...</div>}
                    {!roadmapLoading && roadmapError && <div className="roadmap-error">{roadmapError}</div>}
                    {!roadmapLoading && roadmap && (
                        <>
                            <div className="roadmap-match-row">
                                <span>Current skill match</span>
                                <strong>{roadmap.currentSkillMatch}%</strong>
                            </div>
                            <div className="roadmap-progress-track">
                                <div className="roadmap-progress-fill" style={{ width: `${Math.min(100, Math.max(0, roadmap.currentSkillMatch || 0))}%` }} />
                            </div>
                            {roadmap.matchedSkills?.length > 0 && (
                                <div className="roadmap-skill-group">
                                    <h3>✓ Skills you already have</h3>
                                    <div className="roadmap-skill-list">
                                        {roadmap.matchedSkills.map((skill, index) => <span className="roadmap-skill matched" key={index}>{skill}</span>)}
                                    </div>
                                </div>
                            )}
                            {roadmap.missingSkills?.length === 0 ? (
                                <div className="roadmap-complete">
                                    <strong>🎉 You're all caught up!</strong>
                                    <p>Your profile currently includes all the skills listed for this job. Keep practising and building projects to strengthen your experience.</p>
                                </div>
                            ) : (
                                <>
                                    <div className="roadmap-skill-group">
                                        <h3>Skills to develop</h3>
                                        <div className="roadmap-skill-list">
                                            {roadmap.missingSkills.map((skill, index) => <span className="roadmap-skill missing" key={index}>{skill}</span>)}
                                        </div>
                                    </div>
                                    <div className="roadmap-plans">
                                        {roadmap.learningPlan?.map((plan, index) => (
                                            <article className="roadmap-plan-card" key={`${plan.skill}-${index}`}>
                                                <div className="roadmap-plan-top">
                                                    <div>
                                                        <span className="roadmap-step-label">SKILL {index + 1}</span>
                                                        <h3>{plan.skill}</h3>
                                                    </div>
                                                    <div className="roadmap-plan-meta">
                                                        <span className="roadmap-priority">{plan.priority} priority</span>
                                                        <span>{plan.estimatedWeeks} weeks</span>
                                                    </div>
                                                </div>
                                                <ol className="roadmap-steps">
                                                    {plan.learningSteps?.map((step, stepIndex) => <li key={stepIndex}>{step}</li>)}
                                                </ol>
                                            </article>
                                        ))}
                                    </div>
                                    <p className="roadmap-disclaimer">Duration estimates are planning guides. Actual learning time depends on your prior experience and practice.</p>
                                </>
                            )}
                        </>
                    )}
                </section>

                {!aiLoading && aiRecommendation && (
                    <section className="job-ai-section">
                        <div className="job-ai-header">
                            <div>
                                <span className="job-ai-label">
                                    🤖 AI CAREER INTELLIGENCE
                                </span>
                                <h2>Your AI Match</h2>
                            </div>

                            <div className="job-ai-percentage">
                                ⭐ {aiRecommendation.matchPercentage}%
                            </div>
                        </div>

                        <div className="job-ai-role">
                            <span>🎯 Career Direction</span>
                            <strong>{aiRecommendation.matchedRole}</strong>
                        </div>

                        {aiRecommendation.matchedSkills?.length > 0 && (
                            <div className="job-ai-skills">
                                <h3>✓ Matched Skills</h3>

                                <div className="job-ai-skill-list">
                                    {aiRecommendation.matchedSkills.map(
                                        (skill, index) => (
                                            <span
                                                className="job-ai-skill matched"
                                                key={index}
                                            >
                                                ✓ {skill}
                                            </span>
                                        )
                                    )}
                                </div>
                            </div>
                        )}

                        {aiRecommendation.missingSkills?.length > 0 && (
                            <div className="job-ai-skills">
                                <h3>⚠ Skills to Improve</h3>

                                <div className="job-ai-skill-list">
                                    {aiRecommendation.missingSkills.map(
                                        (skill, index) => (
                                            <span
                                                className="job-ai-skill missing"
                                                key={index}
                                            >
                                                {skill}
                                            </span>
                                        )
                                    )}
                                </div>
                            </div>
                        )}

                        {aiRecommendation.explanation && (
                            <div className="job-ai-explanation">
                                <strong>💡 Why this match?</strong>
                                <p>{aiRecommendation.explanation}</p>
                            </div>
                        )}
                    </section>
                )}

                {jobAIInsightLoading && (
                    <section className="job-ai-section">
                        <div className="job-ai-header">
                            <div>
                                <span className="job-ai-label">
                                    ✨ OLLAMA AI INSIGHT
                                </span>
                                <h2>AI Analysis of This Job</h2>
                            </div>
                        </div>

                        <div className="job-ai-explanation">
                            <p>Generating personalized AI insight...</p>
                        </div>
                    </section>
                )}

                {!jobAIInsightLoading && jobAIInsight && (
                    <section className="job-ai-section">
                        <div className="job-ai-header">
                            <div>
                                <span className="job-ai-label">
                                    ✨ OLLAMA AI INSIGHT
                                </span>
                                <h2>AI Analysis of This Job</h2>
                                <p>
                                    Personalized guidance based on your
                                    profile and this specific job.
                                </p>
                            </div>
                        </div>

                        <div className="ai-insight-container">
                            {formatAIInsight(jobAIInsight)}
                        </div>
                    </section>
                )}

                {!jobAIInsightLoading &&
                    !jobAIInsight &&
                    jobAIInsightError && (
                        <section className="job-ai-no-match">
                            <h2>✨ Ollama AI Insight</h2>
                            <p>{jobAIInsightError}</p>
                        </section>
                    )}

                {!aiLoading && !aiRecommendation && (
                    <section className="job-ai-no-match">
                        <h2>🤖 AI Career Intelligence</h2>
                        <p>
                            This job does not currently match your profile
                            strongly enough to appear as an AI recommendation.
                        </p>
                    </section>
                )}

                <section className="job-description">
                    <h2>About This Job</h2>

                    <p>
                        Join {job.company} as a {job.title}.
                        This opportunity is based in {job.location}.
                    </p>

                    <p>
                        Explore this opportunity and take the next step in
                        your career journey with CareerConnect.
                    </p>
                </section>

                <button
                    className="apply-button"
                    onClick={handleApply}
                    disabled={applying || eligibilityLoading || !eligibility || !eligibility.eligible}
                >
                    {applying ? "Applying..." : eligibility && !eligibility.eligible ? "Not Eligible to Apply" : "Apply Now"}
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
