package org.eckmo.functionnal.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eckmo.functionnal.dto.UserDTO;
import org.eckmo.functionnal.functional.UserProcessor;
import org.eckmo.functionnal.model.User;
import org.eckmo.functionnal.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * Get all users
     */
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        log.info("GET /api/users - Fetching all users");
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * Get user by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        log.info("GET /api/users/{} - Fetching user", id);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    /**
     * Get all active users
     */
    @GetMapping("/active/list")
    public ResponseEntity<List<UserDTO>> getActiveUsers() {
        log.info("GET /api/users/active/list - Fetching active users");
        return ResponseEntity.ok(userService.getActiveUsers());
    }

    /**
     * Get all instructors
     */
    @GetMapping("/instructors/list")
    public ResponseEntity<List<UserDTO>> getInstructors() {
        log.info("GET /api/users/instructors/list - Fetching instructors");
        return ResponseEntity.ok(userService.getInstructors());
    }

    /**
     * Create new user
     */
    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO) {
        log.info("POST /api/users - Creating new user");
        UserDTO created = userService.createUser(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Update user
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        log.info("PUT /api/users/{} - Updating user", id);
        return ResponseEntity.ok(userService.updateUser(id, userDTO));
    }

    /**
     * Delete user
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("DELETE /api/users/{} - Deleting user", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Count users by role using functional interface
     */
    @GetMapping("/count/{role}")
    public ResponseEntity<Long> countUsersByRole(@PathVariable String role) {
        log.info("GET /api/users/count/{} - Counting users", role);
        long count = userService.countUsersByRole(User.UserRole.valueOf(role.toUpperCase()));
        return ResponseEntity.ok(count);
    }

    /**
     * Process user with functional interface
     */
    @PostMapping("/{id}/process")
    public ResponseEntity<String> processUser(@PathVariable Long id) {
        log.info("POST /api/users/{}/process - Processing user", id);

        // Example processor implementation using functional interface
        UserProcessor processor = (userId, email, role) ->
                String.format("Processed User ID: %d, Email: %s, Role: %s", userId, email, role);

        String result = userService.processUser(id, processor);
        return ResponseEntity.ok(result);
    }
}

