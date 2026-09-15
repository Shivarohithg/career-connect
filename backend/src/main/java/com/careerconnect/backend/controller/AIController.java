package com.careerconnect.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AIController {

    @GetMapping("/ai/test")
    public String testAI() {

        return "CareerConnect AI module is running successfully.";
    }
}