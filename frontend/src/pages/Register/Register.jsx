
import { useState } from "react";
import { useNavigate } from "react-router-dom";

import {
    registerStudent,
    sendOtp,
    verifyOtp
} from "../../services/authService";

import { FaEye, FaEyeSlash } from "react-icons/fa";

import "./Register.css";

function Register() {

    const navigate = useNavigate();

    // Registration form data
    const [formData, setFormData] = useState({
        name: "",
        branch: "",
        cgpa: "",
        email: "",
        password: ""
    });

    // Password visibility
    const [showPassword, setShowPassword] = useState(false);

    // OTP states
    const [otp, setOtp] = useState("");
    const [otpSent, setOtpSent] = useState(false);
    const [otpVerified, setOtpVerified] = useState(false);

    // Loading states
    const [sendingOtp, setSendingOtp] = useState(false);
    const [verifyingOtp, setVerifyingOtp] = useState(false);
    const [registering, setRegistering] = useState(false);

    // Messages
    const [message, setMessage] = useState("");
    const [error, setError] = useState("");

    // ================= HANDLE INPUT CHANGE =================

    const handleChange = (e) => {

        const { name, value } = e.target;

        setFormData((prev) => ({
            ...prev,
            [name]: value
        }));

        // Reset OTP verification when email changes
        if (name === "email") {
            setOtp("");
            setOtpSent(false);
            setOtpVerified(false);

            setMessage("");
            setError("");
        }
    };

    // ================= SEND OTP =================

    const handleSendOtp = async () => {

        setMessage("");
        setError("");

        const email = formData.email.trim();

        // Email validation
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

        if (!emailRegex.test(email)) {
            setError("Please enter a valid email address.");
            return;
        }

        try {

            setSendingOtp(true);

            const response = await sendOtp(email);

            setOtpSent(true);
            setOtpVerified(false);
            setOtp("");

            setMessage(
                typeof response === "string"
                    ? response
                    : "OTP sent successfully! Please check your email."
            );

        } catch (error) {

            console.error("Send OTP error:", error);

            setError(
                error.response?.data?.message ||
                (typeof error.response?.data === "string"
                    ? error.response.data
                    : null) ||
                "Failed to send OTP. Please try again."
            );

        } finally {

            setSendingOtp(false);

        }
    };

    // ================= VERIFY OTP =================

    const handleVerifyOtp = async () => {

        setMessage("");
        setError("");

        const email = formData.email.trim();

        if (!otp.trim()) {
            setError("Please enter the OTP.");
            return;
        }

        if (!/^\d{6}$/.test(otp.trim())) {
            setError("OTP must contain exactly 6 digits.");
            return;
        }

        try {

            setVerifyingOtp(true);

            const response = await verifyOtp(
                email,
                otp.trim()
            );

            setOtpVerified(true);

            setMessage(
                typeof response === "string"
                    ? response
                    : "Email verified successfully!"
            );

        } catch (error) {

            console.error("OTP verification error:", error);

            setOtpVerified(false);

            setError(
                error.response?.data?.message ||
                (typeof error.response?.data === "string"
                    ? error.response.data
                    : null) ||
                "Invalid or expired OTP. Please try again."
            );

        } finally {

            setVerifyingOtp(false);

        }
    };

    // ================= REGISTER STUDENT =================

    const handleSubmit = async (e) => {

        e.preventDefault();

        setMessage("");
        setError("");

        // Email format validation
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

        if (!emailRegex.test(formData.email.trim())) {
            setError("Please enter a valid email address.");
            return;
        }

        // OTP verification check
        if (!otpVerified) {
            setError("Please verify your email before registering.");
            return;
        }

        // CGPA validation
        const cgpa = Number(formData.cgpa);

        if (
            formData.cgpa === "" ||
            !Number.isFinite(cgpa) ||
            cgpa < 0 ||
            cgpa > 10
        ) {
            setError("CGPA must be between 0 and 10.");
            return;
        }

        // Password validation
        if (formData.password.length < 8) {
            setError("Password must contain at least 8 characters.");
            return;
        }

        try {

            setRegistering(true);

            await registerStudent({
                ...formData,
                email: formData.email.trim(),
                cgpa: cgpa
            });

            setMessage(
                "Registration successful! Redirecting to login..."
            );

            setTimeout(() => {
                navigate("/login");
            }, 1500);

        } catch (error) {

            console.error("Registration error:", error);

            setError(
                error.response?.data?.message ||
                (typeof error.response?.data === "string"
                    ? error.response.data
                    : null) ||
                "Registration failed."
            );

        } finally {

            setRegistering(false);

        }
    };

    // ================= UI =================

    return (

        <div className="register-page">

            <div className="register-card">

                <h1>Create Account</h1>

                <p>Join CareerConnect AI</p>

                {/* Success Message */}

                {message && (
                    <div className="success-message">
                        {message}
                    </div>
                )}

                {/* Error Message */}

                {error && (
                    <div className="error-message">
                        {error}
                    </div>
                )}

                <form onSubmit={handleSubmit}>

                    {/* Full Name */}

                    <input
                        type="text"
                        name="name"
                        placeholder="Full Name"
                        value={formData.name}
                        onChange={handleChange}
                        required
                    />

                    {/* Branch */}

                    <input
                        type="text"
                        name="branch"
                        placeholder="Branch (e.g. CSE)"
                        value={formData.branch}
                        onChange={handleChange}
                        required
                    />

                    {/* CGPA */}

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

                    {/* Email */}

                    <input
                        type="email"
                        name="email"
                        placeholder="Email Address"
                        value={formData.email}
                        onChange={handleChange}
                        required
                    />

                    {/* Send OTP Button */}

                    {!otpVerified && (

                        <button
                            type="button"
                            onClick={handleSendOtp}
                            disabled={sendingOtp || !formData.email.trim()}
                        >

                            {sendingOtp
                                ? "Sending OTP..."
                                : otpSent
                                    ? "Resend OTP"
                                    : "Send OTP"}

                        </button>

                    )}

                    {/* OTP Input */}

                    {otpSent && !otpVerified && (

                        <>

                            <input
                                type="text"
                                name="otp"
                                placeholder="Enter 6-digit OTP"
                                value={otp}
                                onChange={(e) => {
                                    setOtp(
                                        e.target.value
                                            .replace(/\D/g, "")
                                            .slice(0, 6)
                                    );
                                }}
                                maxLength="6"
                                inputMode="numeric"
                                autoComplete="one-time-code"
                                required
                            />

                            {/* Verify OTP Button */}

                            <button
                                type="button"
                                onClick={handleVerifyOtp}
                                disabled={verifyingOtp || otp.length !== 6}
                            >

                                {verifyingOtp
                                    ? "Verifying..."
                                    : "Verify OTP"}

                            </button>

                        </>

                    )}

                    {/* Verified Status */}

{otpVerified && (

    <div className="success-message">
        <span>✓ Email verified</span>
    </div>

)}

                    {/* Password with Eye Icon */}

                    <div className="password-container">

                        <input
                            type={
                                showPassword
                                    ? "text"
                                    : "password"
                            }
                            name="password"
                            placeholder="Password"
                            value={formData.password}
                            onChange={handleChange}
                            minLength="8"
                            required
                        />

                        <button
                            type="button"
                            className="password-toggle"
                            onClick={() =>
                                setShowPassword(!showPassword)
                            }
                            aria-label={
                                showPassword
                                    ? "Hide password"
                                    : "Show password"
                            }
                        >

                            {showPassword
                                ? <FaEyeSlash />
                                : <FaEye />}

                        </button>

                    </div>

                    {/* Register Button */}

                    <button
                        type="submit"
                        disabled={!otpVerified || registering}
                    >

                        {registering
                            ? "Registering..."
                            : "Register"}

                    </button>

                </form>

                {/* Login Link */}

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
