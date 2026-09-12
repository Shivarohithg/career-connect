import { useEffect, useState } from "react";
import axios from "axios";
import "./CareerAnalysis.css";

function CareerAnalysis() {

    const [analysis, setAnalysis] = useState(null);
    const [careerAdvice, setCareerAdvice] = useState("");

    const [loading, setLoading] = useState(true);
    const [adviceLoading, setAdviceLoading] = useState(true);

    const [error, setError] = useState("");

    useEffect(() => {

        const loadAnalysis = async () => {

            try {

                // Temporary student ID for testing
                const studentId = 1;

                // Get existing career analysis
                const analysisResponse = await axios.get(
                    `http://localhost:8080/career-analysis/${studentId}`
                );

                setAnalysis(analysisResponse.data);

                // Get personalized career recommendation
                const adviceResponse = await axios.get(
                    `http://localhost:8080/career-recommendation/${studentId}`
                );

                setCareerAdvice(adviceResponse.data);

            } catch (error) {

                console.error(
                    "Error loading career analysis:",
                    error
                );

                console.error(
                    "Response:",
                    error.response?.data
                );

                setError(
                    error.response?.data ||
                    "Unable to load career analysis."
                );

            } finally {

                setLoading(false);
                setAdviceLoading(false);

            }
        };

        loadAnalysis();

    }, []);


    // Loading
    if (loading) {

        return (
            <div className="career-analysis-page">

                <h2>
                    Loading career analysis...
                </h2>

            </div>
        );

    }


    // Error
    if (error) {

        return (
            <div className="career-analysis-page">

                <div className="analysis-card">

                    <h2>
                        Unable to Load Career Analysis
                    </h2>

                    <p>
                        {error}
                    </p>

                </div>

            </div>
        );

    }


    // No analysis
    if (!analysis) {

        return (
            <div className="career-analysis-page">

                <div className="analysis-card">

                    <h2>
                        Career analysis not found.
                    </h2>

                </div>

            </div>
        );

    }


    // Convert backend strings into arrays
    const roles = analysis.recommendedRoles
        ? analysis.recommendedRoles
            .split(",")
            .map(role => role.trim())
            .filter(role => role.length > 0)
        : [];


    const skills = analysis.recommendedSkills
        ? analysis.recommendedSkills
            .split(",")
            .map(skill => skill.trim())
            .filter(skill => skill.length > 0)
        : [];


    const skillGaps = analysis.skillGaps
        ? analysis.skillGaps
            .split(",")
            .map(gap => gap.trim())
            .filter(gap => gap.length > 0)
        : [];


    // Extract match percentage
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


    // Remove percentage from role name
    const getRoleName = (role) => {

        return role
            .replace(
                /\s*\(\d+%\s*Match\)/i,
                ""
            )
            .trim();

    };


    // Get highest role percentage
    const bestRolePercentage =
        roles.length > 0
            ? Math.max(
                ...roles.map(role =>
                    getPercentage(role)
                )
            )
            : 0;


    // Display career advice line by line
    const adviceLines = careerAdvice
        ? careerAdvice
            .split("\n")
            .filter(line => line.trim() !== "")
        : [];


    return (

        <div className="career-analysis-page">


            {/* Header */}

            <div className="career-analysis-header">

                <h1>
                    AI Career Analysis
                </h1>

                <p>
                    Discover suitable career roles,
                    recommended skills, and areas
                    for improvement.
                </p>

            </div>


            {/* AI Career Insights */}

            <div className="analysis-card ai-career-card">

                <h2>
                    🤖 AI Career Insights
                </h2>


                {adviceLoading ? (

                    <p>
                        Generating personalized
                        career insights...
                    </p>

                ) : careerAdvice ? (

                    <div className="ai-advice">

                        {adviceLines.map(
                            (line, index) => (

                                <p key={index}>
                                    {line}
                                </p>

                            )
                        )}

                    </div>

                ) : (

                    <p>
                        No personalized career
                        advice available.
                    </p>

                )}

            </div>


            {/* Career Score */}

            <div className="analysis-card career-score-card">

                <h2>
                    📊 Career Readiness
                </h2>

                <div className="career-score">

                    <div className="score-number">
                        {bestRolePercentage}%
                    </div>

                    <div className="score-text">

                        <strong>
                            Career Match Score
                        </strong>

                        <p>
                            Based on your current
                            skills and available
                            career roles.
                        </p>

                    </div>

                </div>


                <div className="progress-bar">

                    <div
                        className="progress-fill"
                        style={{
                            width: `${bestRolePercentage}%`
                        }}
                    />

                </div>

            </div>


            {/* Recommended Roles */}

            <div className="analysis-card">

                <h2>
                    🎯 Recommended Roles
                </h2>

                <div className="role-list">

                    {roles.length > 0 ? (

                        roles.map(
                            (role, index) => {

                                const percentage =
                                    getPercentage(role);

                                const roleName =
                                    getRoleName(role);

                                return (

                                    <div
                                        className="role-match"
                                        key={index}
                                    >

                                        <div className="role-match-header">

                                            <span className="role-name">

                                                {roleName}

                                            </span>

                                            <span className="match-percentage">

                                                {percentage}%
                                                Match

                                            </span>

                                        </div>


                                        <div className="progress-bar">

                                            <div
                                                className="progress-fill"
                                                style={{
                                                    width: `${percentage}%`
                                                }}
                                            />

                                        </div>

                                    </div>

                                );

                            }
                        )

                    ) : (

                        <p>
                            No recommended roles
                            available.
                        </p>

                    )}

                </div>

            </div>


            {/* Recommended Skills */}

            <div className="analysis-card">

                <h2>
                    💡 Recommended Skills
                </h2>

                <div className="analysis-list">

                    {skills.length > 0 ? (

                        skills.map(
                            (skill, index) => (

                                <div
                                    className="analysis-item"
                                    key={index}
                                >

                                    ✓ {skill}

                                </div>

                            )
                        )

                    ) : (

                        <p>
                            No recommended skills
                            available.
                        </p>

                    )}

                </div>

            </div>


            {/* Skill Gaps */}

            <div className="analysis-card">

                <h2>
                    📚 Skill Gaps
                </h2>

                <div className="analysis-list">

                    {skillGaps.length > 0 ? (

                        skillGaps.map(
                            (gap, index) => (

                                <div
                                    className="analysis-item skill-gap"
                                    key={index}
                                >

                                    ⚠ {gap}

                                </div>

                            )
                        )

                    ) : (

                        <p>
                            No major skill gaps
                            identified.
                        </p>

                    )}

                </div>

            </div>


            {/* Learning Roadmap */}

            <div className="analysis-card">

                <h2>
                    🗺️ Recommended Learning Roadmap
                </h2>

                <div className="roadmap">

                    {skillGaps.length > 0 ? (

                        skillGaps
                            .slice(0, 5)
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
                                            Step {index + 1}
                                        </h3>

                                        <p>
                                            Learn and practice{" "}
                                            <strong>
                                                {gap}
                                            </strong>
                                        </p>

                                    </div>

                                </div>

                            ))

                    ) : (

                        <p>
                            Continue building projects
                            and preparing for interviews.
                        </p>

                    )}

                </div>

            </div>

        </div>

    );

}

export default CareerAnalysis;