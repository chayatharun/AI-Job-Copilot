package com.jobcopilot.backend;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * Free, offline career coaching helpers. This service does not call an external AI API.
 * Its feedback is based on simple rules and should be treated as practice guidance.
 */
@Service
public class AiCoachService {

    public boolean isConfigured() {
        return true;
    }

    public String providerName() {
        return "Local rule-based coach (no API key required)";
    }

    public String generateRecommendations(AiRecommendationRequest request) {
        if (request == null || request.jobDescription() == null || request.jobDescription().isBlank()) {
            throw new IllegalArgumentException("Job description is required.");
        }

        List<String> missing = safeList(request.missingSkills());
        List<String> matching = safeList(request.matchingSkills());
        List<String> resume = safeList(request.resumeSkills());
        List<String> recommendations = new ArrayList<>();

        if (!missing.isEmpty()) {
            recommendations.add("Prioritize these skills from the job description: "
                + String.join(", ", missing.subList(0, Math.min(5, missing.size())))
                + ". Learn the fundamentals and build a small project to demonstrate them.");
        } else {
            recommendations.add("Your detected resume skills cover the skills identified in this job description. Add concrete project examples to prove your proficiency.");
        }

        if (!matching.isEmpty()) {
            recommendations.add("Make these matching skills easy to find near the top of your resume: "
                + String.join(", ", matching.subList(0, Math.min(6, matching.size()))) + ".");
        } else {
            recommendations.add("Tailor your resume summary and project bullets to the role, using only skills and experience you genuinely have.");
        }

        recommendations.add("Rewrite two project bullets using action + technology + outcome. Include measurable results only when you can verify them.");
        recommendations.add("Prepare one short example of a technical challenge, how you investigated it, and what you changed.");
        if (request.matchScore() < 50) {
            recommendations.add("Your current rule-based match score is " + request.matchScore()
                + "%. Compare the required skills with your real experience and close the most important gaps before applying.");
        } else {
            recommendations.add("Your current rule-based match score is " + request.matchScore()
                + "%. Review the job's responsibilities and prepare examples that connect your projects to them.");
        }
        if (resume.isEmpty()) {
            recommendations.add("Make sure your resume contains readable text and clearly names your technologies and project contributions.");
        }
        return String.join("\n\n", recommendations);
    }

    public String evaluateInterviewAnswer(InterviewFeedbackRequest request) {
        if (request == null || request.question() == null || request.question().isBlank()) {
            throw new IllegalArgumentException("Interview question is required.");
        }
        if (request.answer() == null || request.answer().isBlank()) {
            throw new IllegalArgumentException("Write your answer before requesting feedback.");
        }

        String answer = request.answer().trim();
        String lower = answer.toLowerCase(Locale.ROOT);
        int words = answer.split("\\s+").length;
        int score = 4;
        List<String> strengths = new ArrayList<>();
        List<String> improvements = new ArrayList<>();

        if (words >= 40) { score++; strengths.add("You provided enough detail to begin evaluating your approach."); }
        else improvements.add("Add more detail. Aim for roughly 60–120 seconds of spoken content, rather than a very short response.");
        if (containsAny(lower, "i built", "i developed", "i implemented", "i designed", "i tested", "i fixed", "i created")) {
            score++; strengths.add("You described an action you personally took.");
        } else {
            improvements.add("Clarify your personal contribution by using specific statements such as “I implemented…” or “I tested…”, when accurate.");
        }
        if (containsAny(lower, "because", "therefore", "so that", "which meant", "as a result")) {
            score++; strengths.add("You included reasoning or a connection between actions and outcomes.");
        } else {
            improvements.add("Explain why you chose that approach and what changed because of it.");
        }
        if (containsAny(lower, "result", "improved", "reduced", "increased", "learned", "successfully", "outcome")) {
            score++; strengths.add("You mentioned an outcome or lesson.");
        } else {
            improvements.add("Finish with the result, evidence of success, or what you learned.");
        }
        if (containsAny(lower, "team", "customer", "user", "project", "deadline", "challenge", "problem")) {
            score++; strengths.add("You included context that helps a listener understand the situation.");
        } else {
            improvements.add("Give brief context: the project, problem, user need, or team situation.");
        }

        String questionLower = request.question().toLowerCase(Locale.ROOT);
        String structure = questionLower.contains("tell me about") || questionLower.contains("describe")
            || questionLower.contains("challenge") || questionLower.contains("conflict")
            ? "For a behavioural question, structure your answer as Situation, Task, Action, Result (STAR)."
            : "For a technical question, define the concept, explain your approach, give a concrete example, and mention how you tested it.";

        StringBuilder feedback = new StringBuilder();
        feedback.append("Practice score: ").append(Math.min(10, score)).append("/10 (rule-based estimate, not an AI or hiring assessment).");
        feedback.append("\n\nWhat is working:\n");
        if (strengths.isEmpty()) feedback.append("- Your answer is a starting point; add specific details about your own actions.\n");
        else for (String item : strengths) feedback.append("- ").append(item).append('\n');
        feedback.append("\nWhat to improve:\n");
        if (improvements.isEmpty()) feedback.append("- Keep the answer focused and make sure each claim is backed by a real example.\n");
        else for (String item : improvements) feedback.append("- ").append(item).append('\n');
        feedback.append("\nSuggested structure:\n").append(structure);
        feedback.append("\n\nRemember: this feedback checks answer structure and keywords, not factual correctness or technical depth. Adapt it to your real experience.");
        return feedback.toString();
    }

    private boolean containsAny(String text, String... phrases) {
        for (String phrase : phrases) if (text.contains(phrase)) return true;
        return false;
    }

    private List<String> safeList(List<String> values) {
        if (values == null) return List.of();
        Set<String> unique = new LinkedHashSet<>();
        for (String value : values) {
            if (value != null && !value.isBlank()) unique.add(value.trim());
        }
        return new ArrayList<>(unique);
    }

    public record AiRecommendationRequest(String jobDescription, List<String> resumeSkills,
            List<String> jobSkills, List<String> matchingSkills, List<String> missingSkills, int matchScore) {}
    public record InterviewFeedbackRequest(String jobDescription, String question, String answer) {}
    public static class AiNotConfiguredException extends RuntimeException {
        public AiNotConfiguredException(String message) { super(message); }
    }
    public static class AiProviderException extends RuntimeException {
        public AiProviderException(String message) { super(message); }
    }
}
