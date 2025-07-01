package com.example.hallyuclass.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignUpRequest {
    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, max = 40, message = "Password must be between 6 and 40 characters")
    private String password;

    @NotBlank(message = "Name cannot be blank")
    @Pattern(regexp = "^[\\p{L} ]+$", message = "Name must only contain letters and spaces")
    private String name;

    @Pattern(regexp = "^(0|\\+84)[3|5|7|8|9]\\d{8}$", message = "Phone number must be a valid Vietnamese number")
    private String phoneNumber;

    private String img;

    private String address;
}