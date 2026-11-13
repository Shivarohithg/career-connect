import axios from "axios";

const API_URL = "http://localhost:8080/mock-interview";

export const generateInterview = async (studentId, role) => {
    const response = await axios.post(
        `${API_URL}/${studentId}/generate`,
        { role }
    );

    return response.data;
};

export const evaluateInterview = async (studentId, data) => {
    const response = await axios.post(
        `${API_URL}/${studentId}/evaluate`,
        data
    );

    return response.data;
};

// Get all interview attempts
export const getInterviewHistory = async (studentId) => {
    const response = await axios.get(
        `${API_URL}/${studentId}/history`
    );

    return response.data;
};

// Get details of one interview attempt
export const getInterviewHistoryDetails = async (studentId, attemptId) => {
    const response = await axios.get(
        `${API_URL}/${studentId}/history/${attemptId}`
    );

    return response.data;
};
