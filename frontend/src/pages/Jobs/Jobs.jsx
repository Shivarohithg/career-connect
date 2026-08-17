import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getAllJobs } from "../../services/jobService";
import "./Jobs.css";

function Jobs() {
  const navigate = useNavigate();

  const [jobs, setJobs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [searchTerm, setSearchTerm] = useState("");
  const [location, setLocation] = useState("");

  useEffect(() => {
    const loadJobs = async () => {
      try {
        const data = await getAllJobs();

        setJobs(data);
      } catch (error) {
        console.error("Error loading jobs:", error);

        setError("Unable to load jobs.");
      } finally {
        setLoading(false);
      }
    };

    loadJobs();
  }, []);

  const filteredJobs = jobs.filter((job) => {
    const matchesSearch =
      job.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
      job.company.toLowerCase().includes(searchTerm.toLowerCase());

    const matchesLocation = job.location
      .toLowerCase()
      .includes(location.toLowerCase());

    return matchesSearch && matchesLocation;
  });

  if (loading) {
    return <h2>Loading jobs...</h2>;
  }

  if (error) {
    return <h2>{error}</h2>;
  }

  return (
    <div className="jobs-page">
      {/* Hero Section */}
      <section className="jobs-hero">
        <h1>Find Your Next Opportunity</h1>

        <p>Discover jobs that match your skills and career goals.</p>
      </section>

      {/* Search Section */}
      <div className="search-container">
        <input
          type="text"
          placeholder="Search by job title or company"
          className="search-box"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />

        <input
          type="text"
          placeholder="Location"
          className="search-box"
          value={location}
          onChange={(e) => setLocation(e.target.value)}
        />

        <button className="search-button">Search</button>
      </div>

      {/* Jobs Section */}
      <h2 className="jobs-section-title">Available Jobs</h2>

      {/* No Jobs */}
      {filteredJobs.length === 0 ? (
        <p className="no-jobs">No jobs found.</p>
      ) : (
        /* Job Cards */
        <div className="jobs-grid">
          {filteredJobs.map((job) => (
            <div className="job-card" key={job.id}>
              {/* Company + Job Title */}
              <div className="job-card-header">
                <div className="company-logo">
                  {job.company.charAt(0).toUpperCase()}
                </div>

                <div>
                  <h3 className="job-title">{job.title}</h3>

                  <p className="job-company">{job.company}</p>
                </div>
              </div>

              {/* Job Details */}
              <div className="job-details">
                <p className="job-info">📍 {job.location}</p>

                <p className="job-salary">
                  ₹{job.salary.toLocaleString("en-IN")}
                </p>
              </div>

              {/* Job Tags */}
              <div className="job-tags">
                <span className="job-tag">Full Time</span>

                <span className="job-tag">{job.location}</span>
              </div>

              <button
                className="view-button"
                onClick={() => navigate(`/jobs/${job.id}`)}
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
