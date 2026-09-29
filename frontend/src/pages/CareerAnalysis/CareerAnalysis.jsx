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

        const storedStudent = localStorage.getItem("student");

        if (!storedStudent) {
          window.location.href = "/login";
          return;
        }

        const student = JSON.parse(storedStudent);
        const studentId = student.id;

        // Get career analysis
        const analysisResponse = await axios.get(
          `http://localhost:8080/career-analysis/${studentId}`
        );

        setAnalysis(analysisResponse.data);

        // Get AI career recommendation
        const adviceResponse = await axios.get(
          `http://localhost:8080/career-recommendation/${studentId}`
        );

        setCareerAdvice(adviceResponse.data);

      } catch (error) {

        console.error("Error loading career analysis:", error);

        setError(
          typeof error.response?.data === "string"
            ? error.response.data
            : "Unable to load career analysis."
        );

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

  // ==========================================
  // Existing Career Analysis Data
  // ==========================================

  const roles = analysis.recommendedRoles
    ? analysis.recommendedRoles
        .split(",")
        .map(role => role.trim())
        .filter(Boolean)
    : [];

  const skills = analysis.recommendedSkills
    ? analysis.recommendedSkills
        .split(",")
        .map(skill => skill.trim())
        .filter(Boolean)
    : [];

  const skillGaps = analysis.skillGaps
    ? analysis.skillGaps
        .split(",")
        .map(gap => gap.trim())
        .filter(Boolean)
    : [];

  const getPercentage = (role) => {

    const percentageMatch = role.match(/(\d+)%/);

    return percentageMatch
      ? parseInt(percentageMatch[1])
      : 0;
  };

  const getRoleName = (role) => {

    return role
      .replace(/\s*\(\d+%\s*Match\)/i, "")
      .trim();
  };

  // ==========================================
  // Extract AI Sections
  // ==========================================

  const getAISection = (start, ends = []) => {

    const text = careerAdvice || "";

    const startIndex = text.toLowerCase().indexOf(
      start.toLowerCase()
    );

    if (startIndex === -1) {
      return "";
    }

    let content = text.substring(
      startIndex + start.length
    );

    let endIndex = content.length;

    ends.forEach(end => {

      const index = content
        .toLowerCase()
        .indexOf(end.toLowerCase());

      if (index !== -1 && index < endIndex) {
        endIndex = index;
      }

    });

    return content
      .substring(0, endIndex)
      .replace(/^[\s:*]+/, "")
      .trim();
  };

  const careerInsights = getAISection(
    "CAREER INSIGHTS",
    [
      "WHY THIS ROLE",
      "CURRENT STRENGTHS"
    ]
  );

  const whyRole = getAISection(
    "Why This Role",
    [
      "CURRENT STRENGTHS",
      "SKILLS TO IMPROVE"
    ]
  );

  const projectRecommendation = getAISection(
    "Project Recommendation",
    [
      "Interview Preparation",
      "NEXT ACTION"
    ]
  );

  const interviewPreparation = getAISection(
    "Interview Preparation",
    [
      "NEXT ACTION",
      "IMPORTANT RULES"
    ]
  );

  const nextAction = getAISection(
    "NEXT ACTION",
    [
      "IMPORTANT RULES"
    ]
  );

  const readinessMatch = careerAdvice.match(
    /Career Readiness Score:\s*(\d+)%/i
  );

  const readinessScore = readinessMatch
    ? readinessMatch[1]
    : "0";

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


      {/* AI Career Insights */}

      <div className="analysis-card ai-career-card">

        <h2>🤖 AI Career Insights</h2>

        {adviceLoading ? (

          <p>
            Generating personalized career insights...
          </p>

        ) : (

          <div className="ai-advice">

            {careerInsights ? (
              <p>{careerInsights}</p>
            ) : (
              <p>
                Your profile has been analyzed
                based on your current skills,
                career roles, and skill gaps.
              </p>
            )}

          </div>

        )}

      </div>


      {/* Career Readiness */}

      <div className="analysis-card career-score-card">

        <h2>📊 Career Readiness</h2>

        <div className="career-score">

          <div className="score-number">
            {readinessScore}%
          </div>

          <div className="score-text">

            <strong>
              Current Readiness
            </strong>

            <p>
              Based on your current skills
              and identified skill gaps.
            </p>

          </div>

        </div>

      </div>


      {/* Recommended Roles */}

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
                  />

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


      {/* AI Why This Role */}

      {whyRole && (

        <div className="analysis-card ai-section-card">

          <h2>💡 Why This Role?</h2>

          <div className="ai-advice">
            <p>{whyRole}</p>
          </div>

        </div>

      )}


      {/* AI Learning Roadmap */}

      <div className="analysis-card">

        <h2>🛣️ AI Learning Roadmap</h2>

        <div className="roadmap">

          {skillGaps.length > 0 ? (

            skillGaps.slice(0, 4).map(
              (gap, index) => (

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
                      Focus on {gap} through
                      practical exercises and
                      project-based learning.
                    </p>

                  </div>

                </div>

              )
            )

          ) : (

            <p>
              Continue improving your existing
              skills through projects and
              interview practice.
            </p>

          )}

        </div>

      </div>


      {/* AI Project Recommendation */}

      {projectRecommendation && (

        <div className="analysis-card ai-section-card">

          <h2>🚀 Project Recommendation</h2>

          <div className="ai-advice">
            <p>{projectRecommendation}</p>
          </div>

        </div>

      )}


      {/* AI Interview Preparation */}

      {interviewPreparation && (

        <div className="analysis-card ai-section-card">

          <h2>🎤 Interview Preparation</h2>

          <div className="ai-advice">
            <p>{interviewPreparation}</p>
          </div>

        </div>

      )}


      {/* Next Action */}

      {nextAction && (

        <div className="analysis-card next-action-card">

          <h2>🚀 Your Next Action</h2>

          <div className="ai-advice">
            <p>{nextAction}</p>
          </div>

        </div>

      )}

    </div>
  );
}

export default CareerAnalysis;
