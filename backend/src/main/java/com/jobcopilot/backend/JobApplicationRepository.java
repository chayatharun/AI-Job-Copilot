package com.jobcopilot.backend;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findAllByOrderByUpdatedAtDesc();
}
