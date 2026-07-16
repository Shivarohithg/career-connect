import styles from "./Hero.module.css";
import heroImage from "../../assets/images/hero.svg";

function Hero() {
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
                    <button className={styles.primary}>
                        Get Started
                    </button>

                    <button className={styles.secondary}>
                        Browse Jobs
                    </button>
                </div>

            </div>

            <div className={styles.right}>
                <img src={heroImage} alt="Career Illustration" />
            </div>

        </section>
    );
}

export default Hero;