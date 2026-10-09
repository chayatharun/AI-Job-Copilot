package com.jobcopilot.backend;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = "http://localhost:5173")
public class JobApplicationController {
    private static final List<String> ALLOWED_STATUSES = List.of(
        "Saved", "Applied", "Assessment", "Interview", "Offer", "Rejected", "Withdrawn"
    );
    private final JobApplicationRepository repository;

    public JobApplicationController(JobApplicationRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<JobApplication> list() {
        return repository.findAllByOrderByUpdatedAtDesc();
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ApplicationRequest request) {
        String validation = validate(request);
        if (validation != null) return ResponseEntity.badRequest().body(Map.of("error", validation));
        JobApplication saved = repository.save(new JobApplication(
            request.jobTitle().trim(), request.company().trim(), clean(request.jobUrl()),
            normalizeStatus(request.status()), request.appliedDate(), clean(request.notes())
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody ApplicationRequest request) {
        String validation = validate(request);
        if (validation != null) return ResponseEntity.badRequest().body(Map.of("error", validation));
        return repository.findById(id).<ResponseEntity<?>>map(existing -> {
            existing.update(request.jobTitle().trim(), request.company().trim(), clean(request.jobUrl()),
                normalizeStatus(request.status()), request.appliedDate(), clean(request.notes()));
            return ResponseEntity.ok(repository.save(existing));
        }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", "Application not found.")));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody StatusRequest request) {
        if (request == null || request.status() == null || !ALLOWED_STATUSES.contains(request.status())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Choose a valid application status."));
        }
        return repository.findById(id).<ResponseEntity<?>>map(existing -> {
            existing.update(existing.getJobTitle(), existing.getCompany(), existing.getJobUrl(),
                request.status(), existing.getAppliedDate(), existing.getNotes());
            return ResponseEntity.ok(repository.save(existing));
        }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", "Application not found.")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Application not found."));
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private String validate(ApplicationRequest request) {
        if (request == null || request.jobTitle() == null || request.jobTitle().isBlank()) return "Job title is required.";
        if (request.company() == null || request.company().isBlank()) return "Company name is required.";
        if (request.jobTitle().length() > 200 || request.company().length() > 200) return "Job title and company must be 200 characters or fewer.";
        if (request.jobUrl() != null && request.jobUrl().length() > 1000) return "Job URL must be 1,000 characters or fewer.";
        if (request.notes() != null && request.notes().length() > 3000) return "Notes must be 3,000 characters or fewer.";
        if (request.status() != null && !ALLOWED_STATUSES.contains(request.status())) return "Choose a valid application status.";
        if (request.appliedDate() != null && request.appliedDate().isAfter(LocalDate.now().plusDays(1))) return "Applied date cannot be in the future.";
        return null;
    }

    private String normalizeStatus(String status) {
        return status == null || status.isBlank() ? "Saved" : status;
    }
    private String clean(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    public record ApplicationRequest(String jobTitle, String company, String jobUrl,
                                     String status, LocalDate appliedDate, String notes) {}
    public record StatusRequest(String status) {}
}
