package com.flashcards.backend.flashcards.controller;

import com.flashcards.backend.flashcards.annotation.ApiDocumentation;
import com.flashcards.backend.flashcards.dto.CreateUserDto;
import com.flashcards.backend.flashcards.dto.UserDto;
import com.flashcards.backend.flashcards.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for managing user accounts and profiles")
public class UserController {
    private final UserService userService;

    @GetMapping("/{id}")
    @ApiDocumentation.GetUserById
    public ResponseEntity<UserDto> getUserById(@ApiDocumentation.UserIdParam @PathVariable String id) {
        log.debug("GET /api/users/{} - Finding user by id", id);

        Optional<UserDto> user = userService.findById(id);

        return user.map(userDto -> {
            log.debug("GET /api/users/{} - User found: {}", id, userDto.getUsername());
            return ResponseEntity.ok(userDto);
        }).orElseGet(() -> {
            log.debug("GET /api/users/{} - User not found", id);
            return ResponseEntity.notFound().build();
        });
    }

    @GetMapping("/by-username")
    @ApiDocumentation.GetUserByUsername
    public ResponseEntity<UserDto> getUserByUsername(@ApiDocumentation.UsernameParam @RequestParam String username) {
        log.debug("GET /api/users/by-username?username={} - Finding user by username", username);

        Optional<UserDto> user = userService.findByUsername(username);

        return user.map(userDto -> {
            log.debug("GET /api/users/by-username - User found: {}", userDto.getUsername());
            return ResponseEntity.ok(userDto);
        }).orElseGet(() -> {
            log.debug("GET /api/users/by-username - User not found with username: {}", username);
            return ResponseEntity.notFound().build();
        });
    }

    @GetMapping("/by-email")
    @ApiDocumentation.GetUserByEmail
    public ResponseEntity<UserDto> getUserByEmail(@ApiDocumentation.EmailParam @RequestParam String email) {
        log.debug("GET /api/users/by-email?email={} - Finding user by email", email);

        Optional<UserDto> user = userService.findByEmail(email);

        return user.map(userDto -> {
            log.debug("GET /api/users/by-email - User found: {}", userDto.getUsername());
            return ResponseEntity.ok(userDto);
        }).orElseGet(() -> {
            log.debug("GET /api/users/by-email - User not found with email: {}", email);
            return ResponseEntity.notFound().build();
        });
    }

    @GetMapping
    @ApiDocumentation.GetAllUsers
    public ResponseEntity<List<UserDto>> getAllUsers() {
        log.debug("GET /api/users - Finding all users");

        List<UserDto> users = userService.findAll();

        log.debug("GET /api/users - Found {} users", users.size());
        return ResponseEntity.ok(users);
    }

    @PostMapping
    @ApiDocumentation.CreateUser
    public ResponseEntity<UserDto> createUser(@ApiDocumentation.CreateUserBody @Valid @RequestBody CreateUserDto createUserDto) {
        log.info("POST /api/users - Creating new user with username: {}", createUserDto.getUsername());

        UserDto createdUser = userService.createUser(createUserDto);

        log.info("POST /api/users - User created successfully with id: {} and username: {}",
                createdUser.getId(), createdUser.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PutMapping("/{id}")
    @ApiDocumentation.UpdateUser
    public ResponseEntity<UserDto> updateUser(@ApiDocumentation.UserIdParam @PathVariable String id,
                                             @ApiDocumentation.UpdateUserBody @Valid @RequestBody UserDto userDto) {
        log.info("PUT /api/users/{} - Updating user", id);

        UserDto updatedUser = userService.updateUser(id, userDto);

        log.info("PUT /api/users/{} - User updated successfully: {}", id, updatedUser.getUsername());
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @ApiDocumentation.DeleteUser
    public ResponseEntity<Void> deleteUser(@ApiDocumentation.UserIdParam @PathVariable String id) {
        log.info("DELETE /api/users/{} - Deleting user", id);

        userService.deleteUser(id);

        log.info("DELETE /api/users/{} - User deleted successfully", id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/check-username")
    @ApiDocumentation.CheckUsernameExists
    public ResponseEntity<Boolean> checkUsernameExists(@ApiDocumentation.UsernameParam @RequestParam String username) {
        log.debug("GET /api/users/check-username?username={} - Checking username existence", username);

        boolean exists = userService.existsByUsername(username);

        log.debug("GET /api/users/check-username - Username {} exists: {}", username, exists);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/check-email")
    @ApiDocumentation.CheckEmailExists
    public ResponseEntity<Boolean> checkEmailExists(@ApiDocumentation.EmailParam @RequestParam String email) {
        log.debug("GET /api/users/check-email?email={} - Checking email existence", email);

        boolean exists = userService.existsByEmail(email);

        log.debug("GET /api/users/check-email - Email {} exists: {}", email, exists);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/count")
    @ApiDocumentation.GetUserCount
    public ResponseEntity<Long> getUserCount() {
        log.debug("GET /api/users/count - Getting user count");

        long count = userService.countUsers();

        log.debug("GET /api/users/count - Total users: {}", count);
        return ResponseEntity.ok(count);
    }
}