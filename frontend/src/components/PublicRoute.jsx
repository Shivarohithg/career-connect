import { Navigate } from "react-router-dom";

function PublicRoute({ children }) {

    const student = localStorage.getItem("student");

    if (student) {
        return <Navigate to="/dashboard" replace />;
    }

    return children;
}

export default PublicRoute;