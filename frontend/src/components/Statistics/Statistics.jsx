import {
  FaUserGraduate,
  FaBuilding,
  FaFileAlt,
  FaRobot,
} from "react-icons/fa";

import styles from "./Statistics.module.css";


function Statistics() {

const stats = [
  {
    icon: <FaUserGraduate />,
    number: "10,000+",
    label: "Students Assisted",
  },
  {
    icon: <FaBuilding />,
    number: "500+",
    label: "Recruiters",
  },
  {
    icon: <FaFileAlt />,
    number: "25,000+",
    label: "Resumes Analyzed",
  },
  {
    icon: <FaRobot />,
    number: "95%",
    label: "AI Accuracy",
  },
];

  return (
    <section className={styles.statistics}>
      <div className={styles.container}>

        <h2>CareerConnect AI in Numbers</h2>

        <p className={styles.subtitle}>
          Helping students prepare smarter, connect faster,
          and achieve better placement outcomes.
        </p>

        <div className={styles.grid}>

          {stats.map((stat, index) => (

            <div key={index} className={styles.card}>

              <div className={styles.icon}>
                {stat.icon}
              </div>

              <h3>{stat.number}</h3>

              <p>{stat.label}</p>

            </div>

          ))}

        </div>

      </div>
    </section>
  );
}

export default Statistics;