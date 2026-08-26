import { useEffect, useState } from "react";
import axios from "axios";
import "./CareerAnalysis.css";

function CareerAnalysis() {

    const [analysis, setAnalysis] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {

        const loadAnalysis = async () => {

            try {

                const studentId = 1;

                const response = await axios.get(
                    `http://localhost:8080/career-analysis/${studentId}`
                );

                setAnalysis(response.data);

            } catch (error) {

                console.error(
                    "Error loading career analysis:",
                    error
                );

                setError("Unable to load career analysis.");

            } finally {

                setLoading(false);
            }
        };

        loadAnalysis();

    }, []);

    if (loading) {
        return <h2>Loading career analysis...</h2>;
    }

    if (error) {
        return <h2>{error}</h2>;
    }

    if (!analysis) {
        return <h2>Career analysis not found.</h2>;
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

    /*
     * Extract percentage from role text.
     *
     * Example:
     * "Java Backend Developer (67% Match)"
     *
     * or
     * "Java Backend Developer (2/3 skills)"
     */

    const getPercentage = (role) => {

        const percentageMatch = role.match(/(\d+)%/);

        if (percentageMatch) {
            return parseInt(percentageMatch[1]);
        }

        const fractionMatch = role.match(/\((\d+)\/(\d+)\s*skills?\)/);

        if (fractionMatch) {

            const matched = parseInt(fractionMatch[1]);
            const total = parseInt(fractionMatch[2]);

            if (total > 0) {
                return Math.round((matched / total) * 100);
            }
        }

        return 0;
    };


    /*
     * Remove percentage / skill information
     * from role name.
     */

    const getRoleName = (role) => {

        return role
            .replace(/\s*\(\d+%\s*Match\)/i, "")
            .replace(/\s*\(\d+\/\d+\s*skills?\)/i, "")
            .trim();
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


            {/* Recommended Roles */}

            <div className="analysis-card">

                <h2>🎯 Recommended Roles</h2>

                <div className="role-list">

                    {roles.map((role, index) => {

                        const percentage = getPercentage(role);
                        const roleName = getRoleName(role);

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
                                        {percentage}% Match
                                    </span>

                                </div>


                                <div className="progress-bar">

                                    <div
                                        className="progress-fill"
                                        style={{
                                            width: `${percentage}%`
                                        }}
                                    >
                                    </div>

                                </div>

                            </div>

                        );

                    })}

                </div>

            </div>


            {/* Recommended Skills */}

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


            {/* Skill Gaps */}

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

        </div>
    );
}

export default CareerAnalysis;