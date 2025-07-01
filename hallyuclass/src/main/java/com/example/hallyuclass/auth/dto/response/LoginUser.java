package com.example.hallyuclass.auth.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginUser {
    private String img;
    private String username;
    private String email;
    private String name;
    private String roles;
}