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

    public Job addJob(Job job) {

        return jobRepository.save(job);

    }

    public List<Job> getJobs() {

        return jobRepository.findAll();

    }

    public Job getJobById(int id) {

        return jobRepository.findById(id).orElse(null);

    }

    public String deleteJob(int id) {

        if (!jobRepository.existsById(id)) {
            return "Job Not Found";
        }

        jobRepository.deleteById(id);

        return "Job Deleted Successfully";
    }
}