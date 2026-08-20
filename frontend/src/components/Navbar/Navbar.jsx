import { Link } from "react-router-dom";
import styles from "./Navbar.module.css";

function Navbar() {
    return (
        <nav className={styles.navbar}>

            <h2 className={styles.logo}>
                Career<span>Connect</span>
            </h2>

            <ul className={styles.navLinks}>

                <li>
                    <Link to="/">Home</Link>
                </li>

                <li>
                    <Link to="/jobs">Jobs</Link>
                </li>

                <li>
                    <Link to="/my-applications">
                        My Applications
                    </Link>
                </li>

                <li>
                    <Link to="/login">Login</Link>
                </li>

                <li>
                    <Link to="/register">Register</Link>
                </li>

            </ul>

        </nav>
    );
}

export default Navbar;