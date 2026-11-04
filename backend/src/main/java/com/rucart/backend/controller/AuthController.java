package com.rucart.backend.controller;

import com.rucart.backend.dto.ApiResponse;
import com.rucart.backend.dto.AuthResponse;
import com.rucart.backend.dto.LoginRequest;
import com.rucart.backend.dto.Msg91VerifyRequest;
import com.rucart.backend.dto.RegisterRequest;
import com.rucart.backend.dto.SendOtpRequest;
import com.rucart.backend.dto.VerifyOtpRequest;
import com.rucart.backend.model.Role;
import com.rucart.backend.model.User;
import com.rucart.backend.security.CustomUserDetails;
import com.rucart.backend.service.AuthService;
import com.rucart.backend.service.Msg91Service;
import com.rucart.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final Msg91Service msg91Service;

    public AuthController(AuthService authService, UserService userService, Msg91Service msg91Service) {
        this.authService = authService;
        this.userService = userService;
        this.msg91Service = msg91Service;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.ok("Registration successful", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }

    /**
     * Returns the MSG91 configuration for the mobile client.
     */
    @GetMapping("/msg91/config")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMsg91Config() {
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "widgetId", msg91Service.getWidgetId(),
                "configured", msg91Service.isConfigured()
        )));
    }

    /**
     * Verifies the token returned by the MSG91 OTP widget and authenticates the user.
     */
    @PostMapping("/msg91/verify")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyMsg91Token(@Valid @RequestBody Msg91VerifyRequest request) {
        AuthResponse response = authService.loginOrRegisterWithMsg91(request);
        return ResponseEntity.ok(ApiResponse.ok("MSG91 token verified", response));
    }

    /**
     * Direct OTP Send endpoint using MSG91.
     */
    @PostMapping("/otp/send")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        boolean sent = authService.sendOtp(request.getPhone());
        return ResponseEntity.ok(ApiResponse.ok("OTP sent successfully to " + request.getPhone(), Map.of(
                "sent", sent,
                "phone", request.getPhone()
        )));
    }

    /**
     * Direct OTP Verify endpoint using MSG91.
     */
    @PostMapping("/otp/verify")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        AuthResponse response = authService.verifyOtpAndLogin(request);
        return ResponseEntity.ok(ApiResponse.ok("OTP verified successfully", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<User>> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Unauthenticated user"));
        }
        User user = userService.getUserById(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.ok(user));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<User>> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getUserById(id)));
    }

    @GetMapping("/users/role/{role}")
    public ResponseEntity<ApiResponse<List<User>>> getUsersByRole(@PathVariable Role role) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getUsersByRole(role)));
    }
}
