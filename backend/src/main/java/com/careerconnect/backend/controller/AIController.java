package com.careerconnect.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.careerconnect.backend.service.OpenAIService;

@RestController
public class AIController {

    @Autowired
    private OpenAIService openAIService;

    @GetMapping("/ai/test")
    public String testAI() {

        String prompt =
                "You are an AI career advisor. "
                + "Give a short career recommendation for "
                + "a CSE student who knows Java, SQL, HTML, CSS "
                + "and basic data structures.";

        return openAIService.generateCareerAdvice(prompt);
    }
}