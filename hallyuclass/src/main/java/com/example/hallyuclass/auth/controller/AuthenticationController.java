package com.example.hallyuclass.auth.controller;

import com.example.hallyuclass.auth.dto.response.LoginResponse;
import com.example.hallyuclass.auth.dto.request.RefreshTokenRequest;
import com.example.hallyuclass.auth.dto.request.SignInRequest;
import com.example.hallyuclass.auth.dto.request.SignUpRequest;
import com.example.hallyuclass.auth.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/signup")
    public ResponseEntity<LoginResponse> signup(@RequestBody SignUpRequest signUpRequest) {
        return ResponseEntity.ok(authenticationService.signup(signUpRequest));
    }

    @PostMapping("/signin")
    public ResponseEntity<LoginResponse> signin(@RequestBody SignInRequest signInRequest) {
        return ResponseEntity.ok(authenticationService.signin(signInRequest));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        return ResponseEntity.ok(authenticationService.refreshToken(refreshTokenRequest));
    }

    @PostMapping("/logout")
    public ResponseEntity<LoginResponse> logout(@AuthenticationPrincipal UserDetails userDetails) {
        authenticationService.logout(userDetails.getUsername());
        return ResponseEntity.ok(
                LoginResponse.builder()
                        .success(true)
                        .message("Đăng xuất thành công")
                        .data(null)
                        .build());
    }

    @PostMapping("/google")
    public ResponseEntity<?> loginWithGoogle(@RequestBody Map<String, String> body) {
        System.out.println("helllo");
        String idToken = body.get("idToken");
        return authenticationService.verifyGoogleIdTokenAndGenerateJwt(idToken);
    }

}