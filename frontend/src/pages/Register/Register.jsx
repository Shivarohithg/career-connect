import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { registerStudent } from "../../services/authService";
import "./Register.css";

function Register() {
    const navigate = useNavigate();

    const [formData, setFormData] = useState({
        name: "",
        branch: "",
        cgpa: "",
        email: "",
        password: ""
    });

    const [message, setMessage] = useState("");
    const [error, setError] = useState("");

    const handleChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        setMessage("");
        setError("");

        try {
            await registerStudent({
                ...formData,
                cgpa: Number(formData.cgpa)
            });

            setMessage("Registration successful! Redirecting to login...");

            setTimeout(() => {
                navigate("/login");
            }, 1500);

        } catch (error) {
            console.error(error);
            setError(
                error.response?.data || "Registration failed"
            );
        }
    };

    return (
        <div className="register-page">
            <div className="register-card">

                <h1>Create Account</h1>
                <p>Join CareerConnect AI</p>

                {message && (
                    <div className="success-message">
                        {message}
                    </div>
                )}

                {error && (
                    <div className="error-message">
                        {error}
                    </div>
                )}

                <form onSubmit={handleSubmit}>

                    <input
                        type="text"
                        name="name"
                        placeholder="Full Name"
                        value={formData.name}
                        onChange={handleChange}
                        required
                    />

                    <input
                        type="text"
                        name="branch"
                        placeholder="Branch (e.g. CSE)"
                        value={formData.branch}
                        onChange={handleChange}
                        required
                    />

                    <input
                        type="number"
                        name="cgpa"
                        placeholder="CGPA"
                        min="0"
                        max="10"
                        step="0.01"
                        value={formData.cgpa}
                        onChange={handleChange}
                        required
                    />

                    <input
                        type="email"
                        name="email"
                        placeholder="Email"
                        value={formData.email}
                        onChange={handleChange}
                        required
                    />

                    <input
                        type="password"
                        name="password"
                        placeholder="Password"
                        value={formData.password}
                        onChange={handleChange}
                        required
                    />

                    <button type="submit">
                        Register
                    </button>

                </form>

                <p className="login-link">
                    Already have an account?{" "}
                    <span onClick={() => navigate("/login")}>
                        Login
                    </span>
                </p>

            </div>
        </div>
    );
}

export default Register;