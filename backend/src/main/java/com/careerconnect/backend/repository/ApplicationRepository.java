package com.careerconnect.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.careerconnect.backend.model.Application;
import com.careerconnect.backend.model.Job;
import com.careerconnect.backend.model.Student;

public interface ApplicationRepository extends JpaRepository<Application, Integer> {

    boolean existsByStudentAndJob(Student student, Job job);
}