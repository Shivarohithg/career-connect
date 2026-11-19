
package com.careerconnect.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.careerconnect.backend.dto.LearningRoadmapDTO;
import com.careerconnect.backend.service.LearningRoadmapService;

@RestController
@RequestMapping("/learning-roadmap")
public class LearningRoadmapController {

    @Autowired
    private LearningRoadmapService learningRoadmapService;

    @GetMapping("/{studentId}/{jobId}")
    public ResponseEntity<LearningRoadmapDTO> generateRoadmap(
            @PathVariable int studentId,
            @PathVariable int jobId) {

        LearningRoadmapDTO roadmap =
                learningRoadmapService.generateRoadmap(studentId, jobId);

        return ResponseEntity.ok(roadmap);
    }
}

