
package com.careerconnect.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.careerconnect.backend.dto.MockInterviewDTO;
import com.careerconnect.backend.service.MockInterviewService;
import java.util.List;
import java.util.stream.Collectors;

import com.careerconnect.backend.model.MockInterviewAttempt;
import com.careerconnect.backend.repository.MockInterviewAttemptRepository;

@RestController
@RequestMapping("/mock-interview")
@CrossOrigin(origins = "http://localhost:5173")
public class MockInterviewController {

    @Autowired
    private MockInterviewService mockInterviewService;

    @Autowired
private MockInterviewAttemptRepository mockInterviewAttemptRepository;

    /*
     * GENERATE INTERVIEW
     */

    @PostMapping("/{studentId}/generate")
    public ResponseEntity<MockInterviewDTO.InterviewResponse>
    generateInterview(
            @PathVariable int studentId,
            @RequestBody MockInterviewDTO.GenerateRequest request) {

        return ResponseEntity.ok(
                mockInterviewService.generateInterview(
                        studentId,
                        request.role()
                )
        );
    }

    /*
     * SUBMIT INTERVIEW
     */

    @PostMapping("/{studentId}/evaluate")
    public ResponseEntity<MockInterviewDTO.EvaluationResponse>
    evaluateInterview(
            @PathVariable int studentId,
            @RequestBody MockInterviewDTO.SubmitRequest request) {

        return ResponseEntity.ok(
                mockInterviewService.evaluateInterview(
                        studentId,
                        request
                )
        );
    }

    // Get all interview attempts of a student
@GetMapping("/{studentId}/history")
public ResponseEntity<List<MockInterviewDTO.HistoryItem>> getInterviewHistory(
        @PathVariable int studentId) {

    List<MockInterviewDTO.HistoryItem> history =
            mockInterviewAttemptRepository
                    .findByStudent_IdOrderByCreatedAtDesc(studentId)
                    .stream()
                    .map(attempt -> new MockInterviewDTO.HistoryItem(
                            attempt.getId(),
                            attempt.getRole(),
                            attempt.getScore(),
                            attempt.getCreatedAt()
                    ))
                    .collect(Collectors.toList());

    return ResponseEntity.ok(history);
}


// Get details of a particular interview attempt
@GetMapping("/{studentId}/history/{attemptId}")
public ResponseEntity<MockInterviewDTO.HistoryDetail> getInterviewHistoryDetails(
        @PathVariable int studentId,
        @PathVariable Long attemptId) {

    return mockInterviewAttemptRepository
            .findByIdAndStudent_Id(attemptId, studentId)
            .map(attempt -> ResponseEntity.ok(
                    new MockInterviewDTO.HistoryDetail(
                            attempt.getId(),
                            attempt.getRole(),
                            attempt.getScore(),
                            attempt.getAnswersText(),
                            attempt.getEvaluation(),
                            attempt.getCreatedAt()
                    )
            ))
            .orElse(ResponseEntity.notFound().build());
}
}

