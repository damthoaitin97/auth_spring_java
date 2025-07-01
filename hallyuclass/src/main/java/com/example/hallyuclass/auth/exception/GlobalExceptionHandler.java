package com.example.hallyuclass.auth.exception;

import com.example.hallyuclass.auth.dto.response.LoginResponse;
import com.example.hallyuclass.auth.dto.error.ErrorData;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Xử lý khi không tìm thấy resource (ví dụ: user không tồn tại)
     * Trả về HTTP 404 và thông báo lỗi cho client
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<LoginResponse> handleResourceNotFoundException(ResourceNotFoundException ex,
            WebRequest request) {
        ErrorData errorData = new ErrorData(Map.of("global", ex.getMessage()));
        LoginResponse response = LoginResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .data(errorData)
                .build();
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * Xử lý khi tạo resource bị trùng (ví dụ: đăng ký user đã tồn tại)
     * Trả về HTTP 409 Conflict
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<LoginResponse> handleDuplicateResourceException(DuplicateResourceException ex,
            WebRequest request) {
        ErrorData errorData = new ErrorData(Map.of("global", ex.getMessage()));
        LoginResponse response = LoginResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .data(errorData)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    /**
     * Xử lý lỗi validate dữ liệu đầu vào (custom ValidationException)
     * Trả về HTTP 400 Bad Request
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<LoginResponse> handleValidationException(ValidationException ex) {
        ErrorData errorData = new ErrorData(ex.getErrors());
        LoginResponse response = LoginResponse.builder()
                .success(false)
                .message("Đăng ký thất bại")
                .data(errorData)
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Xử lý lỗi validate dữ liệu đầu vào (annotation @Valid)
     * Trả về HTTP 400 Bad Request
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        ErrorData errorData = new ErrorData(errors);
        LoginResponse response = LoginResponse.builder()
                .success(false)
                .message("Đăng ký thất bại")
                .data(errorData)
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Xử lý sai username/password khi đăng nhập
     * Trả về HTTP 401 Unauthorized
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<LoginResponse> handleBadCredentialsException(BadCredentialsException ex, WebRequest request) {
        String msg = "Invalid username or password";
        ErrorData errorData = new ErrorData(Map.of("global", msg));
        LoginResponse response = LoginResponse.builder()
                .success(false)
                .message(msg)
                .data(errorData)
                .build();
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Xử lý tất cả các exception chưa được catch riêng
     * Trả về HTTP 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<LoginResponse> handleGlobalException(Exception ex, WebRequest request) {
        ErrorData errorData = new ErrorData(Map.of("global", ex.getMessage()));
        LoginResponse response = LoginResponse.builder()
                .success(false)
                .message("Đã xảy ra lỗi hệ thống")
                .data(errorData)
                .build();
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Xử lý khi gọi API không tồn tại
     * Trả về HTTP 404 Not Found
     */
    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
            NoHandlerFoundException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        ErrorData errorData = new ErrorData(Map.of("global", "API not found"));
        LoginResponse response = LoginResponse.builder()
                .success(false)
                .message("API không tồn tại")
                .data(errorData)
                .build();
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

}