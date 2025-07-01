package com.example.hallyuclass.auth.security;

import com.example.hallyuclass.auth.dto.error.ErrorData;
import com.example.hallyuclass.auth.dto.response.LoginResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

// Handler tuỳ chỉnh xử lý lỗi truy cập sai quyền (Access Denied) trong Spring Security
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

        // ObjectMapper dùng để chuyển đổi object Java sang JSON trả về cho client
        private final ObjectMapper objectMapper;

        /**
         * Hàm xử lý khi người dùng đã đăng nhập nhưng không đủ quyền truy cập tài
         * nguyên.
         * - Thiết lập mã lỗi 403 (FORBIDDEN).
         * - Trả về response dạng JSON với thông báo lỗi rõ ràng cho client.
         * - Giúp frontend dễ dàng nhận biết và xử lý lỗi phân quyền.
         */
        @Override
        public void handle(HttpServletRequest request, HttpServletResponse response,
                        AccessDeniedException accessDeniedException) throws IOException, ServletException {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                // Tạo object thông báo lỗi
                ErrorData errorData = new ErrorData(
                                Map.of("global", "Access Denied. You do not have permission to access this resource."));
                LoginResponse loginResponse = LoginResponse.builder()
                                .success(false)
                                .message("Access Denied")
                                .data(errorData)
                                .build();

                // Ghi response JSON ra cho client
                response.getWriter().write(objectMapper.writeValueAsString(loginResponse));
        }
}