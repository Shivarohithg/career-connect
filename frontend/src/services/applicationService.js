import axios from "axios";

const API_URL = "http://localhost:8080/applications";

export const applyForJob = async (studentId, jobId) => {

    const response = await axios.post(
        `${API_URL}/${studentId}/${jobId}`
    );

    return response.data;
};