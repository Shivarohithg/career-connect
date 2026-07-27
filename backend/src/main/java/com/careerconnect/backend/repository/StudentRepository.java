package com.careerconnect.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.careerconnect.backend.model.Student;

public interface StudentRepository extends JpaRepository<Student, Integer> {
}


