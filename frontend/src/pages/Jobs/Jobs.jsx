import { useEffect, useState } from "react";
import { getAllJobs } from "../../services/jobService";

function Jobs() {

    const [jobs, setJobs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

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

    useEffect(() => {
        loadJobs();
    }, []);

    if (loading) {
        return <h2>Loading jobs...</h2>;
    }

    if (error) {
        return <h2>{error}</h2>;
    }

    return (
        <div>
            <h1>Available Jobs</h1>

            {jobs.length === 0 ? (
                <p>No jobs available.</p>
            ) : (
                jobs.map((job) => (
                    <div key={job.id}>

                        <h2>{job.title}</h2>

                        <p>Company: {job.company}</p>

                        <p>Location: {job.location}</p>

                        <p>Salary: ₹{job.salary}</p>

                        <hr />

                    </div>
                ))
            )}
        </div>
    );
}

export default Jobs;