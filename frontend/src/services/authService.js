import axios from "axios";

const API_URL = "http://localhost:8080/auth";

export const registerStudent = async (studentData) => {
    const response = await axios.post(`${API_URL}/register`, studentData);
    return response.data;
};

export const loginStudent = async (email, password) => {
    const response = await axios.post(`${API_URL}/login`, null, {
        params: {
            email: email,
            password: password
        }
    });

    return response.data;
}
export const getLoggedInStudent = () => {
    const student = localStorage.getItem("student");

    if (!student) {
        return null;
    }

    return JSON.parse(student);
};

export const logoutStudent = () => {
    localStorage.removeItem("student");
};
