package com.vibecoding.tax_server.service;

import com.vibecoding.tax_server.entity.User;
import com.vibecoding.tax_server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public String join(String loginId, String password) {
        // 1. 중복 ID 체크
        userRepository.findByLoginId(loginId)
                .ifPresent(user -> {
                    throw new RuntimeException("이미 존재하는 아이디입니다.");
                });

        // 2. 저장 (실제로는 비밀번호 암호화가 필요하지만, 우선 저장부터!)
        User user = new User(loginId, password);
        userRepository.save(user);

        return "회원가입 성공!";
    }
}