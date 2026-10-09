package com.jobcopilot.backend;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavedJobAnalysisRepository extends JpaRepository<SavedJobAnalysis, Long> {
    List<SavedJobAnalysis> findAllByOrderByCreatedAtDesc();
}
