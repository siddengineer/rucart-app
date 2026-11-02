package com.rucart.backend.service;

import com.rucart.backend.dto.AuthRequest;
import com.rucart.backend.dto.AuthResponse;
import com.rucart.backend.model.Role;
import com.rucart.backend.model.User;
import com.rucart.backend.repository.UserRepository;
import com.rucart.backend.security.CustomUserDetails;
import com.rucart.backend.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse authenticate(AuthRequest request) {
        Optional<User> userOpt = userRepository.findByPhone(request.getPhone());
        User user;

        if (userOpt.isPresent()) {
            user = userOpt.get();
        } else {
            user = new User();
            user.setPhone(request.getPhone());
            user.setName(request.getName() != null && !request.getName().isBlank() ? request.getName() : "User " + request.getPhone().substring(Math.max(0, request.getPhone().length() - 4)));
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword() != null ? request.getPassword() : "123456"));
            user.setRole(request.getRole() != null ? request.getRole() : Role.CUSTOMER);
            user.setAddress(request.getAddress() != null ? request.getAddress() : "Shivajinagar, Pune");
            user = userRepository.save(user);
        }

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String token = jwtUtil.generateToken(userDetails, user.getId(), user.getRole());
        return new AuthResponse(user, token, "Authentication successful");
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public User getUserByPhone(String phone) {
        return userRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("User not found with phone: " + phone));
    }

    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
