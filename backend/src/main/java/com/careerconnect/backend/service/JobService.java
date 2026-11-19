
package com.careerconnect.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.careerconnect.backend.model.Job;
import com.careerconnect.backend.repository.JobRepository;

@Service
public class JobService {

    @Autowired
    private JobRepository jobRepository;

    // Add a new job
    public Job addJob(Job job) {
        return jobRepository.save(job);
    }

    // Get all jobs
    public List<Job> getJobs() {
        return jobRepository.findAll();
    }

    // Get job by ID
    public Job getJobById(int id) {
        return jobRepository.findById(id).orElse(null);
    }

    // Update an existing job
    public String updateJob(int id, Job updatedJob) {

        Job job = jobRepository.findById(id).orElse(null);

        if (job == null) {
            return "Job Not Found";
        }

        job.setTitle(updatedJob.getTitle());
        job.setCompany(updatedJob.getCompany());
        job.setLocation(updatedJob.getLocation());
        job.setSalary(updatedJob.getSalary());

        // Update eligibility requirements
        job.setMinCgpa(updatedJob.getMinCgpa());
        job.setEligibleBranches(updatedJob.getEligibleBranches());
        job.setRequiredSkills(updatedJob.getRequiredSkills());

        jobRepository.save(job);

        return "Job Updated Successfully";
    }

    // Delete a job
    public String deleteJob(int id) {

        if (!jobRepository.existsById(id)) {
            return "Job Not Found";
        }

        jobRepository.deleteById(id);

        return "Job Deleted Successfully";
    }
}
