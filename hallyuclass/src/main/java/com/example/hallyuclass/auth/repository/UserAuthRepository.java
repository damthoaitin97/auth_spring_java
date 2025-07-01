package com.example.hallyuclass.auth.repository;

import com.example.hallyuclass.auth.model.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.cache.annotation.Cacheable;

import java.util.Optional;

@Repository
public interface UserAuthRepository extends JpaRepository<UserAuth, Long> {
    @Cacheable(value = "userByUsername", key = "#username")
    Optional<UserAuth> findByUsername(String username);

    boolean existsByUsername(String username);

    void deleteByUsername(String username);
}