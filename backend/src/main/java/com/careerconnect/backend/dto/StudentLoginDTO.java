package com.careerconnect.backend.dto;

public class StudentLoginDTO {

    private int id;
    private String name;
    private String branch;
    private double cgpa;
    private String email;

    public StudentLoginDTO() {
    }

    public StudentLoginDTO(int id, String name, String branch,
                           double cgpa, String email) {
        this.id = id;
        this.name = name;
        this.branch = branch;
        this.cgpa = cgpa;
        this.email = email;
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
}
