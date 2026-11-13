
import { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { resetPassword } from "../../services/authService";

import "./ResetPassword.css";

function ResetPassword() {

    const navigate = useNavigate();
    const location = useLocation();

    const email = location.state?.email;

    const [newPassword, setNewPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");

    const [error, setError] = useState("");
    const [message, setMessage] = useState("");
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();

        setError("");
        setMessage("");

        if (newPassword.length < 8) {
            setError("Password must contain at least 8 characters.");
            return;
        }

        if (newPassword !== confirmPassword) {
            setError("Passwords do not match.");
            return;
        }

        setLoading(true);

        try {

            const response = await resetPassword(
                email,
                newPassword
            );

            setMessage(response);

            setTimeout(() => {
                navigate("/login", { replace: true });
            }, 2000);

        } catch (err) {

            setError(
                err.response?.data || "Unable to reset password. Please try again."
            );

        } finally {
            setLoading(false);
        }
    };

    // User must complete OTP verification first
    if (!email) {
        return (
            <div className="reset-page">
                <div className="reset-card">
                    <h1>Reset Password</h1>
                    <p>Please verify your email OTP first.</p>

                    <button onClick={() => navigate("/forgot-password")}>
                        Go to Forgot Password
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div className="reset-page">

            <div className="reset-card">

                <h1>Reset Password</h1>

                <p>Create a new password for your CareerConnect account.</p>

                {error && (
                    <div className="error-message">
                        {error}
                    </div>
                )}

                {message && (
                    <div className="success-message">
                        {message}
                    </div>
                )}

                <form onSubmit={handleSubmit}>

                    <input
                        type="password"
                        placeholder="Enter new password"
                        value={newPassword}
                        onChange={(e) => setNewPassword(e.target.value)}
                        minLength={8}
                        required
                    />

                    <input
                        type="password"
                        placeholder="Confirm new password"
                        value={confirmPassword}
                        onChange={(e) => setConfirmPassword(e.target.value)}
                        minLength={8}
                        required
                    />

                    <button type="submit" disabled={loading || !!message}>
                        {loading ? "Updating..." : "Reset Password"}
                    </button>

                </form>

                <p className="back-login">
                    Remember your password?{" "}
                    <span onClick={() => navigate("/login")}>
                        Login
                    </span>
                </p>

            </div>

        </div>
    );
}

export default ResetPassword;
