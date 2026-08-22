import { useEffect, useState } from "react";
import axios from "axios";
import "./MyProfile.css";

function MyProfile() {

    const [profile, setProfile] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {

        const loadProfile = async () => {

            try {

                // Temporary student ID for testing
                const studentId = 1;

                const response = await axios.get(
                    `http://localhost:8080/student-profiles/${studentId}`
                );

                setProfile(response.data);

            } catch (error) {

                console.error("Error loading profile:", error);

                setError("Unable to load profile.");

            } finally {

                setLoading(false);
            }
        };

        loadProfile();

    }, []);

    if (loading) {
        return <h2>Loading profile...</h2>;
    }

    if (error) {
        return <h2>{error}</h2>;
    }

    if (!profile) {
        return <h2>Profile not found.</h2>;
    }

    return (
        <div className="profile-page">

            <div className="profile-header">

                <div className="profile-avatar">
                    {profile.student.name.charAt(0).toUpperCase()}
                </div>

                <div>
                    <h1>{profile.student.name}</h1>

                    <p>
                        {profile.student.branch} | CGPA: {profile.student.cgpa}
                    </p>
                </div>

            </div>

            <div className="profile-card">

                <h2>Skills</h2>
                <p>{profile.skills}</p>

            </div>

            <div className="profile-card">

                <h2>Projects</h2>
                <p>{profile.projects}</p>

            </div>

            <div className="profile-card">

                <h2>Coding Profiles</h2>

                <p>
                    <strong>GitHub:</strong>{" "}
                    {profile.githubUsername}
                </p>

                <p>
                    <strong>LeetCode:</strong>{" "}
                    {profile.leetcodeUsername}
                </p>

                <p>
                    <strong>HackerRank:</strong>{" "}
                    {profile.hackerrankUsername}
                </p>

            </div>

            <div className="profile-card">

                <h2>Resume</h2>

                <p>{profile.resumePath}</p>

            </div>

        </div>
    );
}

export default MyProfile;