package com.Fin.FinApp.controller;

import com.Fin.FinApp.dto.AuthRequestDTO;
import com.Fin.FinApp.dto.AuthResponseDTO;
import com.Fin.FinApp.entity.Role;
import com.Fin.FinApp.entity.User;
import com.Fin.FinApp.repository.UserRepository;
import com.Fin.FinApp.service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
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
    public ResponseEntity<AuthResponseDTO> register(@RequestBody AuthRequestDTO request) {
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
    public ResponseEntity<AuthResponseDTO> login(@RequestBody AuthRequestDTO request) {
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if passwords match
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }


        String jwtToken = jwtService.generateToken(user);
        return ResponseEntity.ok(new AuthResponseDTO(jwtToken));
    }
}