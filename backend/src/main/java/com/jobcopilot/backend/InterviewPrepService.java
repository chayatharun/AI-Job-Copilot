package com.jobcopilot.backend;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class InterviewPrepService {
    public List<InterviewQuestion> generate(String jobDescription, List<String> suppliedSkills) {
        String text = jobDescription == null ? "" : jobDescription.trim();
        if (text.isBlank()) {
            throw new IllegalArgumentException("Enter a job description to generate interview questions.");
        }
        Set<String> skills = new LinkedHashSet<>();
        if (suppliedSkills != null) {
            suppliedSkills.stream().filter(s -> s != null && !s.isBlank()).forEach(skills::add);
        }
        String lower = text.toLowerCase(Locale.ROOT);
        for (String skill : List.of("Java", "Python", "JavaScript", "React", "SQL", "MySQL",
                "Spring Boot", "REST API", "HTML", "CSS", "Node.js", "Machine Learning",
                "Data Structures", "Git", "Docker", "AWS", "Flask", "OOP")) {
            if (lower.contains(skill.toLowerCase(Locale.ROOT))) skills.add(skill);
        }

        List<InterviewQuestion> questions = new ArrayList<>();
        questions.add(new InterviewQuestion("General", "Tell me about yourself and why you are interested in this role.",
            "Give a 60–90 second summary: your education or experience, two relevant skills, one project with your contribution, and why those strengths fit this role."));
        questions.add(new InterviewQuestion("Projects", "Walk me through a project that is relevant to this position.",
            "Explain the problem, your role, the technology choices, one challenge, how you tested it, and the result. Be specific about what you personally implemented."));
        questions.add(new InterviewQuestion("Problem solving", "Describe a technical problem you faced and how you solved it.",
            "Use the STAR structure: situation, task, action, result. Explain how you investigated the issue and what you learned."));
        for (String skill : skills.stream().limit(8).toList()) {
            questions.add(questionForSkill(skill));
        }
        questions.add(new InterviewQuestion("Behavioural", "What would you do if you did not know how to solve a task assigned to you?",
            "Clarify the expected outcome, break the task into smaller parts, research trusted documentation, test a small solution, and ask a focused question when blocked."));
        questions.add(new InterviewQuestion("Role fit", "What would you like to learn or achieve in your first 90 days?",
            "Describe a realistic plan: learn the codebase and team workflow, deliver a small task, seek feedback, and gradually own a measurable feature."));
        return questions;
    }

    private InterviewQuestion questionForSkill(String skill) {
        String normalized = skill.toLowerCase(Locale.ROOT);
        if (normalized.contains("sql") || normalized.contains("mysql")) {
            return new InterviewQuestion("Technical · " + skill,
                "How would you design and query a relational database for a real application?",
                "Discuss tables, primary and foreign keys, relationships, constraints, indexes, joins, and how you would validate query results.");
        }
        if (normalized.contains("react") || normalized.contains("javascript")) {
            return new InterviewQuestion("Technical · " + skill,
                "Explain how you would build and debug an interactive UI using " + skill + ".",
                "Describe component responsibilities, state and props, event handling, loading/error states, and how you would inspect browser console/network errors.");
        }
        if (normalized.contains("java") || normalized.contains("spring") || normalized.equals("oop")) {
            return new InterviewQuestion("Technical · " + skill,
                "How have you used " + skill + " to structure or implement an application?",
                "Explain the relevant concepts, the design you chose, one trade-off, and a concrete example from your own code.");
        }
        if (normalized.contains("machine learning")) {
            return new InterviewQuestion("Technical · " + skill,
                "How would you evaluate a machine-learning model for a practical problem?",
                "Explain train/validation/test splits, an appropriate metric, class imbalance if relevant, overfitting checks, and how you would communicate limitations.");
        }
        return new InterviewQuestion("Technical · " + skill,
            "Describe a practical task where you would use " + skill + " and how you would approach it.",
            "Define the problem, explain the core concept, outline the steps, mention how you would test the result, and give an example you can honestly support.");
    }

    public record InterviewQuestion(String category, String question, String suggestedAnswer) {}
}
