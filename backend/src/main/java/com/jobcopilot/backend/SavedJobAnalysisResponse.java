package com.jobcopilot.backend;

import java.time.LocalDateTime;
import java.util.List;

public record SavedJobAnalysisResponse(
    Long id,
    String resumeFilename,
    String jobDescription,
    int matchScore,
    List<String> resumeSkills,
    List<String> jobSkills,
    List<String> matchingSkills,
    List<String> missingSkills,
    List<String> recommendations,
    int extractedCharacterCount,
    int detectedJobSkillCount,
    String analysisMethod,
    LocalDateTime createdAt
) {}
