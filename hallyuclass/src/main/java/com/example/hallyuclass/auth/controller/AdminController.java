package com.example.hallyuclass.auth.controller;

import com.example.hallyuclass.auth.dto.response.LoginResponse;
import com.example.hallyuclass.auth.service.CacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final CacheService cacheService;

    @DeleteMapping("/cache/clear")
    public ResponseEntity<LoginResponse> clearAllCache() {
        try {
            cacheService.clearAllCache();
            return ResponseEntity.ok(LoginResponse.builder()
                    .success(true)
                    .message("Xóa cache thành công")
                    .data(null)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(LoginResponse.builder()
                    .success(false)
                    .message("Lỗi khi xóa cache: " + e.getMessage())
                    .data(null)
                    .build());
        }
    }

    @DeleteMapping("/cache/user/{username}")
    public ResponseEntity<LoginResponse> clearUserCache(@PathVariable String username) {
        try {
            cacheService.clearUserCache(username);
            return ResponseEntity.ok(LoginResponse.builder()
                    .success(true)
                    .message("Xóa cache của user " + username + " thành công")
                    .data(null)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(LoginResponse.builder()
                    .success(false)
                    .message("Lỗi khi xóa cache user: " + e.getMessage())
                    .data(null)
                    .build());
        }
    }

    @GetMapping("/cache/status")
    public ResponseEntity<LoginResponse> getCacheStatus() {
        try {
            String status = cacheService.getCacheStatus();
            return ResponseEntity.ok(LoginResponse.builder()
                    .success(true)
                    .message("Thông tin cache")
                    .data(status)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(LoginResponse.builder()
                    .success(false)
                    .message("Lỗi khi lấy thông tin cache: " + e.getMessage())
                    .data(null)
                    .build());
        }
    }
}