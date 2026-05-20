package com.livetaxlow.taxfeedback.user;

import com.livetaxlow.taxfeedback.common.NotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserProfileRepository userProfileRepository;

    public UserService(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @Transactional
    public UserProfile create(long annualIncome, int dependentsCount) {
        return userProfileRepository.save(new UserProfile(annualIncome, dependentsCount));
    }

    @Transactional(readOnly = true)
    public UserProfile get(UUID userId) {
        return userProfileRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }

    @Transactional
    public UserProfile updateIncome(UUID userId, long annualIncome) {
        UserProfile user = get(userId);
        user.updateIncome(annualIncome);
        return user;
    }

    @Transactional
    public UserProfile updateDependents(UUID userId, int dependentsCount) {
        UserProfile user = get(userId);
        user.updateDependentsCount(dependentsCount);
        return user;
    }
}
