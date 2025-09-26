package com.flashcards.backend.flashcards.config;

import com.flashcards.backend.flashcards.dao.UserDao;
import com.flashcards.backend.flashcards.model.Role;
import com.flashcards.backend.flashcards.model.User;
import com.flashcards.backend.flashcards.service.PasswordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.flashcards.backend.flashcards.constants.ErrorMessages.INIT_ADMIN_CREATION_FAILED;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.INIT_ADMIN_UPGRADE_FAILED;
import static org.apache.commons.lang3.BooleanUtils.isFalse;
import static org.apache.logging.log4j.util.Strings.isNotBlank;

/**
 * Initializes database with essential data on application startup.
 * This includes creating an admin user if none exists and admin credentials are provided.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserDao userDao;
    private final PasswordService passwordService;

    @Value("${ADMIN_EMAIL:}")
    private String adminEmail;

    @Value("${ADMIN_USERNAME:}")
    private String adminUsername;

    @Value("${ADMIN_PASSWORD:}")
    private String adminPassword;

    @Value("${ADMIN_FIRST_NAME:System}")
    private String adminFirstName;

    @Value("${ADMIN_LAST_NAME:Administrator}")
    private String adminLastName;

    @Override
    public void run(ApplicationArguments args) {
        initializeAdminUser();
    }

    private void initializeAdminUser() {
        try {
            if (isNotBlank(adminEmail) && isNotBlank(adminPassword)) {
                log.info("Admin initialization: Checking for existing admin users");

                long adminCount = userDao.countByRole(Role.ADMIN);

                if (adminCount == 0) {
                    log.info("No admin users found. Creating initial admin user");
                    handleAdminUserCreation();
                } else {
                    log.info("Found {} existing admin user(s). Skipping admin creation", adminCount);
                    handleExistingUserUpgrade();
                }
            } else {
                log.debug("Admin credentials not provided in environment variables. Skipping admin initialization");
                log.debug("To create an admin user, set ADMIN_EMAIL and ADMIN_PASSWORD environment variables");
            }
        } catch (Exception e) {
            log.error("Admin initialization failed: {}", e.getMessage(), e);
        }
    }

    private void handleAdminUserCreation() {
        try {
            Optional<User> existingUser = userDao.findByEmail(adminEmail);

            if (existingUser.isPresent()) {
                upgradeUserToAdmin(existingUser.get());
            } else {
                createNewAdminUser();
            }
        } catch (Exception e) {
            log.error("Failed to initialize admin user", e);
        }
    }

    private void handleExistingUserUpgrade() {
        if (isNotBlank(adminEmail)) {
            Optional<User> specifiedUser = userDao.findByEmail(adminEmail);

            if (specifiedUser.isPresent() && isFalse(specifiedUser.get().getRoles().contains(Role.ADMIN))) {
                log.info("Upgrading specified user {} to admin as per configuration", adminEmail);
                upgradeUserToAdmin(specifiedUser.get());
            }
        }
    }

    private void upgradeUserToAdmin(User user) {
        try {
            log.info("User with email {} already exists. Upgrading to admin role", user.getEmail());

            Set<Role> roles = new HashSet<>(user.getRoles());
            roles.add(Role.ADMIN);
            user.setRoles(roles);
            user.setUpdatedAt(LocalDateTime.now());

            userDao.save(user);
            log.info("Successfully upgraded user {} to admin role", user.getEmail());

        } catch (Exception e) {
            log.error("Failed to upgrade user {} to admin role", user.getUsername(), e);
        }
    }

    private void createNewAdminUser() {
        String username = isNotBlank(adminUsername) ?
            adminUsername :
            adminEmail.split("@")[0];

        User adminUser = User.builder()
                .username(username)
                .email(adminEmail)
                .password(passwordService.encryptPassword(adminPassword))
                .firstName(adminFirstName)
                .lastName(adminLastName)
                .roles(Set.of(Role.USER, Role.ADMIN))
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .totpEnabled(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        User savedAdmin = userDao.save(adminUser);
        log.info("Successfully created admin user: {} ({})", savedAdmin.getUsername(), savedAdmin.getEmail());
        log.info("Admin user can now log in with the provided credentials");
    }
}