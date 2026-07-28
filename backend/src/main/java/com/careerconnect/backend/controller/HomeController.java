package com.careerconnect.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.careerconnect.backend.model.Student;
import com.careerconnect.backend.service.StudentService;

@RestController
public class HomeController {

    @Autowired
    private StudentService studentService;

    @GetMapping("/")
    public String home() {
        return "Welcome to CareerConnect AI Backend";
    }

    @GetMapping("/student")
    public Student getStudent() {

        Student student = new Student(

                "Shiva Rohith",
                "CSE",
                8.8);

        return student;
    }

    @GetMapping("/students")
    public List<Student> getStudents() {
        return studentService.getStudents();
    }

    @GetMapping("/add")
    public String addStudent() {

        studentService.addStudent();

        return "Student Added Successfully";
    }
}