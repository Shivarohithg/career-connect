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

                setError(
                    "Unable to load career analysis."
                );

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

    const roles =
        analysis.recommendedRoles
            .split(",")
            .map(role => role.trim());

    const skills =
        analysis.recommendedSkills
            .split(",")
            .map(skill => skill.trim());

    const skillGaps =
        analysis.skillGaps
            .split(",")
            .map(gap => gap.trim());

    return (
        <div className="career-analysis-page">

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

                <h2>Recommended Roles</h2>

                <div className="analysis-list">

                    {roles.map((role, index) => (

                        <div
                            className="analysis-item"
                            key={index}
                        >
                            ✓ {role}
                        </div>

                    ))}

                </div>

            </div>


            {/* Recommended Skills */}

            <div className="analysis-card">

                <h2>Recommended Skills</h2>

                <div className="analysis-list">

                    {skills.map((skill, index) => (

                        <div
                            className="analysis-item"
                            key={index}
                        >
                            {skill}
                        </div>

                    ))}

                </div>

            </div>


            {/* Skill Gaps */}

            <div className="analysis-card">

                <h2>Skill Gaps</h2>

                <div className="analysis-list">

                    {skillGaps.map((gap, index) => (

                        <div
                            className="analysis-item"
                            key={index}
                        >
                            {gap}
                        </div>

                    ))}

                </div>

            </div>

        </div>
    );
}

export default CareerAnalysis;