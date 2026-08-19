import axios from "axios";

const API_URL = "http://localhost:8080/jobs";

export const getAllJobs = async () => {
    const response = await axios.get(API_URL);
    return response.data;
};

export const getJobById = async (id) => {
    const response = await axios.get(`${API_URL}/${id}`);
    return response.data;
};

export const applyForJob = async (studentId, jobId) => {
    const response = await axios.post(
        `http://localhost:8080/applications/${studentId}/${jobId}`
    );

    return response.data;
};