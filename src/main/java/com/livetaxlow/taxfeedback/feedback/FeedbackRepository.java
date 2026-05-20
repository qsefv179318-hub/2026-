package com.livetaxlow.taxfeedback.feedback;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, UUID> {

    List<Feedback> findTop20ByUser_IdOrderByCreatedAtDesc(UUID userId);
}
