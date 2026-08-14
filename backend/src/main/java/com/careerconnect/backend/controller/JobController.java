package com.careerconnect.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;


import com.careerconnect.backend.model.Job;
import com.careerconnect.backend.service.JobService;

@RestController
public class JobController {

    @Autowired
    private JobService jobService;

    @PostMapping("/jobs")
    public ResponseEntity<Job> addJob(@RequestBody Job job) {

        Job savedJob = jobService.addJob(job);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedJob);
    }

    @GetMapping("/jobs")
    public List<Job> getJobs() {

        return jobService.getJobs();

    }
    @GetMapping("/jobs/{id}")
public Job getJobById(@PathVariable int id) {

    return jobService.getJobById(id);

}
@DeleteMapping("/jobs/{id}")
public String deleteJob(@PathVariable int id) {

    return jobService.deleteJob(id);

}

@PutMapping("/jobs/{id}")
public String updateJob(
        @PathVariable int id,
        @RequestBody Job job) {

    return jobService.updateJob(id, job);
}
}
