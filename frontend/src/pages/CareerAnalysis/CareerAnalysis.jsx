
import { useEffect, useState } from "react";
import axios from "axios";
import "./CareerAnalysis.css";

function CareerAnalysis() {
    const [analysis, setAnalysis] = useState(null);
    const [careerAdvice, setCareerAdvice] = useState("");
    const [resumeAI, setResumeAI] = useState("");
    const [resumeQuality, setResumeQuality] = useState(null);

    const [loading, setLoading] = useState(true);
    const [adviceLoading, setAdviceLoading] = useState(true);
    const [resumeLoading, setResumeLoading] = useState(true);
    const [qualityLoading, setQualityLoading] = useState(true);
    const [error, setError] = useState("");
    const [qualityError, setQualityError] = useState("");

    useEffect(() => {
        const loadAnalysis = async () => {
            const storedStudent = localStorage.getItem("student");

            if (!storedStudent) {
                window.location.href = "/login";
                return;
            }

            const student = JSON.parse(storedStudent);
            const studentId = student.id;

            try {
                const response = await axios.get(
                    `http://localhost:8080/career-analysis/${studentId}`
                );
                setAnalysis(response.data);
            } catch (err) {
                console.error("Career analysis error:", err);
                setError(
                    err.response?.data ||
                    "Unable to load career analysis."
                );
            } finally {
                setLoading(false);
            }

            try {
                const response = await axios.get(
                    `http://localhost:8080/career-recommendation/${studentId}`
                );
                setCareerAdvice(response.data);
            } catch (err) {
                console.error("Career advice error:", err);
            } finally {
                setAdviceLoading(false);
            }

            try {
                const response = await axios.get(
                    `http://localhost:8080/ai/resume-analysis/${studentId}`
                );
                setResumeAI(response.data);
            } catch (err) {
                console.error("Resume AI error:", err);
                setResumeAI(
                    "Resume AI analysis is unavailable. Please upload a PDF resume."
                );
            } finally {
                setResumeLoading(false);
            }

            try {
                const response = await axios.get(
                    `http://localhost:8080/ai/resume-quality/${studentId}`
                );
                setResumeQuality(response.data);
            } catch (err) {
                console.error("Resume quality error:", err);
                setQualityError(
                    typeof err.response?.data === "string"
                        ? err.response.data
                        : "Resume quality analysis is unavailable. Please upload a PDF resume."
                );
            } finally {
                setQualityLoading(false);
            }
        };

        loadAnalysis();
    }, []);

    const roles = analysis?.recommendedRoles
        ? analysis.recommendedRoles.split(",").map(role => role.trim())
        : [];

    const skills = analysis?.recommendedSkills
        ? analysis.recommendedSkills.split(",").map(skill => skill.trim())
        : [];

    const skillGaps = analysis?.skillGaps
        ? analysis.skillGaps.split(",").map(gap => gap.trim())
        : [];

    const getPercentage = (role) => {
        const match = role.match(/(\d+)%/);
        return match ? parseInt(match[1], 10) : 0;
    };

    const getRoleName = (role) => {
        return role
            .replace(/\s*\(\d+%\s*Match\)/i, "")
            .trim();
    };

    const parseSections = (content, headings) => {
        if (!content) return [];

        const sections = [];
        let currentTitle = "";
        let currentContent = [];

        const flushSection = () => {
            if (currentTitle && currentContent.join("\n").trim()) {
                sections.push({
                    title: currentTitle,
                    content: currentContent.join("\n").trim()
                });
            }
            currentContent = [];
        };

        content.replace(/\r/g, "").split("\n").forEach(line => {
            const cleanLine = line
                .replace(/^#+\s*/, "")
                .replace(/\*\*/g, "")
                .replace(/^[-*]\s*/, "")
                .trim();

            const heading = headings.find(item =>
                cleanLine.toUpperCase().startsWith(item)
            );

            if (heading) {
                flushSection();
                currentTitle = heading;

                const remaining = cleanLine
                    .substring(heading.length)
                    .replace(/^:\s*/, "")
                    .trim();

                if (remaining) currentContent.push(remaining);
            } else if (currentTitle && cleanLine) {
                currentContent.push(cleanLine);
            }
        });

        flushSection();
        return sections;
    };

    const resumeSections = parseSections(resumeAI, [
        "RESUME SUMMARY",
        "TECHNICAL STRENGTHS",
        "CAREER DIRECTION",
        "AREAS TO IMPROVE",
        "RECOMMENDED NEXT STEPS"
    ]);

    const qualitySections = parseSections(
        resumeQuality?.aiSuggestions,
        [
            "STRENGTHS",
            "AREAS TO IMPROVE",
            "MISSING KEYWORDS",
            "ACTIONABLE SUGGESTIONS"
        ]
    );

    const scoreItems = [
        {
            label: "ATS Compatibility",
            value: resumeQuality?.atsCompatibility,
            description: "Resume structure and common ATS-readable information"
        },
        {
            label: "Technical Skills",
            value: resumeQuality?.technicalSkillsScore,
            description: "Technical keywords identified in the resume"
        },
        {
            label: "Project Quality",
            value: resumeQuality?.projectQualityScore,
            description: "Project descriptions, action verbs and measurable outcomes"
        },
        {
            label: "Resume Completeness",
            value: resumeQuality?.resumeCompletenessScore,
            description: "Presence of important resume sections"
        }
    ];

    const getScoreColor = (score) => {
        if (score >= 75) return "#22c55e";
        if (score >= 50) return "#f59e0b";
        return "#ef4444";
    };

    if (loading) {
        return (
            <div className="career-analysis-page">
                <h2>Loading career analysis...</h2>
            </div>
        );
    }

    if (error) {
        return (
            <div className="career-analysis-page">
                <h2>{error}</h2>
            </div>
        );
    }

    if (!analysis) {
        return (
            <div className="career-analysis-page">
                <h2>Career analysis not found.</h2>
            </div>
        );
    }

    return (
        <div className="career-analysis-page">

            {/* PAGE HEADER */}
            <div className="career-analysis-header">
                <h1>AI Career Analysis</h1>
                <p>
                    Discover suitable career roles, recommended
                    skills, resume quality and areas for improvement.
                </p>
            </div>

            {/* AI RESUME INTELLIGENCE */}
            <div className="analysis-card">
                <div style={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                    gap: "15px",
                    flexWrap: "wrap"
                }}>
                    <div>
                        <h2>📄 AI Resume Intelligence</h2>
                        <p>
                            AI analysis of your uploaded resume,
                            technical strengths and career direction.
                        </p>
                    </div>

                    <span style={{
                        padding: "7px 12px",
                        borderRadius: "20px",
                        background: "rgba(99,102,241,0.15)",
                        fontSize: "13px"
                    }}>
                        Powered by Local AI
                    </span>
                </div>

                {resumeLoading ? (
                    <p>🤖 Analyzing your resume...</p>
                ) : resumeSections.length > 0 ? (
                    <div style={{ marginTop: "20px" }}>
                        {resumeSections.map((section, index) => (
                            <div
                                key={index}
                                style={{
                                    background: "rgba(255,255,255,0.04)",
                                    border: "1px solid rgba(255,255,255,0.08)",
                                    borderRadius: "14px",
                                    padding: "20px",
                                    marginBottom: "16px"
                                }}
                            >
                                <h3>
                                    {section.title === "RESUME SUMMARY"
                                        ? "📝 Resume Summary"
                                        : section.title === "TECHNICAL STRENGTHS"
                                        ? "💪 Technical Strengths"
                                        : section.title === "CAREER DIRECTION"
                                        ? "🎯 Career Direction"
                                        : section.title === "AREAS TO IMPROVE"
                                        ? "📚 Areas to Improve"
                                        : "🚀 Recommended Next Steps"}
                                </h3>
                                <p style={{
                                    margin: 0,
                                    lineHeight: "1.7",
                                    whiteSpace: "pre-line"
                                }}>
                                    {section.content}
                                </p>
                            </div>
                        ))}
                    </div>
                ) : (
                    <p>
                        {resumeAI || "Resume analysis is not available."}
                    </p>
                )}
            </div>

            {/* NEW: RESUME QUALITY AND ATS ANALYSIS */}
            <div className="analysis-card">
                <div style={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                    flexWrap: "wrap",
                    gap: "12px"
                }}>
                    <div>
                        <h2>📊 AI Resume Quality & ATS Analysis</h2>
                        <p>
                            Evaluate your resume and discover
                            opportunities to improve it.
                        </p>
                    </div>

                    <span style={{
                        padding: "7px 12px",
                        borderRadius: "20px",
                        background: "rgba(34,197,94,0.12)",
                        fontSize: "13px"
                    }}>
                        CareerConnect Estimate
                    </span>
                </div>

                {qualityLoading ? (
                    <p>🤖 Evaluating resume quality...</p>
                ) : resumeQuality ? (
                    <>
                        {/* OVERALL SCORE */}
                        <div style={{
                            display: "flex",
                            alignItems: "center",
                            gap: "24px",
                            flexWrap: "wrap",
                            padding: "24px",
                            marginTop: "20px",
                            borderRadius: "14px",
                            background: "rgba(99,102,241,0.08)"
                        }}>
                            <div style={{
                                width: "125px",
                                height: "125px",
                                borderRadius: "50%",
                                border: `8px solid ${getScoreColor(resumeQuality.overallScore)}`,
                                display: "flex",
                                flexDirection: "column",
                                justifyContent: "center",
                                alignItems: "center",
                                flexShrink: 0
                            }}>
                                <strong style={{ fontSize: "32px" }}>
                                    {resumeQuality.overallScore}%
                                </strong>
                                <span style={{ fontSize: "11px" }}>
                                    OVERALL
                                </span>
                            </div>

                            <div>
                                <h3>Resume Quality Score</h3>
                                <p>
                                    Your overall score is based on
                                    ATS compatibility, technical skills,
                                    project quality and resume completeness.
                                </p>
                            </div>
                        </div>

                        {/* INDIVIDUAL SCORES */}
                        <div style={{
                            display: "grid",
                            gridTemplateColumns: "repeat(auto-fit, minmax(220px, 1fr))",
                            gap: "16px",
                            marginTop: "20px"
                        }}>
                            {scoreItems.map((item, index) => (
                                <div
                                    key={index}
                                    style={{
                                        padding: "18px",
                                        borderRadius: "12px",
                                        background: "rgba(255,255,255,0.04)",
                                        border: "1px solid rgba(255,255,255,0.08)"
                                    }}
                                >
                                    <div style={{
                                        display: "flex",
                                        justifyContent: "space-between",
                                        gap: "10px"
                                    }}>
                                        <strong>{item.label}</strong>
                                        <strong style={{
                                            color: getScoreColor(item.value)
                                        }}>
                                            {item.value}%
                                        </strong>
                                    </div>

                                    <div style={{
                                        height: "9px",
                                        borderRadius: "10px",
                                        background: "rgba(255,255,255,0.12)",
                                        overflow: "hidden",
                                        margin: "14px 0"
                                    }}>
                                        <div style={{
                                            width: `${item.value}%`,
                                            height: "100%",
                                            borderRadius: "10px",
                                            background: getScoreColor(item.value),
                                            transition: "width 0.5s ease"
                                        }} />
                                    </div>

                                    <p style={{
                                        fontSize: "13px",
                                        opacity: 0.75,
                                        margin: 0
                                    }}>
                                        {item.description}
                                    </p>
                                </div>
                            ))}
                        </div>

                        {/* AI SUGGESTIONS */}
                        <div style={{ marginTop: "28px" }}>
                            <h3>🤖 AI Improvement Recommendations</h3>

                            {qualitySections.length > 0 ? (
                                qualitySections.map((section, index) => (
                                    <div
                                        key={index}
                                        style={{
                                            padding: "18px",
                                            marginBottom: "14px",
                                            borderRadius: "12px",
                                            background: "rgba(255,255,255,0.04)",
                                            border: "1px solid rgba(255,255,255,0.08)"
                                        }}
                                    >
                                        <h3 style={{ marginTop: 0 }}>
                                            {section.title === "STRENGTHS"
                                                ? "✓ Strengths"
                                                : section.title === "AREAS TO IMPROVE"
                                                ? "⚠ Areas to Improve"
                                                : section.title === "MISSING KEYWORDS"
                                                ? "🔍 Missing Keywords"
                                                : "🚀 Actionable Suggestions"}
                                        </h3>

                                        <p style={{
                                            whiteSpace: "pre-line",
                                            lineHeight: "1.7",
                                            marginBottom: 0
                                        }}>
                                            {section.content}
                                        </p>
                                    </div>
                                ))
                            ) : (
                                <p>
                                    {resumeQuality.aiSuggestions ||
                                        "No AI suggestions available."}
                                </p>
                            )}
                        </div>

                        <p style={{
                            fontSize: "12px",
                            opacity: 0.65,
                            marginTop: "18px"
                        }}>
                            Note: These are heuristic CareerConnect
                            estimates, not official ATS scores or
                            guarantees of selection.
                        </p>
                    </>
                ) : (
                    <p>
                        {qualityError || "Resume quality analysis is unavailable."}
                    </p>
                )}
            </div>

            {/* AI CAREER INSIGHTS */}
            <div className="analysis-card ai-career-card">
                <h2>🤖 AI Career Insights</h2>

                {adviceLoading ? (
                    <p>Generating personalized career insights...</p>
                ) : (
                    <div className="ai-advice">
                        {careerAdvice.split("\n").map((line, index) => (
                            <p key={index}>{line}</p>
                        ))}
                    </div>
                )}
            </div>

            {/* CAREER READINESS */}
            <div className="analysis-card career-score-card">
                <h2>📊 Career Readiness</h2>

                <div className="career-score">
                    <div className="score-number">
                        {careerAdvice.match(
                            /Career Readiness Score:\s*(\d+)%/
                        )?.[1] || 0}%
                    </div>

                    <div className="score-text">
                        <strong>Current Readiness</strong>
                        <p>
                            Based on your current skills and
                            identified skill gaps.
                        </p>
                    </div>
                </div>
            </div>

            {/* RECOMMENDED ROLES */}
            <div className="analysis-card">
                <h2>🎯 Recommended Roles</h2>

                <div className="role-list">
                    {roles.map((role, index) => {
                        const percentage = getPercentage(role);
                        const roleName = getRoleName(role);

                        return (
                            <div className="role-match" key={index}>
                                <div className="role-match-header">
                                    <span className="role-name">
                                        {roleName}
                                    </span>
                                    <span className="match-percentage">
                                        {percentage}% Match
                                    </span>
                                </div>

                                <div className="progress-bar">
                                    <div
                                        className="progress-fill"
                                        style={{ width: `${percentage}%` }}
                                    />
                                </div>
                            </div>
                        );
                    })}
                </div>
            </div>

            {/* RECOMMENDED SKILLS */}
            <div className="analysis-card">
                <h2>💡 Recommended Skills</h2>

                <div className="analysis-list">
                    {skills.map((skill, index) => (
                        <div className="analysis-item" key={index}>
                            ✓ {skill}
                        </div>
                    ))}
                </div>
            </div>

            {/* SKILL GAPS */}
            <div className="analysis-card">
                <h2>📚 Skill Gaps</h2>

                <div className="analysis-list">
                    {skillGaps.map((gap, index) => (
                        <div
                            className="analysis-item skill-gap"
                            key={index}
                        >
                            ⚠ {gap}
                        </div>
                    ))}
                </div>
            </div>

            {/* LEARNING ROADMAP */}
            <div className="analysis-card">
                <h2>🛣️ Learning Roadmap</h2>

                <div className="roadmap">
                    {skillGaps.slice(0, 4).map((gap, index) => (
                        <div className="roadmap-step" key={index}>
                            <div className="roadmap-number">
                                {index + 1}
                            </div>

                            <div>
                                <h3>Learn {gap}</h3>
                                <p>
                                    Improve this skill to increase
                                    your career readiness.
                                </p>
                            </div>
                        </div>
                    ))}

                    {skillGaps.length === 0 && (
                        <p>
                            Continue improving your existing skills
                            through projects and interview practice.
                        </p>
                    )}
                </div>
            </div>

        </div>
    );
}

export default CareerAnalysis;

