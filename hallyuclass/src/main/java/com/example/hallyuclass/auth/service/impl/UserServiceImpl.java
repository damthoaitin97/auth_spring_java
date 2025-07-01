package com.example.hallyuclass.auth.service.impl;

import com.example.hallyuclass.auth.repository.UserAuthRepository;
import com.example.hallyuclass.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.CacheEvict;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserAuthRepository userAuthRepository;

    @Override
    public UserDetailsService userDetailsService() {
        return username -> userAuthRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @CacheEvict(value = "userByUsername", key = "#username")
    public void deleteUserByUsername(String username) {
        userAuthRepository.deleteByUsername(username);
    }
}