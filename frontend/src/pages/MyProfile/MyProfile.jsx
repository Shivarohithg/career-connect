import { useEffect, useState } from "react";
import axios from "axios";
import "./MyProfile.css";

function MyProfile() {

    const [profile, setProfile] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const [editing, setEditing] = useState(false);
    const [saving, setSaving] = useState(false);

    const [resumeFile, setResumeFile] = useState(null);
    const [uploadingResume, setUploadingResume] = useState(false);

    const [formData, setFormData] = useState({
        skills: "",
        projects: "",
        githubUsername: "",
        leetcodeUsername: "",
        hackerrankUsername: "",
        resumePath: ""
    });

    useEffect(() => {

        const loadProfile = async () => {

            try {

                // Temporary student ID for testing
                const studentId = 1;

                const response = await axios.get(
                    `http://localhost:8080/student-profiles/${studentId}`
                );

                setProfile(response.data);

                setFormData({
                    skills: response.data.skills || "",
                    projects: response.data.projects || "",
                    githubUsername: response.data.githubUsername || "",
                    leetcodeUsername: response.data.leetcodeUsername || "",
                    hackerrankUsername: response.data.hackerrankUsername || "",
                    resumePath: response.data.resumePath || ""
                });

            } catch (error) {

                console.error("Error loading profile:", error);

                setError("Unable to load profile.");

            } finally {

                setLoading(false);
            }
        };

        loadProfile();

    }, []);

    const handleChange = (e) => {

        setFormData({
            ...formData,
            [e.target.name]: e.target.value
        });

    };

    const handleSave = async () => {

        try {

            setSaving(true);

            const studentId = 1;

            const response = await axios.put(
                `http://localhost:8080/student-profiles/${studentId}`,
                null,
                {
                    params: formData
                }
            );

            setProfile(response.data);

            setEditing(false);

        } catch (error) {

            console.error("Error updating profile:", error);

            setError("Unable to update profile.");

        } finally {

            setSaving(false);
        }
    };

    const handleResumeUpload = async () => {

        if (!resumeFile) {
            alert("Please select a PDF first.");
            return;
        }

        if (resumeFile.type !== "application/pdf") {
            alert("Please select a PDF file.");
            return;
        }

        try {

            setUploadingResume(true);

            const studentId = 1;

            const uploadData = new FormData();

            uploadData.append("resume", resumeFile);

            const response = await axios.post(
                `http://localhost:8080/student-profiles/${studentId}/resume`,
                uploadData
            );

            console.log(response.data);

            // Reload profile after successful upload
            const profileResponse = await axios.get(
                `http://localhost:8080/student-profiles/${studentId}`
            );

            setProfile(profileResponse.data);

            setFormData({
                skills: profileResponse.data.skills || "",
                projects: profileResponse.data.projects || "",
                githubUsername: profileResponse.data.githubUsername || "",
                leetcodeUsername: profileResponse.data.leetcodeUsername || "",
                hackerrankUsername: profileResponse.data.hackerrankUsername || "",
                resumePath: profileResponse.data.resumePath || ""
            });

            setResumeFile(null);

            alert("Resume uploaded successfully!");

        } catch (error) {

            console.error("Resume upload error:", error);

            alert("Failed to upload resume.");

        } finally {

            setUploadingResume(false);
        }
    };

    if (loading) {
        return <h2>Loading profile...</h2>;
    }

    if (error && !profile) {
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

                <button
                    className="edit-profile-button"
                    onClick={() => setEditing(true)}
                >
                    Edit Profile
                </button>

            </div>

            {error && (
                <p className="profile-error">
                    {error}
                </p>
            )}

            {editing ? (

                <div className="profile-edit-section">

                    {/* Skills */}

                    <div className="profile-card">

                        <h2>Skills</h2>

                        <input
                            type="text"
                            name="skills"
                            value={formData.skills}
                            onChange={handleChange}
                            placeholder="Enter your skills"
                        />

                    </div>


                    {/* Projects */}

                    <div className="profile-card">

                        <h2>Projects</h2>

                        <textarea
                            name="projects"
                            value={formData.projects}
                            onChange={handleChange}
                            placeholder="Enter your projects"
                        />

                    </div>


                    {/* Coding Profiles */}

                    <div className="profile-card">

                        <h2>Coding Profiles</h2>

                        <input
                            type="text"
                            name="githubUsername"
                            value={formData.githubUsername}
                            onChange={handleChange}
                            placeholder="GitHub username"
                        />

                        <input
                            type="text"
                            name="leetcodeUsername"
                            value={formData.leetcodeUsername}
                            onChange={handleChange}
                            placeholder="LeetCode username"
                        />

                        <input
                            type="text"
                            name="hackerrankUsername"
                            value={formData.hackerrankUsername}
                            onChange={handleChange}
                            placeholder="HackerRank username"
                        />

                    </div>


                    {/* Resume Upload */}

                    <div className="profile-card">

                        <h2>Resume</h2>

                        <p>
                            <strong>Current resume:</strong>{" "}
                            {profile.resumePath
                                ? profile.resumePath
                                : "No resume uploaded"}
                        </p>

                        <input
                            type="file"
                            accept=".pdf"
                            onChange={(e) =>
                                setResumeFile(e.target.files[0])
                            }
                        />

                        {resumeFile && (
                            <p>
                                <strong>Selected file:</strong>{" "}
                                {resumeFile.name}
                            </p>
                        )}

                        <button
                            className="upload-resume-button"
                            onClick={handleResumeUpload}
                            disabled={uploadingResume}
                        >
                            {uploadingResume
                                ? "Uploading..."
                                : "Upload Resume"}
                        </button>

                    </div>


                    {/* Save / Cancel */}

                    <div className="profile-actions">

                        <button
                            className="save-profile-button"
                            onClick={handleSave}
                            disabled={saving}
                        >
                            {saving
                                ? "Saving..."
                                : "Save Changes"}
                        </button>

                        <button
                            className="cancel-profile-button"
                            onClick={() => setEditing(false)}
                        >
                            Cancel
                        </button>

                    </div>

                </div>

            ) : (

                <>

                    {/* Skills */}

                    <div className="profile-card">

                        <h2>Skills</h2>

                        <p>
                            {profile.skills}
                        </p>

                    </div>


                    {/* Projects */}

                    <div className="profile-card">

                        <h2>Projects</h2>

                        <p>
                            {profile.projects}
                        </p>

                    </div>


                    {/* Coding Profiles */}

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


                    {/* Resume */}

                    <div className="profile-card">

                        <h2>Resume</h2>

                        <p>
                            {profile.resumePath
                                ? profile.resumePath
                                : "No resume uploaded"}
                        </p>

                    </div>

                </>

            )}

        </div>
    );
}

export default MyProfile;