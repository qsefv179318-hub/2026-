package com.livetaxlow.taxfeedback.feedback;

import com.livetaxlow.taxfeedback.feedback.FeedbackDtos.FeedbackResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/{userId}/feedbacks")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @GetMapping
    List<FeedbackResponse> list(@PathVariable UUID userId) {
        return feedbackService.list(userId).stream()
                .map(FeedbackResponse::from)
                .toList();
    }
}
