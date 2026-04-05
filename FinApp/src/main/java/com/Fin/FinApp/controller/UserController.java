package com.Fin.FinApp.controller;

import com.Fin.FinApp.entity.User;
import com.Fin.FinApp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
// Only users with the ADMIN role can even enter this controller!
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET: Fetch all users in the company
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // POST: Create a new Viewer or Analyst (Protected with @Valid!)
    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) { // <-- ADDED @Valid
        // Returning 201 CREATED instead of 200 OK for new resources
        return new ResponseEntity<>(userService.createUser(user), HttpStatus.CREATED);
    }

    // PUT: Update a user's role or deactivate them (Protected with @Valid!)
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable UUID id, @Valid @RequestBody User user) { // <-- ADDED @Valid
        return ResponseEntity.ok(userService.updateUser(id, user));
    }
}