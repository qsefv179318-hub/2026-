package com.vibecoding.tax_server.controller;

import com.vibecoding.tax_server.dto.UserJoinRequest;
import com.vibecoding.tax_server.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/check")
    public String checkServer() {
        return "서버가 쌩쌩 돌아가고 있습니다! 🚀";
    }

    // 회원가입 API
    @PostMapping("/join")
    public String join(@RequestBody UserJoinRequest request) {
        return userService.join(request.getLoginId(), request.getPassword());
    }
}