import { useEffect, useState } from "react";
import axios from "axios";
import "./CareerAnalysis.css";

function CareerAnalysis() {

    const [analysis, setAnalysis] = useState(null);
    const [careerAdvice, setCareerAdvice] = useState("");
    const [resumeAI, setResumeAI] = useState("");

    const [loading, setLoading] = useState(true);
    const [adviceLoading, setAdviceLoading] = useState(true);
    const [resumeLoading, setResumeLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {

        const loadAnalysis = async () => {

            try {

                const storedStudent =
                    localStorage.getItem("student");

                if (!storedStudent) {
                    window.location.href = "/login";
                    return;
                }

                const student =
                    JSON.parse(storedStudent);

                const studentId = student.id;

                const analysisResponse =
                    await axios.get(
                        `http://localhost:8080/career-analysis/${studentId}`
                    );

                setAnalysis(analysisResponse.data);

                try {

                    const adviceResponse =
                        await axios.get(
                            `http://localhost:8080/career-recommendation/${studentId}`
                        );

                    setCareerAdvice(adviceResponse.data);

                } catch (error) {

                    console.error(
                        "Career advice error:",
                        error
                    );

                } finally {

                    setAdviceLoading(false);

                }

                try {

                    const resumeResponse =
                        await axios.get(
                            `http://localhost:8080/ai/resume-analysis/${studentId}`
                        );

                    setResumeAI(resumeResponse.data);

                } catch (error) {

                    console.error(
                        "Resume AI analysis error:",
                        error
                    );

                    setResumeAI(
                        "Resume AI analysis is unavailable. Please make sure a PDF resume has been uploaded."
                    );

                } finally {

                    setResumeLoading(false);

                }

            } catch (error) {

                console.error(
                    "Error loading career analysis:",
                    error
                );

                setError(
                    error.response?.data ||
                    "Unable to load career analysis."
                );

                setAdviceLoading(false);
                setResumeLoading(false);

            } finally {

                setLoading(false);

            }
        };

        loadAnalysis();

    }, []);

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

    const roles = analysis.recommendedRoles
        ? analysis.recommendedRoles
            .split(",")
            .map(role => role.trim())
        : [];

    const skills = analysis.recommendedSkills
        ? analysis.recommendedSkills
            .split(",")
            .map(skill => skill.trim())
        : [];

    const skillGaps = analysis.skillGaps
        ? analysis.skillGaps
            .split(",")
            .map(gap => gap.trim())
        : [];

    const getPercentage = (role) => {

        const percentageMatch =
            role.match(/(\d+)%/);

        if (percentageMatch) {
            return parseInt(
                percentageMatch[1]
            );
        }

        return 0;
    };

    const getRoleName = (role) => {

        return role
            .replace(
                /\s*\(\d+%\s*Match\)/i,
                ""
            )
            .trim();
    };

    const parseResumeAI = () => {

        if (!resumeAI) {
            return [];
        }

        const headings = [
            "RESUME SUMMARY",
            "TECHNICAL STRENGTHS",
            "CAREER DIRECTION",
            "AREAS TO IMPROVE",
            "RECOMMENDED NEXT STEPS"
        ];

        const sections = [];

        let currentTitle = "";
        let currentContent = [];

        const lines =
            resumeAI
                .replace(/\r/g, "")
                .split("\n");

        const flushSection = () => {

            if (
                currentTitle &&
                currentContent.join("\n").trim()
            ) {

                sections.push({
                    title: currentTitle,
                    content:
                        currentContent
                            .join("\n")
                            .trim()
                });

            }

            currentContent = [];

        };

        lines.forEach(line => {

            const cleanLine =
                line
                    .replace(/^#+\s*/, "")
                    .replace(/\*\*/g, "")
                    .trim();

            const matchedHeading =
                headings.find(
                    heading =>
                        cleanLine
                            .toUpperCase()
                            .startsWith(heading)
                );

            if (matchedHeading) {

                flushSection();
                currentTitle =
                    matchedHeading;

                const remaining =
                    cleanLine
                        .substring(
                            matchedHeading.length
                        )
                        .replace(/^:\s*/, "")
                        .trim();

                if (remaining) {
                    currentContent.push(
                        remaining
                    );
                }

            } else if (currentTitle) {

                currentContent.push(cleanLine);

            }

        });

        flushSection();

        return sections;
    };

    const resumeSections =
        parseResumeAI();

    const resumeSectionStyle = {
        background: "rgba(255,255,255,0.04)",
        border: "1px solid rgba(255,255,255,0.08)",
        borderRadius: "14px",
        padding: "20px",
        marginBottom: "16px"
    };

    const resumeTitleStyle = {
        marginTop: 0,
        marginBottom: "12px",
        fontSize: "18px"
    };

    const resumeTextStyle = {
        margin: 0,
        lineHeight: "1.7",
        whiteSpace: "pre-line"
    };

    return (

        <div className="career-analysis-page">

            {/* Header */}

            <div className="career-analysis-header">

                <h1>AI Career Analysis</h1>

                <p>
                    Discover suitable career roles,
                    recommended skills, and areas
                    for improvement.
                </p>

            </div>


            {/* ================================================= */}
            {/* RESUME INTELLIGENCE */}
            {/* ================================================= */}

            <div className="analysis-card">

                <div
                    style={{
                        display: "flex",
                        justifyContent: "space-between",
                        alignItems: "center",
                        gap: "15px",
                        flexWrap: "wrap"
                    }}
                >

                    <div>

                        <h2 style={{ marginBottom: "6px" }}>
                            📄 AI Resume Intelligence
                        </h2>

                        <p style={{ marginTop: 0 }}>
                            AI analysis of your uploaded resume,
                            skills, projects, and career direction.
                        </p>

                    </div>

                    <span
                        style={{
                            padding: "7px 12px",
                            borderRadius: "20px",
                            background: "rgba(99,102,241,0.15)",
                            fontSize: "13px"
                        }}
                    >
                        Powered by Local AI
                    </span>

                </div>


                {resumeLoading ? (

                    <div
                        style={{
                            padding: "30px 0",
                            textAlign: "center"
                        }}
                    >

                        <p>
                            🤖 Analyzing your resume...
                        </p>

                        <p style={{ opacity: 0.7 }}>
                            This may take a few seconds.
                        </p>

                    </div>

                ) : resumeSections.length > 0 ? (

                    <div style={{ marginTop: "20px" }}>

                        {resumeSections.map(
                            (section, index) => (

                                <div
                                    key={index}
                                    style={
                                        resumeSectionStyle
                                    }
                                >

                                    <h3
                                        style={
                                            resumeTitleStyle
                                        }
                                    >
                                        {section.title ===
                                        "RESUME SUMMARY"
                                            ? "📝 Resume Summary"
                                            : section.title ===
                                              "TECHNICAL STRENGTHS"
                                            ? "💪 Technical Strengths"
                                            : section.title ===
                                              "CAREER DIRECTION"
                                            ? "🎯 Career Direction"
                                            : section.title ===
                                              "AREAS TO IMPROVE"
                                            ? "📚 Areas to Improve"
                                            : "🚀 Recommended Next Steps"}
                                    </h3>

                                    <p
                                        style={
                                            resumeTextStyle
                                        }
                                    >
                                        {section.content}
                                    </p>

                                </div>

                            )
                        )}

                    </div>

                ) : (

                    <div
                        style={{
                            marginTop: "20px",
                            padding: "20px",
                            borderRadius: "12px"
                        }}
                    >

                        <p>
                            {resumeAI ||
                                "Resume analysis is not available."}
                        </p>

                    </div>

                )}

            </div>


            {/* ================================================= */}
            {/* AI CAREER INSIGHTS */}
            {/* ================================================= */}

            <div className="analysis-card ai-career-card">

                <h2>🤖 AI Career Insights</h2>

                {adviceLoading ? (

                    <p>
                        Generating personalized
                        career insights...
                    </p>

                ) : (

                    <div className="ai-advice">

                        {careerAdvice
                            .split("\n")
                            .map((line, index) => (

                                <p key={index}>
                                    {line}
                                </p>

                            ))}

                    </div>

                )}

            </div>


            {/* ================================================= */}
            {/* CAREER READINESS */}
            {/* ================================================= */}

            <div className="analysis-card career-score-card">

                <h2>📊 Career Readiness</h2>

                <div className="career-score">

                    <div className="score-number">

                        {
                            careerAdvice.match(
                                /Career Readiness Score:\s*(\d+)%/
                            )?.[1] || 0
                        }%

                    </div>

                    <div className="score-text">

                        <strong>
                            Current Readiness
                        </strong>

                        <p>
                            Based on your current
                            skills and identified
                            skill gaps.
                        </p>

                    </div>

                </div>

            </div>


            {/* ================================================= */}
            {/* RECOMMENDED ROLES */}
            {/* ================================================= */}

            <div className="analysis-card">

                <h2>🎯 Recommended Roles</h2>

                <div className="role-list">

                    {roles.map((role, index) => {

                        const percentage =
                            getPercentage(role);

                        const roleName =
                            getRoleName(role);

                        return (

                            <div
                                className="role-match"
                                key={index}
                            >

                                <div
                                    className="role-match-header"
                                >

                                    <span className="role-name">
                                        {roleName}
                                    </span>

                                    <span
                                        className="match-percentage"
                                    >
                                        {percentage}% Match
                                    </span>

                                </div>

                                <div className="progress-bar">

                                    <div
                                        className="progress-fill"
                                        style={{
                                            width:
                                                `${percentage}%`
                                        }}
                                    >
                                    </div>

                                </div>

                            </div>

                        );

                    })}

                </div>

            </div>


            {/* ================================================= */}
            {/* RECOMMENDED SKILLS */}
            {/* ================================================= */}

            <div className="analysis-card">

                <h2>💡 Recommended Skills</h2>

                <div className="analysis-list">

                    {skills.map((skill, index) => (

                        <div
                            className="analysis-item"
                            key={index}
                        >
                            ✓ {skill}
                        </div>

                    ))}

                </div>

            </div>


            {/* ================================================= */}
            {/* SKILL GAPS */}
            {/* ================================================= */}

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


            {/* ================================================= */}
            {/* LEARNING ROADMAP */}
            {/* ================================================= */}

            <div className="analysis-card">

                <h2>🛣️ Learning Roadmap</h2>

                <div className="roadmap">

                    {skillGaps
                        .slice(0, 4)
                        .map((gap, index) => (

                            <div
                                className="roadmap-step"
                                key={index}
                            >

                                <div className="roadmap-number">
                                    {index + 1}
                                </div>

                                <div>

                                    <h3>
                                        Learn {gap}
                                    </h3>

                                    <p>
                                        Improve this skill
                                        to increase your
                                        career readiness.
                                    </p>

                                </div>

                            </div>

                        ))}

                    {skillGaps.length === 0 && (

                        <p>
                            Continue improving your
                            existing skills through
                            projects and interview
                            practice.
                        </p>

                    )}

                </div>

            </div>

        </div>
    );
}

export default CareerAnalysis;
