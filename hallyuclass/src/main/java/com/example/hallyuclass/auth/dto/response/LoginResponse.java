package com.example.hallyuclass.auth.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private boolean success;
    private String message;
    private Object data;
}