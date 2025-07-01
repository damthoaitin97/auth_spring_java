package com.example.hallyuclass.auth.dto.error;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.Map;

@Data
@AllArgsConstructor
public class ErrorData {
    private Map<String, String> errors;
}