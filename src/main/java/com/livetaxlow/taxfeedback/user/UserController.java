package com.livetaxlow.taxfeedback.user;

import com.livetaxlow.taxfeedback.user.UserDtos.CreateUserRequest;
import com.livetaxlow.taxfeedback.user.UserDtos.UpdateDependentsRequest;
import com.livetaxlow.taxfeedback.user.UserDtos.UpdateIncomeRequest;
import com.livetaxlow.taxfeedback.user.UserDtos.UserResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    UserResponse create(@Valid @RequestBody CreateUserRequest request) {
        return UserResponse.from(userService.create(request.annualIncome(), request.dependentsCount()));
    }

    @GetMapping("/{userId}")
    UserResponse get(@PathVariable UUID userId) {
        return UserResponse.from(userService.get(userId));
    }

    @PutMapping("/{userId}/income")
    UserResponse updateIncome(@PathVariable UUID userId, @Valid @RequestBody UpdateIncomeRequest request) {
        return UserResponse.from(userService.updateIncome(userId, request.annualIncome()));
    }

    @PutMapping("/{userId}/dependents")
    UserResponse updateDependents(@PathVariable UUID userId, @Valid @RequestBody UpdateDependentsRequest request) {
        return UserResponse.from(userService.updateDependents(userId, request.dependentsCount()));
    }
}
