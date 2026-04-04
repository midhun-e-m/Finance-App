package com.Fin.FinApp.service;

import com.Fin.FinApp.entity.User;
import com.Fin.FinApp.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 1. Get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // 2. Admin creates a new user
    public User createUser(User user) {
        // We must hash their password before saving to the database!
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    // 3. Admin updates a user's role or active status
    public User updateUser(UUID id, User updatedUserData) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update the allowed fields
        existingUser.setRole(updatedUserData.getRole());
        existingUser.setIsActive(updatedUserData.getIsActive());

        return userRepository.save(existingUser);
    }
}