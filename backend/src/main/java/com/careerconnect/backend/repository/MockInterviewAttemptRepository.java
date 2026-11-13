package com.careerconnect.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.careerconnect.backend.model.MockInterviewAttempt;

@Repository
public interface MockInterviewAttemptRepository
        extends JpaRepository<MockInterviewAttempt, Long> {

    List<MockInterviewAttempt>
    findByStudent_IdOrderByCreatedAtDesc(int studentId);

    Optional<MockInterviewAttempt>
    findByIdAndStudent_Id(Long id, int studentId);
}
