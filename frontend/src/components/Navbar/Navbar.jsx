import { useNavigate } from "react-router-dom";
import styles from "./Navbar.module.css";


function Navbar() {

    const navigate = useNavigate();

    const student = JSON.parse(
        localStorage.getItem("student")
    );

    const handleLogout = () => {

        localStorage.removeItem("student");

        navigate("/login");
    };

    return (
        <nav className={styles.navbar}>

            <h2 className={styles.logo}>
                Career<span>Connect</span>
            </h2>

            <ul className={styles.navLinks}>

                <li>
                    <a href="/">Home</a>
                </li>

                {student && (
                    <>
                        <li>
                            <a href="/dashboard">
                                Dashboard
                            </a>
                        </li>

                        <li>
                            <a href="/jobs">
                                Jobs
                            </a>
                        </li>

                        <li>
                            <a href="/career-analysis">
                                Career Analysis
                            </a>
                        </li>

                        <li>
                            <a href="/my-applications">
                                My Applications
                            </a>
                        </li>

                        <li>
                            <a href="/profile">
                                My Profile
                            </a>
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
                            <a href="/login">
                                Login
                            </a>
                        </li>

                        <li>
                            <a href="/register">
                                Register
                            </a>
                        </li>
                    </>
                )}

            </ul>

        </nav>
    );
}

export default Navbar;