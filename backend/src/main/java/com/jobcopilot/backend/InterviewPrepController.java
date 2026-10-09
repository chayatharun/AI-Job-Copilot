package com.jobcopilot.backend;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interview")
@CrossOrigin(origins = "http://localhost:5173")
public class InterviewPrepController {
    private final InterviewPrepService service;

    public InterviewPrepController(InterviewPrepService service) {
        this.service = service;
    }

    @PostMapping("/questions")
    public ResponseEntity<?> generateQuestions(@RequestBody InterviewRequest request) {
        try {
            if (request == null || request.jobDescription() == null || request.jobDescription().isBlank()) {
                return ResponseEntity.badRequest().body(
                    Map.of("error", "Enter a job description to generate interview questions."));
            }
            return ResponseEntity.ok(service.generate(request.jobDescription(), request.skills()));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("error", exception.getMessage()));
        }
    }

    public record InterviewRequest(String jobDescription, List<String> skills) {}
}
