package com.example.messagerie.service;

import com.example.messagerie.model.User;
import com.example.messagerie.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User create(String name, String username, String rawPassword) {
        return create(name, username, rawPassword, com.example.messagerie.model.Role.USER);
    }

    @Transactional
    public User create(String name, String username, String rawPassword, com.example.messagerie.model.Role role) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Le nom d'affichage est requis");
        if (username == null || username.isBlank()) throw new IllegalArgumentException("Le nom d'utilisateur est requis");
        if (rawPassword == null || rawPassword.length() < 6) throw new IllegalArgumentException("Le mot de passe doit contenir au moins 6 caractères");
        userRepository.findByUsername(username).ifPresent(u -> { throw new IllegalArgumentException("Nom d'utilisateur déjà utilisé"); });
        userRepository.findByName(name).ifPresent(u -> { throw new IllegalArgumentException("Nom d'affichage déjà utilisé"); });
        User u = new User();
        u.setName(name);
        u.setUsername(username);
        u.setPasswordHash(passwordEncoder.encode(rawPassword));
        u.setRole(role);
        return userRepository.save(u);
    }

    @Transactional(readOnly = true)
    public User require(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable: " + id));
    }

    @Transactional(readOnly = true)
    public User requireByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable: " + username));
    }

    @Transactional(readOnly = true)
    public List<User> list() {
        return userRepository.findAll();
    }
}
