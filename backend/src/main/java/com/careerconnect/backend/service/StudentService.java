package com.careerconnect.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.careerconnect.backend.model.Student;

@Service
public class StudentService {

    public List<Student> getStudents() {

        Student s1 = new Student(1, "Shiva Rohith", "CSE", 8.8);
        Student s2 = new Student(2, "Rahul", "ECE", 8.5);
        Student s3 = new Student(3, "Priya", "IT", 9.1);

        return List.of(s1, s2, s3);
    }
}
