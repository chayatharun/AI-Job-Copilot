
package com.jobcopilot.backend;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

@Service
public class JobAnalysisService {

    private static final List<String> SKILL_LIBRARY = List.of(
        "JavaScript",
        "TypeScript",
        "Java",
        "Python",
        "C++",
        "C#",
        "SQL",
        "MySQL",
        "PostgreSQL",
        "MongoDB",
        "HTML",
        "CSS",
        "React",
        "Angular",
        "Vue",
        "Node.js",
        "Express",
        "Spring Boot",
        "Flask",
        "Django",
        "REST API",
        "Git",
        "GitHub",
        "Docker",
        "Kubernetes",
        "AWS",
        "Azure",
        "Google Cloud",
        "Linux",
        "Data Structures",
        "Algorithms",
        "OOP",
        "Machine Learning",
        "Deep Learning",
        "NLP",
        "TensorFlow",
        "PyTorch",
        "scikit-learn",
        "Pandas",
        "NumPy",
        "Excel",
        "Power BI",
        "Tableau",
        "Agile",
        "Selenium",
        "JUnit",
        "Maven"
    );

    public AnalysisResult analyze(String resumeText, String jobDescription) {
        if (resumeText == null || resumeText.isBlank()) {
            throw new IllegalArgumentException(
                "The resume contains no readable text."
            );
        }

        if (jobDescription == null || jobDescription.isBlank()) {
            throw new IllegalArgumentException(
                "Please provide a job description."
            );
        }

        Set<String> resumeSkills = findSkills(resumeText);
        Set<String> jobSkills = findSkills(jobDescription);

        List<String> matchingSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();

        for (String skill : jobSkills) {
            if (resumeSkills.contains(skill)) {
                matchingSkills.add(skill);
            } else {
                missingSkills.add(skill);
            }
        }

        int matchScore = jobSkills.isEmpty()
            ? 0
            : (int) Math.round(
                matchingSkills.size() * 100.0 / jobSkills.size()
            );

        List<String> recommendations = buildRecommendations(
            jobSkills,
            matchingSkills,
            missingSkills,
            resumeSkills
        );

        return new AnalysisResult(
            matchScore,
            new ArrayList<>(resumeSkills),
            new ArrayList<>(jobSkills),
            matchingSkills,
            missingSkills,
            recommendations,
            resumeText.length(),
            jobSkills.size(),
            "Rule-based skill comparison"
        );
    }

    private Set<String> findSkills(String text) {
        Set<String> foundSkills = new LinkedHashSet<>();

        for (String skill : SKILL_LIBRARY) {
            Pattern pattern = Pattern.compile(
                "(?i)(?<![a-z0-9])"
                    + Pattern.quote(skill)
                    + "(?![a-z0-9])"
            );

            if (pattern.matcher(text).find()) {
                foundSkills.add(skill);
            }
        }

        return foundSkills;
    }

    private List<String> buildRecommendations(
            Set<String> jobSkills,
            List<String> matchingSkills,
            List<String> missingSkills,
            Set<String> resumeSkills) {

        List<String> recommendations = new ArrayList<>();

        if (jobSkills.isEmpty()) {
            recommendations.add(
                "No supported technical skills were detected in the job "
                + "description. Include clearer skill requirements for "
                + "a more useful comparison."
            );
        } else if (missingSkills.isEmpty()) {
            recommendations.add(
                "Your resume mentions all detected skills from this job "
                + "description. Verify that your experience demonstrates "
                + "each skill."
            );
        } else {
            recommendations.add(
                "Review these job requirements and build practical "
                + "experience with the missing skills: "
                + String.join(", ", missingSkills) + "."
            );
        }

        if (matchingSkills.isEmpty()) {
            recommendations.add(
                "Add relevant projects or work experience to your resume "
                + "where they truthfully demonstrate the role's requirements."
            );
        } else {
            recommendations.add(
                "Highlight your experience with "
                + String.join(", ", matchingSkills)
                + " in your skills and project sections."
            );
        }

        if (resumeSkills.isEmpty()) {
            recommendations.add(
                "No supported skills were detected in your resume text. "
                + "Check the extracted text and use clear skill names "
                + "in your resume."
            );
        }

        recommendations.add(
            "Tailor your project descriptions to the role and include "
            + "specific responsibilities and measurable results where "
            + "you can substantiate them."
        );

        return recommendations;
    }

    public record AnalysisResult(
        int matchScore,
        List<String> resumeSkills,
        List<String> jobSkills,
        List<String> matchingSkills,
        List<String> missingSkills,
        List<String> recommendations,
        int extractedCharacterCount,
        int detectedJobSkillCount,
        String analysisMethod
    ) {}
}
