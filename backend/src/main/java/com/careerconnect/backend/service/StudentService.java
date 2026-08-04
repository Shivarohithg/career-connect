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

    public Student addStudent() {

        Student student = new Student(
                "Shiva Rohith",
                "CSE",
                8.8);

        return studentRepository.save(student);
    }

    public List<Student> getStudents() {
        return studentRepository.findAll();
    }

    public Student getStudentById(int id) {

        if (!studentRepository.existsById(id)) {
            throw new RuntimeException("Student Not Found");
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

}
