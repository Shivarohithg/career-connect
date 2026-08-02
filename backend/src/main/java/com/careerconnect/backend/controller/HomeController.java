package com.careerconnect.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.careerconnect.backend.model.Student;
import com.careerconnect.backend.service.StudentService;
import com.careerconnect.backend.dto.StudentDTO;

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

@PostMapping("/add")
public ResponseEntity<StudentDTO> addStudent(@RequestBody Student student){

    Student savedStudent = studentService.addStudent(student);
    StudentDTO studentDTO = new StudentDTO(
        savedStudent.getId(),
        savedStudent.getName(),
        savedStudent.getBranch(),
        savedStudent.getCgpa()
);

    return ResponseEntity.status(HttpStatus.CREATED)
            .body(studentDTO);

}

    @GetMapping("/students/{id}")
    public Student getStudent(@PathVariable int id) {

        return studentService.getStudentById(id);

    }

    @GetMapping("/update/{id}")
    public String updateStudent(@PathVariable int id) {

        return studentService.updateStudent(id);

    }

    @GetMapping("/delete/{id}")
    public String deleteStudent(@PathVariable int id) {

        return studentService.deleteStudent(id);

    }

}