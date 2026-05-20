package com.livetaxlow.taxfeedback.user;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public final class UserDtos {

    private UserDtos() {
    }

    public record CreateUserRequest(
            @NotNull @Min(0) Long annualIncome,
            @NotNull @Min(0) Integer dependentsCount
    ) {
    }

    public record UpdateIncomeRequest(@NotNull @Min(0) Long annualIncome) {
    }

    public record UpdateDependentsRequest(@NotNull @Min(0) Integer dependentsCount) {
    }

    public record UserResponse(
            UUID id,
            long annualIncome,
            int dependentsCount,
            Instant createdAt,
            Instant updatedAt
    ) {
        static UserResponse from(UserProfile user) {
            return new UserResponse(
                    user.getId(),
                    user.getAnnualIncome(),
                    user.getDependentsCount(),
                    user.getCreatedAt(),
                    user.getUpdatedAt()
            );
        }
    }
}
