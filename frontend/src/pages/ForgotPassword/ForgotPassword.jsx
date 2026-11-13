
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import {
    sendPasswordResetOtp,
    verifyPasswordResetOtp
} from "../../services/authService";

import "./ForgotPassword.css";

function ForgotPassword() {

    const navigate = useNavigate();

    const [email, setEmail] = useState("");
    const [otp, setOtp] = useState("");

    const [otpSent, setOtpSent] = useState(false);
    const [loading, setLoading] = useState(false);

    const [message, setMessage] = useState("");
    const [error, setError] = useState("");

    const handleSendOtp = async (e) => {
        e.preventDefault();

        setError("");
        setMessage("");
        setLoading(true);

        try {
            const response = await sendPasswordResetOtp(email.trim());

            setOtpSent(true);
            setMessage(response);

        } catch (err) {
            setError(
                err.response?.data || "Unable to send OTP. Please try again."
            );
        } finally {
            setLoading(false);
        }
    };

    const handleVerifyOtp = async (e) => {
        e.preventDefault();

        setError("");
        setMessage("");
        setLoading(true);

        try {
await verifyPasswordResetOtp(
    email.trim(),
    otp.trim()
);

// Navigate after successful OTP verification
navigate("/reset-password", {
    state: { email: email.trim() }
});

            // Pass email to the reset page for this flow.
            navigate("/reset-password", {
                state: { email: email.trim() }
            });

        } catch (err) {
            setError(
                err.response?.data || "Invalid OTP. Please try again."
            );
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="forgot-page">
            <div className="forgot-card">

                <h1>Forgot Password?</h1>

                <p>
                    {otpSent
                        ? "Enter the OTP sent to your registered email."
                        : "Enter your registered email to reset your password."}
                </p>

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

                {!otpSent ? (

                    <form onSubmit={handleSendOtp}>

                        <input
                            type="email"
                            placeholder="Enter registered email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            required
                        />

                        <button type="submit" disabled={loading}>
                            {loading ? "Sending..." : "Send OTP"}
                        </button>

                    </form>

                ) : (

                    <form onSubmit={handleVerifyOtp}>

                        <input
                            type="text"
                            placeholder="Enter 6-digit OTP"
                            value={otp}
                            onChange={(e) => setOtp(e.target.value)}
                            maxLength={6}
                            pattern="[0-9]{6}"
                            inputMode="numeric"
                            required
                        />

                        <button type="submit" disabled={loading}>
                            {loading ? "Verifying..." : "Verify OTP"}
                        </button>

                    </form>

                )}

                <p className="back-login">
                    Remember your password?{" "}
                    <span onClick={() => navigate("/login")}>
                        Back to Login
                    </span>
                </p>

            </div>
        </div>
    );
}

export default ForgotPassword;
