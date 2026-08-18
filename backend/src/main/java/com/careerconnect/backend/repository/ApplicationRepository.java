package com.careerconnect.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.careerconnect.backend.model.Application;

public interface ApplicationRepository extends JpaRepository<Application, Integer> {

}
