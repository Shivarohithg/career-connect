
package com.careerconnect.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.careerconnect.backend.dto.EligibilityDTO;
import com.careerconnect.backend.service.EligibilityService;

@RestController
@RequestMapping("/eligibility")
public class EligibilityController {

    @Autowired
    private EligibilityService eligibilityService;

    @GetMapping("/{studentId}/{jobId}")
    public ResponseEntity<EligibilityDTO> checkEligibility(
            @PathVariable int studentId,
            @PathVariable int jobId) {

        EligibilityDTO result =
                eligibilityService.checkEligibility(studentId, jobId);

        return ResponseEntity.ok(result);
    }
}

