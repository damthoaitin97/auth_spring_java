package com.example.hallyuclass.auth.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginTokens {
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
}