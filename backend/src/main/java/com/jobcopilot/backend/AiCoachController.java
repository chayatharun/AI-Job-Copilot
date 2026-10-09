package com.jobcopilot.backend;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "http://localhost:5173")
public class AiCoachController {
    private final AiCoachService service;

    public AiCoachController(AiCoachService service) {
        this.service = service;
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of("configured", true, "provider", service.providerName(), "apiKeyRequired", false);
    }

    @PostMapping("/recommendations")
    public ResponseEntity<?> recommendations(@RequestBody AiCoachService.AiRecommendationRequest request) {
        try {
            return ResponseEntity.ok(Map.of("recommendations", service.generateRecommendations(request)));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("error", exception.getMessage()));
        }
    }

    @PostMapping("/interview-feedback")
    public ResponseEntity<?> interviewFeedback(
            @RequestBody(required = false) AiCoachService.InterviewFeedbackRequest request) {
        try {
            return ResponseEntity.ok(Map.of("feedback", service.evaluateInterviewAnswer(request)));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("error", exception.getMessage()));
        }
    }
}
