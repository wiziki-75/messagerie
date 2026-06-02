package com.example.messagerie.service;

import com.example.messagerie.model.User;
import com.example.messagerie.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User create(String name) {
        User u = new User();
        u.setName(name);
        return userRepository.save(u);
    }

    @Transactional(readOnly = true)
    public User require(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable: " + id));
    }

    @Transactional(readOnly = true)
    public List<User> list() { return userRepository.findAll(); }
}
