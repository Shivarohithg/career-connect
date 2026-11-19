
package com.careerconnect.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "jobs")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String title;
    private String company;
    private String location;
    private double salary;

    // Job eligibility requirements
    private double minCgpa;

    private String eligibleBranches;

    private String requiredSkills;

    public Job() {
    }

    public Job(
            String title,
            String company,
            String location,
            double salary,
            double minCgpa,
            String eligibleBranches,
            String requiredSkills) {

        this.title = title;
        this.company = company;
        this.location = location;
        this.salary = salary;
        this.minCgpa = minCgpa;
        this.eligibleBranches = eligibleBranches;
        this.requiredSkills = requiredSkills;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCompany() {
        return company;
    }

    public String getLocation() {
        return location;
    }

    public double getSalary() {
        return salary;
    }

    public double getMinCgpa() {
        return minCgpa;
    }

    public String getEligibleBranches() {
        return eligibleBranches;
    }

    public String getRequiredSkills() {
        return requiredSkills;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public void setMinCgpa(double minCgpa) {
        this.minCgpa = minCgpa;
    }

    public void setEligibleBranches(String eligibleBranches) {
        this.eligibleBranches = eligibleBranches;
    }

    public void setRequiredSkills(String requiredSkills) {
        this.requiredSkills = requiredSkills;
    }
}

