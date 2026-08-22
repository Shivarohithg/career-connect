package com.careerconnect.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.careerconnect.backend.model.Student;
import com.careerconnect.backend.model.StudentProfile;

public interface StudentProfileRepository
        extends JpaRepository<StudentProfile, Integer> {

    Optional<StudentProfile> findByStudent(Student student);
}