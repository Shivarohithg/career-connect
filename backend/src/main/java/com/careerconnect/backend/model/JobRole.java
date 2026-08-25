package com.careerconnect.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "job_roles")
public class JobRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String roleName;

    private String requiredSkills;

    public JobRole() {

    }

    public JobRole(String roleName, String requiredSkills) {
        this.roleName = roleName;
        this.requiredSkills = requiredSkills;
    }

    public int getId() {
        return id;
    }

    public String getRoleName() {
        return roleName;
    }

    public String getRequiredSkills() {
        return requiredSkills;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public void setRequiredSkills(String requiredSkills) {
        this.requiredSkills = requiredSkills;
    }
}