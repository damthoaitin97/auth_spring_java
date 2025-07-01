package com.example.hallyuclass.auth.service;

import com.example.hallyuclass.auth.dto.request.RefreshTokenRequest;
import com.example.hallyuclass.auth.dto.request.SignInRequest;
import com.example.hallyuclass.auth.dto.request.SignUpRequest;
import com.example.hallyuclass.auth.dto.response.LoginResponse;
import org.springframework.http.ResponseEntity;

public interface AuthenticationService {
    LoginResponse signup(SignUpRequest signUpRequest);

    LoginResponse signin(SignInRequest signInRequest);

    LoginResponse refreshToken(RefreshTokenRequest refreshTokenRequest);

    void logout(String username);

    ResponseEntity<?> verifyGoogleIdTokenAndGenerateJwt(String idTokenString);
}