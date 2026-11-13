package com.careerconnect.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import com.careerconnect.backend.repository.StudentRepository;
import com.careerconnect.backend.exception.StudentNotFoundException;

import java.util.List;

import org.springframework.stereotype.Service;

import com.careerconnect.backend.model.Student;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
private PasswordEncoder passwordEncoder;

    public List<Student> getStudents() {
        return studentRepository.findAll();
    }

    public Student getStudentById(int id) {

        if (!studentRepository.existsById(id)) {
            throw new StudentNotFoundException("Student Not Found");
        }

        return studentRepository.findById(id).get();
    }

    public String updateStudent(int id) {

        Student student = studentRepository.findById(id).orElse(null);

        if (student == null) {
            return "Student Not Found";
        }

        student.setCgpa(9.2);

        studentRepository.save(student);

        return "Student Updated Successfully";
    }

    public String deleteStudent(int id) {

        if (!studentRepository.existsById(id)) {
            return "Student Not Found";
        }

        studentRepository.deleteById(id);

        return "Student Deleted Successfully";
    }

    public Student addStudent(Student student) {
        return studentRepository.save(student);
    }

// Register new student
public Student registerStudent(Student student) {

    if (studentRepository.existsByEmail(student.getEmail())) {
        throw new RuntimeException("Email already registered");
    }

    // Hash password before saving
    student.setPassword(
            passwordEncoder.encode(student.getPassword())
    );

    return studentRepository.save(student);
}

// Login student
public Student loginStudent(String email, String password) {

    Student student = studentRepository.findByEmail(email)
            .orElseThrow(() ->
                    new RuntimeException("Invalid email or password"));

    String storedPassword = student.getPassword();

    // Check whether password is already BCrypt hashed
    boolean isBCrypt = storedPassword.startsWith("$2a$")
            || storedPassword.startsWith("$2b$")
            || storedPassword.startsWith("$2y$");

    if (isBCrypt) {

        // Verify hashed password
        if (!passwordEncoder.matches(password, storedPassword)) {
            throw new RuntimeException("Invalid email or password");
        }

    } else {

        // Support existing plain-text accounts temporarily
        if (!storedPassword.equals(password)) {
            throw new RuntimeException("Invalid email or password");
        }

        // Upgrade old password to BCrypt after successful login
        student.setPassword(passwordEncoder.encode(password));

        studentRepository.save(student);
    }

    return student;
}
    }
