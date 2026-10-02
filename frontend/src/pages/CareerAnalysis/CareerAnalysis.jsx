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

        const analysisResponse = await axios.get(
          `http://localhost:8080/career-analysis/${studentId}`
        );

        setAnalysis(analysisResponse.data);

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

  /*
   * Extract a section from Ollama's response.
   */
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
    "WHY THIS ROLE",
    [
      "CURRENT STRENGTHS",
      "SKILLS TO IMPROVE"
    ]
  );

  const aiRoadmap = getAISection(
    "LEARNING ROADMAP",
    [
      "PROJECT RECOMMENDATION",
      "INTERVIEW PREPARATION",
      "NEXT ACTION"
    ]
  );

  const projectRecommendation = getAISection(
    "PROJECT RECOMMENDATION",
    [
      "INTERVIEW PREPARATION",
      "NEXT ACTION",
      "IMPORTANT RULES"
    ]
  );

  const interviewPreparation = getAISection(
    "INTERVIEW PREPARATION",
    [
      "NEXT ACTION",
      "IMPORTANT RULES"
    ]
  );

  /*
   * Stop NEXT ACTION from accidentally including
   * Project Recommendation.
   */
  const nextAction = getAISection(
    "NEXT ACTION",
    [
      "PROJECT RECOMMENDATION",
      "INTERVIEW PREPARATION",
      "IMPORTANT RULES"
    ]
  );

  const readinessMatch = careerAdvice.match(
    /Career Readiness Score:\s*(\d+)%/i
  );

  const readinessScore = readinessMatch
    ? readinessMatch[1]
    : "0";

  /*
   * Convert AI roadmap into individual steps.
   */
  const roadmapSteps = aiRoadmap
    ? aiRoadmap
        .split(/(?=Step\s*\d+|\d+\s*[-—:.])/i)
        .map(step => step.trim())
        .filter(Boolean)
    : [];

  return (

    <div className="career-analysis-page">

      {/* HEADER */}

      <div className="career-analysis-header">

        <h1>AI Career Analysis</h1>

        <p>
          Discover suitable career roles,
          recommended skills, and areas
          for improvement.
        </p>

      </div>


      {/* AI CAREER INSIGHTS */}

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


      {/* CAREER READINESS */}

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


      {/* RECOMMENDED ROLES */}

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


      {/* RECOMMENDED SKILLS */}

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


      {/* WHY THIS ROLE */}

      {whyRole && (

        <div className="analysis-card ai-section-card">

          <h2>💡 Why This Role?</h2>

          <div className="ai-advice">

            <p>{whyRole}</p>

          </div>

        </div>

      )}


      {/* AI LEARNING ROADMAP */}

      <div className="analysis-card">

        <h2>🛣️ AI Learning Roadmap</h2>

        <div className="roadmap">

          {roadmapSteps.length > 0 ? (

            roadmapSteps.map((step, index) => (

              <div
                className="roadmap-step"
                key={index}
              >

                <div className="roadmap-number">
                  {index + 1}
                </div>

                <div>

                  <h3>
                    {step
                      .replace(/^Step\s*\d+\s*[-—:.]?\s*/i, "")
                      .replace(/^\d+\s*[-—:.]?\s*/, "")
                      .split("\n")[0]
                    }
                  </h3>

                  <p>
                    {step
                      .split("\n")
                      .slice(1)
                      .join(" ")
                      .trim() ||
                      "Follow this step through practical learning and exercises."
                    }
                  </p>

                </div>

              </div>

            ))

          ) : (

            <p>
              AI roadmap could not be generated.
              Continue improving your identified
              skill gaps through practical projects.
            </p>

          )}

        </div>

      </div>


      {/* PROJECT RECOMMENDATION */}

      {projectRecommendation && (

        <div className="analysis-card ai-section-card project-ai-card">

          <h2>🚀 Project Recommendation</h2>

          <div className="ai-advice">

            <p>{projectRecommendation}</p>

          </div>

        </div>

      )}


      {/* INTERVIEW PREPARATION */}

      {interviewPreparation && (

        <div className="analysis-card ai-section-card interview-ai-card">

          <h2>🎤 Interview Preparation</h2>

          <div className="ai-advice">

            <p>{interviewPreparation}</p>

          </div>

        </div>

      )}


      {/* NEXT ACTION */}

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
