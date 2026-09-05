package com.careerconnect.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.careerconnect.backend.model.Student;
import com.careerconnect.backend.service.StudentService;
import com.careerconnect.backend.dto.StudentLoginDTO;
import com.careerconnect.backend.service.StudentProfileService;
import com.careerconnect.backend.service.StudentProfileService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private StudentService studentService;
    @Autowired
private StudentProfileService studentProfileService;

@PostMapping("/register")
public ResponseEntity<?> register(@RequestBody Student student) {
    try {
        Student registeredStudent =
                studentService.registerStudent(student);

        studentProfileService.createProfile(
                registeredStudent.getId(),
                "",
                "",
                "",
                "",
                "",
                ""
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(registeredStudent);

    } catch (RuntimeException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(e.getMessage());
    }
}

  @PostMapping("/login")
public ResponseEntity<?> login(
        @RequestParam String email,
        @RequestParam String password) {

    try {
        Student student = studentService.loginStudent(email, password);

        StudentLoginDTO response = new StudentLoginDTO(
                student.getId(),
                student.getName(),
                student.getBranch(),
                student.getCgpa(),
                student.getEmail()
        );

        return ResponseEntity.ok(response);

    } catch (RuntimeException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(e.getMessage());
    }
}
}
