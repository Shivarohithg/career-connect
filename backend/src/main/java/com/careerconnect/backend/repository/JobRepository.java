package com.careerconnect.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.careerconnect.backend.model.Job;

public interface JobRepository extends JpaRepository<Job, Integer> {

}
