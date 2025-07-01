package com.example.hallyuclass.auth.security;

import com.example.hallyuclass.auth.service.JwtService;
import com.example.hallyuclass.auth.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Lớp filter kiểm tra và xác thực JWT cho mỗi request (chạy 1 lần cho mỗi request)
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // Service xử lý logic liên quan đến JWT (tạo, xác thực, trích xuất thông tin từ
    // token)
    private final JwtService jwtService;
    // Service lấy thông tin user từ database
    private final UserService userService;

    /**
     * Hàm chính của filter, được gọi cho mỗi request.
     * - Kiểm tra header Authorization có chứa Bearer token không.
     * - Nếu có, trích xuất JWT và lấy username từ token.
     * - Kiểm tra token hợp lệ và user chưa được xác thực trong context.
     * - Nếu hợp lệ, tạo Authentication và set vào SecurityContextHolder (đánh dấu
     * user đã đăng nhập).
     * - Nếu không có token hoặc token không hợp lệ, bỏ qua filter này.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;
        // Nếu không có header hoặc header không bắt đầu bằng "Bearer ", bỏ qua filter
        // này
        if (StringUtils.isEmpty(authHeader) || !StringUtils.startsWith(authHeader, "Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        // Lấy JWT từ header
        jwt = authHeader.substring(7);
        // Trích xuất username (email) từ JWT
        userEmail = jwtService.extractUserName(jwt);
        // Nếu có username và chưa có Authentication trong context
        if (StringUtils.isNotEmpty(userEmail) && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Lấy thông tin user từ database
            UserDetails userDetails = userService.userDetailsService().loadUserByUsername(userEmail);
            // Kiểm tra token hợp lệ với user
            if (jwtService.isTokenValid(jwt, userDetails)) {
                // Tạo context xác thực mới
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                context.setAuthentication(authToken);
                SecurityContextHolder.setContext(context);
            }
        }
        // Tiếp tục chuỗi filter
        filterChain.doFilter(request, response);
    }
}