package com.careerconnect.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import com.careerconnect.backend.repository.StudentRepository;

import java.util.List;

import org.springframework.stereotype.Service;

import com.careerconnect.backend.model.Student;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    public void addStudent() {

        Student student = new Student(
                "Shiva Rohith",
                "CSE",
                8.8);

        studentRepository.save(student);
    }

    public List<Student> getStudents() {
        return studentRepository.findAll();
    }
}
