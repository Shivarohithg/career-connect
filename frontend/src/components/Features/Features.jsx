import styles from "./Features.module.css";

function Features() {
  const Features = [
    {
      icon: "📄",
      title: "Resume Management",
      description: "Upload and manage your resume in one place.",
    },
    {
      icon: "🎯",
      title: "Smart Job Matching",
      description: "Find jobs based on your skills and interests.",
    },
    {
      icon: "📈",
      title: "Application Tracking",
      description: "Track every job application with ease.",
    },
    {
      icon: "🏢",
      title: "Top Recruiters",
      description: "Connect with verified companies and recruiters.",
    },
  ];

  return (
    <section className={styles.features}>
      <h2>Why Choose CareerConnect AI Platform?</h2>

      <div className={styles.cards}>
        {Features.map((feature, index) => (
          <div className={styles.card} key={index}>
            <div className={styles.icon}>{feature.icon}</div>

            <h3>{feature.title}</h3>

            <p>{feature.description}</p>
          </div>
        ))}
      </div>

      

      <p className={styles.subtitle}>
        Experience AI-powered placement preparation, resume analysis, interview
        coaching, and smart job recommendations—all in one platform.
      </p>
    </section>
  );
}

export default Features;
