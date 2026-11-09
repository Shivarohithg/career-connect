
import axios from "axios";

const API_URL = "http://localhost:8080/auth";

// ================= REGISTER STUDENT =================
export const registerStudent = async (studentData) => {
    const response = await axios.post(
        `${API_URL}/register`,
        studentData
    );

    return response.data;
};

// ================= SEND OTP =================
export const sendOtp = async (email) => {
    const response = await axios.post(
        `${API_URL}/send-otp`,
        null,
        {
            params: {
                email: email
            }
        }
    );

    return response.data;
};

// ================= VERIFY OTP =================
export const verifyOtp = async (email, otp) => {
    const response = await axios.post(
        `${API_URL}/verify-otp`,
        null,
        {
            params: {
                email: email,
                otp: otp
            }
        }
    );

    return response.data;
};

// ================= LOGIN STUDENT =================
export const loginStudent = async (email, password) => {
    const response = await axios.post(
        `${API_URL}/login`,
        null,
        {
            params: {
                email: email,
                password: password
            }
        }
    );

    return response.data;
};

// ================= GET LOGGED-IN STUDENT =================
export const getLoggedInStudent = () => {
    const student = localStorage.getItem("student");

    if (!student) {
        return null;
    }

    return JSON.parse(student);
};

// ================= LOGOUT STUDENT =================
export const logoutStudent = () => {
    localStorage.removeItem("student");
};

