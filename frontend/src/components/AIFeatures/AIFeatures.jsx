import styles from "./AIFeatures.module.css";
import {
  FaRobot,
  FaBullseye,
  FaComments,
  FaChartLine,
} from "react-icons/fa";

function AIFeatures() {
const aiFeatures = [
  {
    icon: <FaRobot />,
    title: "AI Resume Analyzer",
    description: "...",
  },
  {
    icon: <FaBullseye />,
    title: "AI Job Recommendations",
    description: "...",
  },
  {
    icon: <FaComments />,
    title: "AI Interview Coach",
    description: "...",
  },
  {
    icon: <FaChartLine />,
    title: "Career Insights",
    description: "...",
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