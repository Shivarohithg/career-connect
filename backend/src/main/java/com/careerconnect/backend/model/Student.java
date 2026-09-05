package com.careerconnect.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank(message = "Name cannot be empty")
    private String name;

    @NotBlank(message = "Branch cannot be empty")
    private String branch;

    @Min(value = 0, message = "CGPA cannot be negative")
    @Max(value = 10, message = "CGPA cannot exceed 10")
    private double cgpa;

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Enter a valid email")
    private String email;

    @NotBlank(message = "Password cannot be empty")
    private String password;

    public Student() {
    }

    public Student(String name, String branch, double cgpa,
                   String email, String password) {
        this.name = name;
        this.branch = branch;
        this.cgpa = cgpa;
        this.email = email;
        this.password = password;
    }
    public Student(String name, String branch, double cgpa) {
    this.name = name;
    this.branch = branch;
    this.cgpa = cgpa;
}

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBranch() {
        return branch;
    }

    public double getCgpa() {
        return cgpa;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public void setCgpa(double cgpa) {
        this.cgpa = cgpa;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
