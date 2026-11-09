
import { useNavigate } from "react-router-dom";
import styles from "./Hero.module.css";
import heroImage from "../../assets/images/hero.svg";

function Hero() {
    const navigate = useNavigate();

    const handleGetStarted = () => {

        // Check whether student is already logged in
        const student = localStorage.getItem("student");

        if (student) {

            // Existing student
            navigate("/dashboard");

        } else {

            // New student
            navigate("/register");

        }
    };

    return (
        <section className={styles.hero}>

            <div className={styles.left}>

                <h1>
                    Find Your Dream Career
                    <span> with Confidence.</span>
                </h1>

                <p>
                    CareerConnect helps students connect with recruiters,
                    discover placement opportunities, and manage
                    applications from one platform.
                </p>

                <div className={styles.buttons}>

                    {/* Get Started */}
                    <button
                        className={styles.primary}
                        onClick={handleGetStarted}
                    >
                        Get Started
                    </button>

                    {/* Browse Jobs */}
                    <button
                        className={styles.secondary}
                        onClick={() => navigate("/jobs")}
                    >
                        Browse Jobs
                    </button>

                </div>

            </div>

            <div className={styles.right}>

                <img
                    src={heroImage}
                    alt="Career Illustration"
                />

            </div>

        </section>
    );
}

export default Hero;


