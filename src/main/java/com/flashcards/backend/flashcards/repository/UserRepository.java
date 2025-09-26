package com.flashcards.backend.flashcards.repository;

import com.flashcards.backend.flashcards.model.Role;
import com.flashcards.backend.flashcards.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByOauthProviderAndOauthId(String oauthProvider, String oauthId);
    List<User> findByRolesContaining(Role role);
    long countByRolesContaining(Role role);
}