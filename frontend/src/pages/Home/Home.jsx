import Hero from "../../components/Hero/Hero";
import Features from "../../components/Features/Features";
import Companies from "../../components/Companies/Companies";
import AIFeatures from "../../components/AIFeatures/AIFeatures";
import Statistics from "../../components/Statistics/Statistics";
import CTA from "../../components/CTA/CTA";

function Home() {
  return (
    <>
      <Hero />
      <Features />
      <Companies />
      <AIFeatures />
      <Statistics />
      <CTA />
    </>
  );
}

export default Home;