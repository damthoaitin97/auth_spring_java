package com.example.hallyuclass.auth.service;

public interface CacheService {
    void clearAllCache();

    void clearUserCache(String username);

    String getCacheStatus();
}