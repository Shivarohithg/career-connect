import styles from "./Footer.module.css";
import {
  FaGithub,
  FaLinkedin,
  FaTwitter,
  FaEnvelope,
  FaPhone,
} from "react-icons/fa";

function Footer() {
  return (
    <footer className={styles.footer}>
      <div className={styles.container}>
        
        <div className={styles.section}>
          <h2>CareerConnect AI</h2>

          <p>
            Empowering students with AI-powered resume building,
            interview preparation, and placement opportunities.
          </p>
        </div>

        <div className={styles.section}>
          <h3>Quick Links</h3>

          <ul>
            <li>Home</li>
            <li>Features</li>
            <li>Login</li>
            <li>Register</li>
          </ul>
        </div>

        <div className={styles.section}>
          <h3>Contact</h3>

          <p>
            <FaEnvelope /> support@careerconnect.ai
          </p>

          <p>
            <FaPhone /> +91 XXXXX XXXXX
          </p>
        </div>

        <div className={styles.section}>
          <h3>Follow Us</h3>

          <div className={styles.icons}>
            <FaGithub />
            <FaLinkedin />
            <FaTwitter />
          </div>
        </div>

      </div>

      <hr />

      <p className={styles.copy}>
        © 2026 CareerConnect AI. All Rights Reserved.
      </p>
    </footer>
  );
}

export default Footer;