package com.careerconnect.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.careerconnect.backend.model.Student;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "Welcome to CareerConnect AI Backend";
    }

    @GetMapping("/student")
    public Student getStudent() {

        Student student = new Student(
                1,
                "Shiva Rohith",
                "CSE",
                8.8);

        return student;
    }

    @GetMapping("/students")
    public List<Student> getStudents() {

        Student s1 = new Student(1, "Shiva Rohith", "CSE", 8.8);
        Student s2 = new Student(2, "Rahul", "ECE", 8.5);
        Student s3 = new Student(3, "Priya", "IT", 9.1);

        return List.of(s1, s2, s3);
    }
}