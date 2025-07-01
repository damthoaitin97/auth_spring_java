package com.example.hallyuclass.auth.service;

import com.example.hallyuclass.auth.dto.request.RefreshTokenRequest;
import com.example.hallyuclass.auth.dto.request.SignInRequest;
import com.example.hallyuclass.auth.dto.request.SignUpRequest;
import com.example.hallyuclass.auth.dto.response.LoginResponse;
import com.example.hallyuclass.auth.dto.response.LoginUser;
import com.example.hallyuclass.auth.dto.response.LoginTokens;
import com.example.hallyuclass.auth.dto.response.LoginData;
import com.example.hallyuclass.auth.exception.ResourceNotFoundException;
import com.example.hallyuclass.auth.exception.ValidationException;
import com.example.hallyuclass.auth.model.Roles;
import com.example.hallyuclass.auth.model.UserAuth;
import com.example.hallyuclass.auth.model.UserProfile;
import com.example.hallyuclass.auth.repository.UserAuthRepository;
import com.example.hallyuclass.auth.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.*;
import java.util.concurrent.TimeUnit;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

        private final UserAuthRepository userAuthRepository;
        private final UserProfileRepository userProfileRepository;
        private final PasswordEncoder passwordEncoder;
        private final AuthenticationManager authenticationManager;
        private final JwtService jwtService;
        private final RedisTemplate<String, String> redisTemplate;
        private final Validator validator;
        @Value("${GOOGLE_CLIENT_ID}")
        private String CLIENT_ID;

        @Override
        @Transactional
        public LoginResponse signup(SignUpRequest signUpRequest) {
                Map<String, String> errors = new HashMap<>();

                // Step 1: Manual validation
                Set<ConstraintViolation<SignUpRequest>> violations = validator.validate(signUpRequest);
                if (!violations.isEmpty()) {
                        for (ConstraintViolation<SignUpRequest> violation : violations) {
                                errors.put(violation.getPropertyPath().toString(), violation.getMessage());
                        }
                }

                // Step 2: Business logic validation
                if (userAuthRepository.existsByUsername(signUpRequest.getUsername())) {
                        errors.put("username", "Username already exists");
                }

                if (userProfileRepository.existsByEmail(signUpRequest.getEmail())) {
                        errors.put("email", "Email already registered");
                }
                if (userProfileRepository.existsByPhoneNumber(signUpRequest.getPhoneNumber())) {
                        errors.put("phoneNumber", "phoneNumber already registered");
                }

                // Step 3: Check if there are any errors
                if (!errors.isEmpty()) {
                        throw new ValidationException(errors);
                }

                UserAuth user = new UserAuth();
                user.setUsername(signUpRequest.getUsername());
                user.setRole(Roles.USER);
                user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));

                UserProfile userProfile = new UserProfile();
                userProfile.setUsername(signUpRequest.getUsername());
                userProfile.setEmail(signUpRequest.getEmail());
                userProfile.setName(signUpRequest.getName());
                userProfile.setPhoneNumber(signUpRequest.getPhoneNumber());
                userProfile.setAddress(signUpRequest.getAddress());
                if (signUpRequest.getImg() != null) {
                        userProfile.setImg(signUpRequest.getImg());
                } else {
                        userProfile
                                        .setImg("https://tintuc.dienthoaigiakho.vn/wp-content/uploads/2024/01/avatar-nam-nu-trang-2.jpg");
                }
                userProfile.setUserAuth(user);
                user.setProfile(userProfile);
                userAuthRepository.save(user);

                var jwt = jwtService.generateToken(user);
                var refreshToken = jwtService.generateRefreshToken(new HashMap<>(), user);
                redisTemplate.opsForValue().set(user.getUsername(), refreshToken, 7, TimeUnit.DAYS);

                LoginUser loginUser = LoginUser.builder()
                                .img(userProfile.getImg())
                                .username(user.getUsername())
                                .email(userProfile.getEmail())
                                .name(userProfile.getName())
                                .roles(user.getRole().name().toLowerCase())
                                .build();

                LoginTokens loginTokens = LoginTokens.builder()
                                .accessToken(jwt)
                                .refreshToken(refreshToken)
                                .expiresIn(3600)
                                .build();

                LoginData loginData = LoginData.builder()
                                .user(loginUser)
                                .tokens(loginTokens)
                                .build();

                return LoginResponse.builder()
                                .success(true)
                                .message("Đăng ký thành công")
                                .data(loginData)
                                .build();
        }

        @Override
        public LoginResponse signin(SignInRequest signInRequest) {
                System.out.println("=== DEBUG SIGNIN ===");
                System.out.println("Username: " + signInRequest.getUsername());

                try {
                        authenticationManager.authenticate(
                                        new UsernamePasswordAuthenticationToken(signInRequest.getUsername(),
                                                        signInRequest.getPassword()));
                        System.out.println("Authentication successful");
                } catch (Exception e) {
                        System.out.println("Authentication failed: " + e.getMessage());
                        throw e;
                }

                System.out.println("Looking for user in database...");
                var user = userAuthRepository.findByUsername(signInRequest.getUsername())
                                .orElseThrow(() -> {
                                        System.out.println("User not found in database!");
                                        return new ResourceNotFoundException("User not found");
                                });
                System.out.println("User found: " + user.getUsername());

                var jwt = jwtService.generateToken(user);
                var refreshToken = jwtService.generateRefreshToken(new HashMap<>(), user);

                redisTemplate.opsForValue().set(user.getUsername(), refreshToken, 7, TimeUnit.DAYS);

                UserProfile profile = user.getProfile();
                System.out.println("Profile: " + (profile != null ? "exists" : "null"));

                LoginUser loginUser = LoginUser.builder()
                                .img(profile != null ? profile.getImg() : null)
                                .username(user.getUsername())
                                .email(profile != null ? profile.getEmail() : null)
                                .name(profile != null ? profile.getName() : null)
                                .roles(user.getRole().name().toLowerCase())
                                .build();

                LoginTokens loginTokens = LoginTokens.builder()
                                .accessToken(jwt)
                                .refreshToken(refreshToken)
                                .expiresIn(3600) // hoặc thời gian thực tế bạn set cho access token
                                .build();

                LoginData loginData = LoginData.builder()
                                .user(loginUser)
                                .tokens(loginTokens)
                                .build();

                System.out.println("=== SIGNIN SUCCESS ===");
                return LoginResponse.builder()
                                .success(true)
                                .message("Đăng nhập thành công")
                                .data(loginData)
                                .build();
        }

        @Override
        public LoginResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
                String userName = jwtService.extractUserName(refreshTokenRequest.getToken());
                UserAuth user = userAuthRepository.findByUsername(userName)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                String storedToken = redisTemplate.opsForValue().get(userName);

                if (refreshTokenRequest.getToken().equals(storedToken)
                                && jwtService.isTokenValid(refreshTokenRequest.getToken(), user)) {
                        var jwt = jwtService.generateToken(user);
                        var newRefreshToken = jwtService.generateRefreshToken(new HashMap<>(), user);

                        redisTemplate.opsForValue().set(user.getUsername(), newRefreshToken, 7, TimeUnit.DAYS);

                        UserProfile profile = user.getProfile();
                        LoginUser loginUser = LoginUser.builder()
                                        .img(profile != null ? profile.getImg() : null)
                                        .username(user.getUsername())
                                        .email(profile != null ? profile.getEmail() : null)
                                        .name(profile != null ? profile.getName() : null)
                                        .roles(user.getRole().name().toLowerCase())
                                        .build();

                        LoginTokens loginTokens = LoginTokens.builder()
                                        .accessToken(jwt)
                                        .refreshToken(newRefreshToken)
                                        .expiresIn(3600)
                                        .build();

                        LoginData loginData = LoginData.builder()
                                        .user(loginUser)
                                        .tokens(loginTokens)
                                        .build();

                        return LoginResponse.builder()
                                        .success(true)
                                        .message("Làm mới token thành công")
                                        .data(loginData)
                                        .build();
                }
                throw new ResourceNotFoundException("Invalid refresh token");
        }

        @Override
        public void logout(String username) {
                redisTemplate.delete(username);
        }

        @Transactional
        @Override
        public ResponseEntity<?> verifyGoogleIdTokenAndGenerateJwt(String idTokenString) {
                GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                                new NetHttpTransport(), JacksonFactory.getDefaultInstance())
                                .setAudience(Collections.singletonList(CLIENT_ID))
                                .build();

                try {
                        GoogleIdToken idToken = verifier.verify(idTokenString);
                        if (idToken == null) {
                                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("ID token không hợp lệ");
                        }

                        GoogleIdToken.Payload payload = idToken.getPayload();
                        String email = payload.getEmail();
                        String name = (String) payload.get("name");
                        String picture = (String) payload.get("picture");

                        if (email == null || email.trim().isEmpty()) {
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                                .body("Không lấy được email từ Google");
                        }

                        String username = email.trim().toLowerCase(); // Đảm bảo username không rỗng

                        // Kiểm tra xem userProfile đã tồn tại theo email chưa
                        UserProfile userProfile = userProfileRepository.findByEmail(email);
                        UserAuth userAuth;

                        if (userProfile == null) {
                                // Nếu đã có username trùng email thì không tạo nữa
                                if (userAuthRepository.existsByUsername(username)) {
                                        return ResponseEntity.status(HttpStatus.CONFLICT).body("Tài khoản đã tồn tại!");
                                }

                                // Tạo mới userAuth + userProfile
                                userAuth = new UserAuth();
                                userAuth.setUsername(username);
                                userAuth.setPassword(passwordEncoder.encode("123123")); // Mặc định password
                                userAuth.setRole(Roles.USER);

                                userProfile = new UserProfile();
                                userProfile.setUsername(username);
                                userProfile.setEmail(email);
                                userProfile.setName(name);
                                userProfile.setImg(picture != null ? picture
                                                : "https://tintuc.dienthoaigiakho.vn/wp-content/uploads/2024/01/avatar-nam-nu-trang-2.jpg");
                                userProfile.setPhoneNumber("");
                                userProfile.setAddress("");
                                userProfile.setUserAuth(userAuth);

                                userAuth.setProfile(userProfile);
                                userAuthRepository.save(userAuth);
                        } else {
                                // Tìm lại userAuth từ profile
                                userAuth = userAuthRepository.findByUsername(userProfile.getUsername())
                                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                        }

                        // Tạo token
                        String jwt = jwtService.generateToken(userAuth);
                        String refreshToken = jwtService.generateRefreshToken(new HashMap<>(), userAuth);
                        redisTemplate.opsForValue().set(userAuth.getUsername(), refreshToken, 7, TimeUnit.DAYS);

                        LoginUser loginUser = LoginUser.builder()
                                        .img(userProfile.getImg())
                                        .username(userAuth.getUsername())
                                        .email(userProfile.getEmail())
                                        .name(userProfile.getName())
                                        .roles(userAuth.getRole().name().toLowerCase())
                                        .build();

                        LoginTokens loginTokens = LoginTokens.builder()
                                        .accessToken(jwt)
                                        .refreshToken(refreshToken)
                                        .expiresIn(3600)
                                        .build();

                        LoginData loginData = LoginData.builder()
                                        .user(loginUser)
                                        .tokens(loginTokens)
                                        .build();

                        return ResponseEntity.ok(LoginResponse.builder()
                                        .success(true)
                                        .message("Đăng nhập Google thành công")
                                        .data(loginData)
                                        .build());

                } catch (Exception e) {
                        e.printStackTrace();
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token verification failed");
                }
        }

}