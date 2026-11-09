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
import com.careerconnect.backend.service.EmailVerificationService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private StudentService studentService;
    @Autowired
private StudentProfileService studentProfileService;
    @Autowired
private EmailVerificationService emailVerificationService;


@PostMapping("/register")
public ResponseEntity<?> register(@RequestBody Student student) {

    try {

        if (!emailVerificationService.isEmailVerified(
                student.getEmail())) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Please verify your email before registering");
        }

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

  
@PostMapping("/send-otp")
public ResponseEntity<?> sendOtp(
        @RequestParam String email) {

    try {

        if (studentService.getStudents().stream()
                .anyMatch(student ->
                        student.getEmail().equalsIgnoreCase(email))) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Email already registered");
        }

        String result =
                emailVerificationService.sendOtp(email);

        return ResponseEntity.ok(result);

    } catch (RuntimeException e) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }
}
 
  
@PostMapping("/verify-otp")
public ResponseEntity<?> verifyOtp(
        @RequestParam String email,
        @RequestParam String otp) {

    try {

        String result =
                emailVerificationService.verifyOtp(email, otp);

        return ResponseEntity.ok(result);

    } catch (RuntimeException e) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }
}


}
