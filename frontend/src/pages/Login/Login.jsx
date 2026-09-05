import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { loginStudent } from "../../services/authService";
import "./Login.css";

function Login() {
    const navigate = useNavigate();

    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");

    const handleSubmit = async (e) => {
        e.preventDefault();

        setError("");

        try {
            const student = await loginStudent(email, password);

            // Store logged-in student information
            localStorage.setItem("student", JSON.stringify(student));

            navigate("/dashboard");

        } catch (error) {
            console.error(error);
            setError(
                error.response?.data || "Invalid email or password"
            );
        }
    };

    return (
        <div className="login-page">
            <div className="login-card">

                <h1>Welcome Back</h1>
                <p>Login to CareerConnect AI</p>

                {error && (
                    <div className="error-message">
                        {error}
                    </div>
                )}

                <form onSubmit={handleSubmit}>

                    <input
                        type="email"
                        placeholder="Email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        required
                    />

                    <input
                        type="password"
                        placeholder="Password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required
                    />

                    <button type="submit">
                        Login
                    </button>

                </form>

                <p className="register-link">
                    Don't have an account?{" "}
                    <span onClick={() => navigate("/register")}>
                        Register
                    </span>
                </p>

            </div>
        </div>
    );
}

export default Login;