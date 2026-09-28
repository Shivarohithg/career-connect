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
        // Get logged-in student
        const storedStudent = localStorage.getItem("student");

        if (!storedStudent) {
          window.location.href = "/login";
          return;
        }

        const student = JSON.parse(storedStudent);

        const studentId = student.id;



        // Get career analysis
        const analysisResponse = await axios.get(
          `http://localhost:8080/career-analysis/${studentId}`,
        );

        setAnalysis(analysisResponse.data);

        // Get AI career recommendation
        const adviceResponse = await axios.get(
          `http://localhost:8080/career-recommendation/${studentId}`,
        );

        setCareerAdvice(adviceResponse.data);
      } catch (error) {
        console.error("Error loading career analysis:", error);

        setError(error.response?.data || "Unable to load career analysis.");
      } finally {
        setLoading(false);
        setAdviceLoading(false);
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
    ? analysis.recommendedRoles.split(",").map((role) => role.trim())
    : [];

  const skills = analysis.recommendedSkills
    ? analysis.recommendedSkills.split(",").map((skill) => skill.trim())
    : [];

  const skillGaps = analysis.skillGaps
    ? analysis.skillGaps.split(",").map((gap) => gap.trim())
    : [];

  const getPercentage = (role) => {
    const percentageMatch = role.match(/(\d+)%/);

    if (percentageMatch) {
      return parseInt(percentageMatch[1]);
    }

    return 0;
  };

  const getRoleName = (role) => {
    return role.replace(/\s*\(\d+%\s*Match\)/i, "").trim();
  };

  return (
    <div className="career-analysis-page">
      {/* Header */}

      <div className="career-analysis-header">
        <h1>AI Career Analysis</h1>

        <p>
          Discover suitable career roles, recommended skills, and areas for
          improvement.
        </p>
      </div>

      {/* AI Career Insights */}

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

      {/* Career Readiness */}

      <div className="analysis-card career-score-card">
        <h2>📊 Career Readiness</h2>

        <div className="career-score">
          <div className="score-number">
            {careerAdvice.match(/Career Readiness Score:\s*(\d+)%/)?.[1] || 0}%
          </div>

          <div className="score-text">
            <strong>Current Readiness</strong>

            <p>Based on your current skills and identified skill gaps.</p>
          </div>
        </div>
      </div>

      {/* Recommended Roles */}

      <div className="analysis-card">
        <h2>🎯 Recommended Roles</h2>

        <div className="role-list">
          {roles.map((role, index) => {
            const percentage = getPercentage(role);

            const roleName = getRoleName(role);

            return (
              <div className="role-match" key={index}>
                <div className="role-match-header">
                  <span className="role-name">{roleName}</span>

                  <span className="match-percentage">{percentage}% Match</span>
                </div>

                <div className="progress-bar">
                  <div
                    className="progress-fill"
                    style={{
                      width: `${percentage}%`,
                    }}
                  ></div>
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
            <div className="analysis-item" key={index}>
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
            <div className="analysis-item skill-gap" key={index}>
              ⚠ {gap}
            </div>
          ))}
        </div>
      </div>

      {/* Learning Roadmap */}

      <div className="analysis-card">
        <h2>🛣️ Learning Roadmap</h2>

        <div className="roadmap">
          {skillGaps.slice(0, 4).map((gap, index) => (
            <div className="roadmap-step" key={index}>
              <div className="roadmap-number">{index + 1}</div>

              <div>
                <h3>Learn {gap}</h3>

                <p>Improve this skill to increase your career readiness.</p>
              </div>
            </div>
          ))}

          {skillGaps.length === 0 && (
            <p>
              Continue improving your existing skills through projects and
              interview practice.
            </p>
          )}
        </div>
      </div>
    </div>
  );
}

export default CareerAnalysis;
