package com.vibecoding.repository;

import com.vibecoding.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.Optional;

// JpaRepository를 상속받으면 기본적인 CRUD(저장, 조회 등)는 스프링이 다 해줍니다.
public interface UserRepository extends JpaRepository<User, UUID> {
    // ID로 사용자를 찾는 기능을 추가합니다.
    Optional<User> findByLoginId(String loginId);
}
