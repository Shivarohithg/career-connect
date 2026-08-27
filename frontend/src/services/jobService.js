import axios from "axios";

const API_URL = "http://localhost:8080";

export const getAllJobs = async () => {
    const response = await axios.get(`${API_URL}/jobs`);
    return response.data;
};

export const getJobById = async (id) => {
    const response = await axios.get(`${API_URL}/jobs/${id}`);
    return response.data;
};