package com.example.hallyuclass.auth.repository;

import com.example.hallyuclass.auth.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.cache.annotation.Cacheable;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    @Cacheable(value = "userByEmail", key = "#email")
    UserProfile findByEmail(String email);

    @Cacheable(value = "userByPhone", key = "#phoneNumber")
    UserProfile findByPhoneNumber(String phoneNumber);
}