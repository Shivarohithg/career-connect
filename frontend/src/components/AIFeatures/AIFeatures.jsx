import styles from "./AIFeatures.module.css";

function AIFeatures() {
  const aiFeatures = [
    {
      icon: "🤖",
      title: "AI Resume Analyzer",
      description:
        "Upload your resume and receive AI-powered suggestions to improve its quality.",
    },
    {
      icon: "🎯",
      title: "AI Job Recommendation",
      description:
        "Get personalized job recommendations based on your skills and profile.",
    },
    {
      icon: "💬",
      title: "AI Interview Coach",
      description:
        "Practice interview questions generated specifically for your desired role.",
    },
    {
      icon: "📈",
      title: "Career Insights",
      description:
        "Receive personalized learning recommendations to strengthen your career path.",
    },
  ];

  return (
    <section className={styles.aiSection}>
      <div className={styles.container}>
        <h2>AI That Works for Your Career</h2>

        <p className={styles.subtitle}>
          Experience the future of placement preparation with Artificial
          Intelligence.
        </p>

        <div className={styles.grid}>
          {aiFeatures.map((feature, index) => (
            <div key={index} className={styles.card}>
              <div className={styles.icon}>{feature.icon}</div>

              <h3>{feature.title}</h3>

              <p>{feature.description}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}

export default AIFeatures;