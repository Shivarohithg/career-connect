
import { useNavigate } from "react-router-dom";
import styles from "./CTA.module.css";

function CTA() {
  const navigate = useNavigate();

  const handleGetStarted = () => {
    const student = localStorage.getItem("student");

    if (student) {
      navigate("/dashboard");
    } else {
      navigate("/register");
    }
  };

  const handleLearnMore = () => {
    const aiSection = document.getElementById("ai-features");

    if (aiSection) {
      aiSection.scrollIntoView({
        behavior: "smooth",
        block: "start",
      });
    }
  };

  return (
    <section className={styles.cta}>
      <div className={styles.container}>
        <h2>Start Your Placement Journey Today</h2>

        <p>
          Join CareerConnect AI to build ATS-friendly resumes,
          prepare for interviews, and connect with top recruiters.
        </p>

        <div className={styles.buttons}>
          <button
            className={styles.primaryBtn}
            onClick={handleGetStarted}
          >
            Get Started
          </button>

          <button
            className={styles.secondaryBtn}
            onClick={handleLearnMore}
          >
            Learn More
          </button>
        </div>
      </div>
    </section>
  );
}

export default CTA;
