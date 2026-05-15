package com.vibecoding.tax_server.repository; // 여기도 tax_server 포함!

import com.vibecoding.tax_server.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByLoginId(String loginId);
}