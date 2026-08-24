package com.careerconnect.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.careerconnect.backend.model.CareerAnalysis;
import com.careerconnect.backend.model.Student;

public interface CareerAnalysisRepository
        extends JpaRepository<CareerAnalysis, Integer> {

    Optional<CareerAnalysis> findByStudent(Student student);

}
