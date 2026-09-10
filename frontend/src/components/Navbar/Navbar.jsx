import { Link, useNavigate } from "react-router-dom";
import styles from "./Navbar.module.css";

function Navbar() {

    const navigate = useNavigate();

    const storedStudent = localStorage.getItem("student");

    let student = null;

    try {
        student = storedStudent
            ? JSON.parse(storedStudent)
            : null;
    } catch (error) {
        console.error("Invalid student data:", error);
        localStorage.removeItem("student");
    }

    const handleLogout = () => {

        localStorage.removeItem("student");

        navigate("/login");
    };

    return (
        <nav className={styles.navbar}>

            <h2 className={styles.logo}>
                <Link to="/">
                    Career<span>Connect</span>
                </Link>
            </h2>

            <ul className={styles.navLinks}>

                <li>
                    <Link to="/">Home</Link>
                </li>
{student && (
    <>
        <li>
            <Link to="/dashboard">Dashboard</Link>
        </li>

        <li>
            <Link to="/jobs">Jobs</Link>
        </li>

        <li>
            <Link to="/career-analysis">
                Career Analysis
            </Link>
        </li>

        <li>
            <Link to="/my-applications">
                My Applications
            </Link>
        </li>

        <li>
            <Link to="/profile">
                My Profile
            </Link>
        </li>

        <li className={styles.welcome}>
            Welcome, {student.name}
        </li>

        <li>
            <button
                className={styles.logoutButton}
                onClick={handleLogout}
            >
                Logout
            </button>
        </li>
    </>
)}
                    

                {!student && (
                    <>
                        <li>
                            <Link to="/login">
                                Login
                            </Link>
                        </li>

                        <li>
                            <Link to="/register">
                                Register
                            </Link>
                        </li>
                    </>
                )}

            </ul>

        </nav>
    );
}

export default Navbar;