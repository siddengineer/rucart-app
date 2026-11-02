package com.rucart.backend.service;

import com.rucart.backend.dto.AuthResponse;
import com.rucart.backend.dto.LoginRequest;
import com.rucart.backend.dto.RegisterRequest;
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

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
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
}
