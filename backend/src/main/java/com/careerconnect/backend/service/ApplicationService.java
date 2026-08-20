package com.careerconnect.backend.service;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

import com.careerconnect.backend.model.Application;
import com.careerconnect.backend.model.Job;
import com.careerconnect.backend.model.Student;
import com.careerconnect.backend.repository.ApplicationRepository;
import com.careerconnect.backend.repository.JobRepository;
import com.careerconnect.backend.repository.StudentRepository;

@Service
public class ApplicationService {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private JobRepository jobRepository;

    public Application applyForJob(int studentId, int jobId) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() ->
                        new RuntimeException("Student not found"));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() ->
                        new RuntimeException("Job not found"));

        // Check if student already applied
        if (applicationRepository.existsByStudentAndJob(student, job)) {
            throw new RuntimeException(
                    "You have already applied for this job");
        }

        Application application = new Application();

        application.setStudent(student);
        application.setJob(job);
        application.setStatus("APPLIED");
        application.setAppliedDate(LocalDate.now());

        return applicationRepository.save(application);
    }
    public List<Application> getApplicationsByStudent(int studentId) {

    Student student = studentRepository.findById(studentId)
            .orElseThrow(() ->
                    new RuntimeException("Student not found"));

    return applicationRepository.findByStudent(student);
}
}
