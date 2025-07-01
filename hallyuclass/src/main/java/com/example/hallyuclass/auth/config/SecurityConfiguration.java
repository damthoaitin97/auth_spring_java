package com.example.hallyuclass.auth.config;

import com.example.hallyuclass.auth.model.Roles;
import com.example.hallyuclass.auth.security.CustomAccessDeniedHandler;
import com.example.hallyuclass.auth.security.JwtAuthenticationFilter;
import com.example.hallyuclass.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.AuthenticationEntryPoint;
//import com.example.hallyuclass.auth.security.CustomOAuth2SuccessHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserService userService;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    // private final CustomOAuth2SuccessHandler customOAuth2SuccessHandler;

    /**
     * Cấu hình chuỗi filter bảo mật chính của Spring Security.
     * - Phân quyền truy cập các endpoint.
     * - Xử lý exception khi truy cập trái phép hoặc chưa xác thực.
     * - Thiết lập session stateless (không lưu trạng thái đăng nhập trên server).
     * - Thêm filter xác thực JWT trước filter mặc định.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        request -> request
                                .requestMatchers("/api/v1/auth/signup", "/api/v1/auth/signin", "/api/v1/auth/refresh",
                                        "/api/v1/auth/google",
                                        "/", "/login/**", "/oauth2/**", "/api/v1/admin/**")
                                .permitAll()
                                // .requestMatchers("/api/v1/admin/**").hasAuthority("ADMIN")
                                .requestMatchers("/api/v1/user/**").hasAuthority("USER")
                                .anyRequest().authenticated())
                .exceptionHandling(e -> e
                        .accessDeniedHandler(customAccessDeniedHandler)
                        .authenticationEntryPoint(authenticationEntryPoint()))
                .sessionManagement(manager -> manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider()).addFilterBefore(
                        jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        // .oauth2Login(oauth2 -> oauth2.successHandler(customOAuth2SuccessHandler));
        return http.build();
    }

    /**
     * Bean cung cấp AuthenticationProvider sử dụng UserService và mã hóa mật khẩu.
     * Dùng để xác thực thông tin đăng nhập của user.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userService.userDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    /**
     * Bean cung cấp PasswordEncoder sử dụng thuật toán BCrypt.
     * Dùng để mã hóa và kiểm tra mật khẩu an toàn.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Bean cung cấp AuthenticationManager để quản lý xác thực.
     * Lấy từ cấu hình AuthenticationConfiguration của Spring.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Bean xử lý khi người dùng chưa xác thực mà truy cập tài nguyên cần đăng nhập.
     * Trả về JSON thông báo lỗi 401 (Unauthorized).
     */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            ObjectMapper objectMapper = new ObjectMapper();
            com.example.hallyuclass.auth.dto.error.ErrorData errorData = new com.example.hallyuclass.auth.dto.error.ErrorData(
                    java.util.Map.of("global", "Unauthorized"));
            com.example.hallyuclass.auth.dto.response.LoginResponse loginResponse = com.example.hallyuclass.auth.dto.response.LoginResponse
                    .builder()
                    .success(false)
                    .message("Unauthorized")
                    .data(errorData)
                    .build();
            response.getWriter().write(objectMapper.writeValueAsString(loginResponse));
        };
    }
}