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
        // 0. Get logged-in student
        // =====================================

        const storedStudent =
          localStorage.getItem("student");

        if (!storedStudent) {
          navigate("/login");
          return;
        }

        const student = JSON.parse(storedStudent);
        const studentId = student.id;

        console.log(
          "Logged-in student ID:",
          studentId
        );


        // =====================================
        // 1. Load all jobs
        // =====================================

        const jobsData = await getAllJobs();

        setJobs(jobsData);


        // =====================================
        // 2. Load Career Analysis
        // =====================================

        const analysisResponse = await axios.get(
          `http://localhost:8080/career-analysis/${studentId}`
        );

        const analysis = analysisResponse.data;


        // =====================================
        // 3. Load Student Profile
        // =====================================

        const profileResponse = await axios.get(
          `http://localhost:8080/student-profiles/${studentId}`
        );

        const profile = profileResponse.data;


        // =====================================
        // 4. Student Profile Skills
        // =====================================

        const profileSkills = profile.skills
          ? profile.skills
              .split(",")
              .map(skill => skill.trim().toLowerCase())
              .filter(skill => skill.length > 2)
          : [];


        // =====================================
        // 5. Job Role Required Skills
        // =====================================

        const jobRoles = {

          "java backend developer": [
            "java",
            "spring boot",
            "sql"
          ],

          "full stack developer": [
            "javascript",
            "react",
            "html",
            "css",
            "node.js"
          ],

          "frontend developer": [
            "html",
            "css",
            "javascript",
            "react",
            "git"
          ],

          "python developer": [
            "python",
            "sql",
            "git",
            "rest apis"
          ],

          "software engineer": [
            "java",
            "dsa",
            "oop",
            "git",
            "sql"
          ],

          "data analyst": [
            "python",
            "sql",
            "excel",
            "statistics",
            "power bi"
          ]

        };


        // =====================================
        // 6. Calculate Profile → Role Match
        // =====================================

        const roleMatches = Object.entries(jobRoles).map(
          ([role, requiredSkills]) => {

            const matchedSkills = requiredSkills.filter(
              requiredSkill =>
                profileSkills.some(profileSkill =>
                  profileSkill.includes(requiredSkill) ||
                  requiredSkill.includes(profileSkill)
                )
            );

            const percentage =
              requiredSkills.length > 0
                ? Math.round(
                    (matchedSkills.length /
                      requiredSkills.length) *
                      100
                  )
                : 0;

            return {
              role,
              percentage,
              matchedSkills
            };

          }
        );


        // =====================================
        // 7. Get Career Analysis Roles
        // =====================================

        const analysisRoles = analysis.recommendedRoles
          ? analysis.recommendedRoles
              .split(",")
              .map(role =>
                role
                  .replace(
                    /\(\d+%\s*Match\)/i,
                    ""
                  )
                  .replace(
                    /\(\d+\/\d+\s*skills\)/i,
                    ""
                  )
                  .trim()
                  .toLowerCase()
              )
          : [];


        // =====================================
        // 8. Find Recommended Jobs
        // =====================================

        const matchedJobs = jobsData.filter(job => {

          const jobTitle =
            job.title.toLowerCase();


          // -------------------------------------
          // Match through Career Analysis
          // -------------------------------------

          const careerAnalysisMatch =
            analysisRoles.some(role => {

              const roleWords =
                role.split(" ");

              return roleWords.some(word => {

                // Ignore generic words
                if (
                  word.length <= 2 ||
                  word === "developer" ||
                  word === "engineer"
                ) {
                  return false;
                }

                return jobTitle.includes(word);

              });

            });


          // -------------------------------------
          // Match through Profile Skills
          // -------------------------------------

          const profileSkillMatch =
            profileSkills.some(
              skill =>
                jobTitle.includes(skill)
            );


          // -------------------------------------
          // Match through Job Roles
          // -------------------------------------

          const roleSkillMatch =
            roleMatches.some(roleData => {

              // Only consider roles where
              // student has at least one matching skill

              if (roleData.percentage === 0) {
                return false;
              }

              const roleName =
                roleData.role;

              // Match meaningful role words
              const roleWords =
                roleName.split(" ");

              return roleWords.some(word => {

                if (
                  word.length <= 2 ||
                  word === "developer" ||
                  word === "engineer"
                ) {
                  return false;
                }

                return jobTitle.includes(word);

              });

            });


          return (
            careerAnalysisMatch ||
            profileSkillMatch ||
            roleSkillMatch
          );

        });


        setRecommendedJobs(matchedJobs);

      } catch (error) {

        console.error(
          "Error loading jobs:",
          error
        );

        setError(
          "Unable to load jobs."
        );

      } finally {

        setLoading(false);

      }

    };


    loadData();

  }, [navigate]);


  // =====================================
  // Search Filtering
  // =====================================

  const filteredJobs = jobs.filter(job => {

    const matchesSearch =
      job.title
        .toLowerCase()
        .includes(searchTerm.toLowerCase()) ||

      job.company
        .toLowerCase()
        .includes(searchTerm.toLowerCase());


    const matchesLocation =
      job.location
        .toLowerCase()
        .includes(location.toLowerCase());


    return matchesSearch && matchesLocation;

  });


  // =====================================
  // Loading
  // =====================================

  if (loading) {

    return (
      <h2>
        Loading jobs...
      </h2>
    );

  }


  // =====================================
  // Error
  // =====================================

  if (error) {

    return (
      <h2>
        {error}
      </h2>
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
      {/* Recommended Jobs */}
      {/* ============================= */}

      {recommendedJobs.length > 0 && (

        <>

          <h2 className="jobs-section-title">
            ⭐ Recommended Jobs
          </h2>


          <div className="jobs-grid">

            {recommendedJobs.map(job => (

              <div
                className="job-card recommended-card"
                key={`recommended-${job.id}`}
              >

                {/* Company + Job Title */}

                <div className="job-card-header">

                  <div className="company-logo">

                    {job.company
                      .charAt(0)
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
                    {job.salary.toLocaleString(
                      "en-IN"
                    )}
                  </p>

                </div>


                {/* Tags */}

                <div className="job-tags">

                  <span className="job-tag">
                    Recommended
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

            ))}

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
                    .charAt(0)
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
                  {job.salary.toLocaleString(
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