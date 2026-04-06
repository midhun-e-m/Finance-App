package com.Fin.FinApp.controller;

import com.Fin.FinApp.dto.AuthRequestDTO;
import com.Fin.FinApp.dto.AuthResponseDTO;
import com.Fin.FinApp.entity.Role;
import com.Fin.FinApp.entity.User;
import com.Fin.FinApp.exception.InvalidCredentialsException;
import com.Fin.FinApp.exception.ResourceNotFoundException;
import com.Fin.FinApp.repository.UserRepository;
import com.Fin.FinApp.service.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // 1. REGISTER
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AuthRequestDTO request) {

        // DUPLICATE EMAIL CHECK: Returns a clean 409 Conflict instead of crashing the DB
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email is already in use");
        }

        // Create new user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.VIEWER);

        userRepository.save(user);

        String jwtToken = jwtService.generateToken(user);
        return ResponseEntity.ok(new AuthResponseDTO(jwtToken));
    }

    // 2. LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequestDTO request) {
        // Find user by email, throw our custom 404 Exception if missing
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if passwords match, throw our custom 401 Exception if wrong
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid password");
        }

        String jwtToken = jwtService.generateToken(user);
        return ResponseEntity.ok(new AuthResponseDTO(jwtToken));
    }
}