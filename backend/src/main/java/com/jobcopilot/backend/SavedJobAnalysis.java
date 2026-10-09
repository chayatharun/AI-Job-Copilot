package com.jobcopilot.backend;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_analyses")
public class SavedJobAnalysis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String resumeFilename;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String jobDescription;

    @Column(nullable = false)
    private int matchScore;

    @Lob @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String resumeSkillsJson;
    @Lob @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String jobSkillsJson;
    @Lob @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String matchingSkillsJson;
    @Lob @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String missingSkillsJson;
    @Lob @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String recommendationsJson;

    private int extractedCharacterCount;
    private int detectedJobSkillCount;

    @Column(nullable = false, length = 100)
    private String analysisMethod;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected SavedJobAnalysis() {}

    public SavedJobAnalysis(String resumeFilename, String jobDescription, int matchScore,
            String resumeSkillsJson, String jobSkillsJson, String matchingSkillsJson,
            String missingSkillsJson, String recommendationsJson, int extractedCharacterCount,
            int detectedJobSkillCount, String analysisMethod) {
        this.resumeFilename = resumeFilename;
        this.jobDescription = jobDescription;
        this.matchScore = matchScore;
        this.resumeSkillsJson = resumeSkillsJson;
        this.jobSkillsJson = jobSkillsJson;
        this.matchingSkillsJson = matchingSkillsJson;
        this.missingSkillsJson = missingSkillsJson;
        this.recommendationsJson = recommendationsJson;
        this.extractedCharacterCount = extractedCharacterCount;
        this.detectedJobSkillCount = detectedJobSkillCount;
        this.analysisMethod = analysisMethod;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getResumeFilename() { return resumeFilename; }
    public String getJobDescription() { return jobDescription; }
    public int getMatchScore() { return matchScore; }
    public String getResumeSkillsJson() { return resumeSkillsJson; }
    public String getJobSkillsJson() { return jobSkillsJson; }
    public String getMatchingSkillsJson() { return matchingSkillsJson; }
    public String getMissingSkillsJson() { return missingSkillsJson; }
    public String getRecommendationsJson() { return recommendationsJson; }
    public int getExtractedCharacterCount() { return extractedCharacterCount; }
    public int getDetectedJobSkillCount() { return detectedJobSkillCount; }
    public String getAnalysisMethod() { return analysisMethod; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
