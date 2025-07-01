package com.example.hallyuclass.auth.service.impl;

import com.example.hallyuclass.auth.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheServiceImpl implements CacheService {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void clearAllCache() {
        try {
            log.info("Clearing all Redis cache...");
            Set<String> keys = redisTemplate.keys("*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("Cleared {} cache entries", keys.size());
            } else {
                log.info("No cache entries found to clear");
            }
        } catch (Exception e) {
            log.error("Error clearing all cache: {}", e.getMessage());
            throw new RuntimeException("Failed to clear all cache: " + e.getMessage());
        }
    }

    @Override
    public void clearUserCache(String username) {
        try {
            log.info("Clearing cache for user: {}", username);
            String userKey = username;
            Boolean deleted = redisTemplate.delete(userKey);
            if (Boolean.TRUE.equals(deleted)) {
                log.info("Cleared cache for user: {}", username);
            } else {
                log.info("No cache found for user: {}", username);
            }
        } catch (Exception e) {
            log.error("Error clearing cache for user {}: {}", username, e.getMessage());
            throw new RuntimeException("Failed to clear cache for user " + username + ": " + e.getMessage());
        }
    }

    @Override
    public String getCacheStatus() {
        try {
            Set<String> keys = redisTemplate.keys("*");
            int cacheCount = keys != null ? keys.size() : 0;

            StringBuilder status = new StringBuilder();
            status.append("Cache Status:\n");
            status.append("- Total cache entries: ").append(cacheCount).append("\n");

            if (keys != null && !keys.isEmpty()) {
                status.append("- Cache keys:\n");
                for (String key : keys) {
                    String value = redisTemplate.opsForValue().get(key);
                    status.append("  * ").append(key).append(": ").append(value != null ? "exists" : "null")
                            .append("\n");
                }
            }

            return status.toString();
        } catch (Exception e) {
            log.error("Error getting cache status: {}", e.getMessage());
            throw new RuntimeException("Failed to get cache status: " + e.getMessage());
        }
    }
}