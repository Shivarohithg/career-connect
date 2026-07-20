import styles from "./CTA.module.css";

function CTA() {
  return (
    <section className={styles.cta}>
      <div className={styles.container}>

        <h2>Start Your Placement Journey Today</h2>

        <p>
          Join thousands of students using CareerConnect AI to build
          ATS-friendly resumes, prepare for interviews, and connect with
          top recruiters.
        </p>

        <div className={styles.buttons}>
          <button className={styles.primaryBtn}>
            Get Started
          </button>

          <button className={styles.secondaryBtn}>
            Learn More
          </button>
        </div>

      </div>
    </section>
  );
}

export default CTA;