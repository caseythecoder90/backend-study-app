package com.flashcards.backend.flashcards.dao;

import com.flashcards.backend.flashcards.model.User;

import java.util.List;
import java.util.Optional;

public interface UserDao {
    Optional<User> findById(String id);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findAll();
    User save(User user);
    User update(User user);
    void deleteById(String id);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByOauthProviderAndOauthId(String oauthProvider, String oauthId);
    long count();
}