package com.careerconnect.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.careerconnect.backend.model.JobRole;

public interface JobRoleRepository extends JpaRepository<JobRole, Integer> {

}
