package com.rucart.backend.service;

import com.rucart.backend.dto.AuthResponse;
import com.rucart.backend.dto.LoginRequest;
import com.rucart.backend.dto.Msg91VerifyRequest;
import com.rucart.backend.dto.RegisterRequest;
import com.rucart.backend.dto.VerifyOtpRequest;
import com.rucart.backend.model.Role;
import com.rucart.backend.model.User;
import com.rucart.backend.repository.UserRepository;
import com.rucart.backend.security.CustomUserDetails;
import com.rucart.backend.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final Msg91Service msg91Service;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       AuthenticationManager authenticationManager,
                       Msg91Service msg91Service) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.msg91Service = msg91Service;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException("A user with this mobile number already exists: " + request.getPhone());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank() && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("A user with this email address already exists: " + request.getEmail());
        }

        User user = new User();
        user.setName(request.getName().trim());
        user.setPhone(request.getPhone().trim());
        user.setEmail(request.getEmail() != null && !request.getEmail().isBlank() ? request.getEmail().trim() : null);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() != null ? request.getRole() : Role.CUSTOMER);
        user.setAddress(request.getAddress() != null ? request.getAddress() : "Shivajinagar, Pune");
        user.setActive(true);

        user = userRepository.save(user);

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String token = jwtUtil.generateToken(userDetails, user.getId(), user.getRole());

        return new AuthResponse(user, token, "User registration successful");
    }

    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getPhone().trim(), request.getPassword())
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();
            String token = jwtUtil.generateToken(userDetails, user.getId(), user.getRole());

            return new AuthResponse(user, token, "Login successful");
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("Invalid mobile number or password");
        }
    }

    /**
     * Verifies the MSG91 OTP widget access token and logs in or registers the customer.
     */
    @Transactional
    public AuthResponse loginOrRegisterWithMsg91(Msg91VerifyRequest request) {
        boolean verified = msg91Service.verifyAccessToken(request.getAccessToken());
        if (!verified) {
            throw new IllegalArgumentException("MSG91 token verification failed. Invalid or expired OTP verification code.");
        }

        String phone = request.getPhone() != null ? request.getPhone().trim() : "";
        if (phone.isEmpty()) {
            throw new IllegalArgumentException("Phone number is required for user account verification.");
        }

        return getOrCreateUserAndGenerateAuthResponse(phone, request.getName(), request.getRole(), "MSG91 verification successful");
    }

    /**
     * Dispatches an OTP SMS to user's phone via MSG91 service.
     */
    public boolean sendOtp(String phone) {
        String cleanPhone = phone.replaceAll("[^0-9]", "");
        if (cleanPhone.length() < 10) {
            throw new IllegalArgumentException("Please provide a valid 10-digit mobile number.");
        }
        String otp = String.format("%06d", new Random().nextInt(1_000_000));
        return msg91Service.sendOtpSms(cleanPhone, otp, "RUCART Account Verification");
    }

    /**
     * Verifies direct OTP code and completes authentication.
     */
    @Transactional
    public AuthResponse verifyOtpAndLogin(VerifyOtpRequest request) {
        boolean valid = msg91Service.verifyDirectOtp(request.getPhone(), request.getOtp());
        if (!valid) {
            throw new IllegalArgumentException("Invalid or expired OTP. Please try again.");
        }

        return getOrCreateUserAndGenerateAuthResponse(request.getPhone(), request.getName(), request.getRole(), "OTP verified successfully");
    }

    private AuthResponse getOrCreateUserAndGenerateAuthResponse(String phone, String name, String roleStr, String message) {
        Optional<User> existingUser = userRepository.findByPhone(phone);
        User user;

        if (existingUser.isPresent()) {
            user = existingUser.get();
        } else {
            user = new User();
            user.setPhone(phone);
            user.setName(name != null && !name.isBlank() ? name.trim() : "Customer " + phone.substring(Math.max(0, phone.length() - 4)));
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // Random password for OTP-only accounts
            Role role = Role.CUSTOMER;
            if (roleStr != null && !roleStr.isBlank()) {
                try {
                    role = Role.valueOf(roleStr.toUpperCase());
                } catch (Exception ignored) {}
            }
            user.setRole(role);
            user.setActive(true);
            user = userRepository.save(user);
        }

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String token = jwtUtil.generateToken(userDetails, user.getId(), user.getRole());

        return new AuthResponse(user, token, message);
    }
}
