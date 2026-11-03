
package com.careerconnect.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.careerconnect.backend.service.CareerRoadmapService;

@RestController
public class CareerRoadmapController {

    @Autowired
    private CareerRoadmapService careerRoadmapService;

    @GetMapping("/ai/career-roadmap/{studentId}")
    public ResponseEntity<?> getCareerRoadmap(
            @PathVariable int studentId) {

        try {

            String roadmap =
                    careerRoadmapService.generateRoadmap(
                            studentId
                    );

            return ResponseEntity.ok(roadmap);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }
}

