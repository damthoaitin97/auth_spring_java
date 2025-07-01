package com.example.hallyuclass.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Cấu hình CORS (Cross-Origin Resource Sharing) cho ứng dụng Spring Boot
@Configuration
public class CorsConfig {
    /**
     * Bean cấu hình CORS cho toàn bộ ứng dụng.
     * Cho phép các domain nhất định truy cập API backend.
     * - allowedOrigins: Chỉ định domain frontend được phép gọi API (nên thay bằng
     * domain thực tế).
     * - allowedMethods: Các phương thức HTTP được phép (GET, POST, PUT, DELETE).
     * - allowedHeaders: Cho phép tất cả các header.
     * - allowCredentials: Cho phép gửi cookie, token qua cross-origin.
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("https://your-frontend-domain.com") // Thay bằng domain thực tế
                        .allowedMethods("GET", "POST", "PUT", "DELETE")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}