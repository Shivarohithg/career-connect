import styles from "./Companies.module.css";

function Companies() {
  const companies = [
    "Google",
    "Microsoft",
    "Amazon",
    "Infosys",
    "TCS",
    "Accenture",
  ];

  return (
    <section className={styles.companies}>
      <div className={styles.container}>
        <h2>Trusted by Top Recruiters</h2>

        <p className={styles.subtitle}>
          Connecting talented students with opportunities from leading
          companies.
        </p>

        <div className={styles.grid}>
          {companies.map((company, index) => (
            <div key={index} className={styles.card}>
              {company}
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}

export default Companies;