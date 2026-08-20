import axios from "axios";

const API_URL = "http://localhost:8080/applications";

export const applyForJob = async (studentId, jobId) => {
    const response = await axios.post(
        `${API_URL}/${studentId}/${jobId}`
    );

    return response.data;
};

export const getApplicationsByStudent = async (studentId) => {
    const response = await axios.get(
        `${API_URL}/student/${studentId}`
    );

    return response.data;
};