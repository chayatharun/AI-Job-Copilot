package com.jobcopilot.backend;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class JobAnalysisController {
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;

    private final ResumeParserService resumeParserService;
    private final JobAnalysisService jobAnalysisService;
    private final SavedJobAnalysisRepository repository;
    private final ObjectMapper objectMapper;

    public JobAnalysisController(ResumeParserService resumeParserService,
            JobAnalysisService jobAnalysisService, SavedJobAnalysisRepository repository,
            ObjectMapper objectMapper) {
        this.resumeParserService = resumeParserService;
        this.jobAnalysisService = jobAnalysisService;
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> analyzeJob(
            @RequestParam("resume") MultipartFile resume,
            @RequestParam("jobDescription") String jobDescription) {
        try {
            if (resume == null || resume.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Please upload your resume."));
            }
            if (resume.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.badRequest().body(Map.of("error", "Resume file must be 10 MB or smaller."));
            }
            if (jobDescription == null || jobDescription.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Please enter a job description."));
            }
            if (jobDescription.length() > 50000) {
                return ResponseEntity.badRequest().body(Map.of("error", "Job description must be 50,000 characters or fewer."));
            }

            String resumeText = resumeParserService.extractText(resume);
            JobAnalysisService.AnalysisResult result = jobAnalysisService.analyze(resumeText, jobDescription);
            SavedJobAnalysis saved = repository.save(new SavedJobAnalysis(
                safeFilename(resume.getOriginalFilename()), jobDescription.trim(), result.matchScore(),
                json(result.resumeSkills()), json(result.jobSkills()), json(result.matchingSkills()),
                json(result.missingSkills()), json(result.recommendations()), result.extractedCharacterCount(),
                result.detectedJobSkillCount(), result.analysisMethod()
            ));
            return ResponseEntity.ok(toResponse(saved));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("error", exception.getMessage()));
        } catch (IOException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                Map.of("error", "Unable to read the resume. Please check the file and upload it again."));
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("error", "Unable to analyze and save this resume. Check the backend console for details."));
        }
    }

    @GetMapping("/analyses")
    public List<SavedJobAnalysisResponse> getAnalyses() {
        return repository.findAllByOrderByCreatedAtDesc().stream().map(this::toResponse).toList();
    }

    @GetMapping("/analyses/{id}")
    public ResponseEntity<?> getAnalysis(@PathVariable Long id) {
        return repository.findById(id)
            .<ResponseEntity<?>>map(saved -> ResponseEntity.ok(toResponse(saved)))
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Saved analysis was not found.")));
    }

    private String safeFilename(String filename) {
        return (filename == null || filename.isBlank()) ? "resume" : filename;
    }

    private String json(List<String> values) throws JsonProcessingException {
        return objectMapper.writeValueAsString(values);
    }

    private List<String> parseList(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception exception) {
            return List.of();
        }
    }

    private SavedJobAnalysisResponse toResponse(SavedJobAnalysis saved) {
        return new SavedJobAnalysisResponse(
            saved.getId(), saved.getResumeFilename(), saved.getJobDescription(), saved.getMatchScore(),
            parseList(saved.getResumeSkillsJson()), parseList(saved.getJobSkillsJson()),
            parseList(saved.getMatchingSkillsJson()), parseList(saved.getMissingSkillsJson()),
            parseList(saved.getRecommendationsJson()), saved.getExtractedCharacterCount(),
            saved.getDetectedJobSkillCount(), saved.getAnalysisMethod(), saved.getCreatedAt()
        );
    }
}
