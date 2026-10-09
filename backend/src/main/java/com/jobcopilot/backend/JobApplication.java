package com.jobcopilot.backend;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_applications")
public class JobApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String jobTitle;

    @Column(nullable = false, length = 200)
    private String company;

    @Column(length = 1000)
    private String jobUrl;

    @Column(nullable = false, length = 40)
    private String status;

    private LocalDate appliedDate;

    @Column(length = 3000)
    private String notes;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected JobApplication() {}

    public JobApplication(String jobTitle, String company, String jobUrl, String status,
                          LocalDate appliedDate, String notes) {
        this.jobTitle = jobTitle;
        this.company = company;
        this.jobUrl = jobUrl;
        this.status = status;
        this.appliedDate = appliedDate;
        this.notes = notes;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    public void beforeUpdate() { this.updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public String getJobTitle() { return jobTitle; }
    public String getCompany() { return company; }
    public String getJobUrl() { return jobUrl; }
    public String getStatus() { return status; }
    public LocalDate getAppliedDate() { return appliedDate; }
    public String getNotes() { return notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void update(String jobTitle, String company, String jobUrl, String status,
                       LocalDate appliedDate, String notes) {
        this.jobTitle = jobTitle;
        this.company = company;
        this.jobUrl = jobUrl;
        this.status = status;
        this.appliedDate = appliedDate;
        this.notes = notes;
        this.updatedAt = LocalDateTime.now();
    }
}
