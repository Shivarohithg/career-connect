package com.careerconnect.backend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.careerconnect.backend.model.JobRole;
import com.careerconnect.backend.repository.JobRoleRepository;

@Component
public class JobRoleDataLoader implements CommandLineRunner {

    private final JobRoleRepository jobRoleRepository;

    public JobRoleDataLoader(JobRoleRepository jobRoleRepository) {
        this.jobRoleRepository = jobRoleRepository;
    }

    @Override
    public void run(String... args) {

        if (jobRoleRepository.count() > 0) {
            return;
        }

        jobRoleRepository.save(
            new JobRole(
                "Java Backend Developer",
                "Java, Spring Boot, SQL"
            )
        );

        jobRoleRepository.save(
            new JobRole(
                "Full Stack Developer",
                "JavaScript, React, HTML, CSS, Node.js"
            )
        );

        jobRoleRepository.save(
            new JobRole(
                "Frontend Developer",
                "HTML, CSS, JavaScript, React, Git"
            )
        );

        jobRoleRepository.save(
            new JobRole(
                "Python Developer",
                "Python, SQL, Git, REST APIs"
            )
        );

        jobRoleRepository.save(
            new JobRole(
                "Software Engineer",
                "Java, DSA, OOP, Git, SQL"
            )
        );

        jobRoleRepository.save(
            new JobRole(
                "Data Analyst",
                "Python, SQL, Excel, Statistics, Power BI"
            )
        );
    }
}
